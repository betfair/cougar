/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.core.impl.logging.RequestLogEvent;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;


/**
 * Provides common ExecutionVenue functionality.
 */
public class BaseExecutionVenue implements ExecutionVenue {

    private final static CougarLogger logger = CougarLoggingUtils.getLogger(ExecutionVenue.class);

    private List<ExecutionPostProcessor> postProcessorList = new ArrayList<ExecutionPostProcessor>();
    private List<ExecutionPreProcessor> preProcessorList = new ArrayList<ExecutionPreProcessor>();
    private IdentityResolver identityResolver;

    private DelayQueue<ExpiringObserver> expiringObservers = new DelayQueue<>();

    protected Map<OperationKey, DefinedExecutable> registry = new HashMap<>();

    @Override
    public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder, long maxExecutionTime) {

        if (isInterceptingSupported()) {
            executable = new InterceptingExecutableWrapper(executable, preProcessorList, postProcessorList);
        }

        OperationKey key = def.getOperationKey();
        if (namespace != null) {
            key = new OperationKey(key, namespace);
        }
        if (registry.containsKey(key)) {
            throw new IllegalArgumentException("The Operation key "+key+" is already defined in the execution venue");
        }
        registry.put(key,
                new DefinedExecutable(
                        def,
                        executable,
                        recorder,
                        maxExecutionTime));
        logger.log(Level.INFO, "Registered operation: %s", key);
    }

    private boolean isInterceptingSupported() {
        return !preProcessorList.isEmpty() || !postProcessorList.isEmpty();
    }

    @Override
    public OperationDefinition getOperationDefinition(OperationKey key) {
        DefinedExecutable de = registry.get(key);
        if (de != null) {
            return de.def;
        } else {
            return null;
        }
    }

    @Override
    @ManagedAttribute
    public Set<OperationKey> getOperationKeys() {
        return registry.keySet();
    }

    private ExecutionContext resolveIdentitiesIfRequired(final ExecutionContext ctx) {
        if (ctx instanceof ExecutionContextWithTokens) {
            ExecutionContextWithTokens contextWithTokens = (ExecutionContextWithTokens) ctx;
            if (identityResolver == null) {
                contextWithTokens.setIdentityChain(new IdentityChainImpl(new ArrayList<Identity>()));
                // if there's no identity resolver then it can't tokenise any tokens back to the transport..
                contextWithTokens.getIdentityTokens().clear();
                return contextWithTokens;
            }
            else {
                IdentityChain chain = new IdentityChainImpl();
                try {
                    identityResolver.resolve(chain, contextWithTokens);
                }
                catch (InvalidCredentialsException e) {
                    if (e.getCredentialFaultCode() != null) { // Check if a custom error code should be used
                        ServerFaultCode sfc = ServerFaultCode.getByCredentialFaultCode(e.getCredentialFaultCode());
                        throw new CougarServiceException(sfc, "Credentials supplied were invalid", e);
                    }
                    throw new CougarServiceException(ServerFaultCode.SecurityException, "Credentials supplied were invalid", e);
                }
                // ensure the identity chain set in the context is immutable
                contextWithTokens.setIdentityChain(new IdentityChainImpl(chain.getIdentities()));
                contextWithTokens.getIdentityTokens().clear();
                List<IdentityToken> tokens = identityResolver.tokenise(contextWithTokens.getIdentity());
                if (tokens != null) {
                    contextWithTokens.getIdentityTokens().addAll(tokens);
                }
                return contextWithTokens;
            }
        }
        // might not be in the case of a client, or a batched transport which will have executed a seperate command to resolve identities for e.g.
        return ctx;
    }

    @Override
    public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, ExecutionObserver observer) {
        final DefinedExecutable de = registry.get(key);
        if (de == null) {
            logger.log(Level.FINE, "Not request logging request to URI: %s as no operation was found", key.toString());
            observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.NoSuchOperation, "Operation not found: "+key.toString())));
        } else {
            if (!(observer instanceof ExpiringObserver)) {
                final long expiryTime = de.maxExecutionTime;
                final ExpiringObserver expiringObserver = new ExpiringObserver(observer, expiryTime);
                if (expiringObserver.expires()) {
                    registerExpiringObserver(expiringObserver);
                }
                observer = expiringObserver;
            }
            observer = new ExecutionObserverWrapper(observer, de.recorder, key);

            try {
                ExecutionContext contextToUse = resolveIdentitiesIfRequired(ctx);
                de.exec.execute(contextToUse, key, args, observer, this);
            } catch (CougarException e) {
                observer.onResult(new ExecutionResult(e));
            } catch (Exception e) {
                observer.onResult(new ExecutionResult(
                        new CougarServiceException(ServerFaultCode.ServiceRuntimeException,
                                "Exception thrown by service method",
                                e)));
            }
        }
        if (observer instanceof ExpiringObserver) {
            deregisterExpiringObserver((ExpiringObserver) observer);
        }
    }

    @Override
    public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, ExecutionObserver observer, final Executor executor) {
        final DefinedExecutable de = registry.get(key);
        final long expiryTime = de != null ? de.maxExecutionTime : 0;
        final ExpiringObserver expiringObserver = new ExpiringObserver(observer, expiryTime);
        if (expiringObserver.expires()) {
            registerExpiringObserver(expiringObserver);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                execute(ctx, key, args, expiringObserver);
            }
        });
    }

    protected void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                processExpiredObservers();
            }
        }, "EV-ExecutableExpiryDetection");
        t.setDaemon(true);
        t.start();
    }

    private void processExpiredObservers() {
        // this executes on a daemon thread so we can happily loop forever
        while (true) {
            try {
                ExpiringObserver expired = expiringObservers.take();
                expired.expire();
            } catch (InterruptedException e) {
                // ignore, just carry on round
            }
        }
    }

    private void registerExpiringObserver(ExpiringObserver expiringObserver) {
        expiringObservers.add(expiringObserver);
    }

    private void deregisterExpiringObserver(ExpiringObserver expiringObserver) {
        expiringObservers.remove(expiringObserver);
    }

    private class ExpiringObserver implements ExecutionObserver, Delayed {

        private AtomicBoolean onResultCalled = new AtomicBoolean(false);
        private final ExecutionObserver observer;
        private final long expiryTime;

        private ExpiringObserver(final ExecutionObserver observer, final long expiryTime) {
            this.observer = observer;
            this.expiryTime = expiryTime;
        }

        public boolean expires() {
            return expiryTime != 0;
        }

        @Override
        public void onResult(ExecutionResult executionResult) {
            if (onResultCalled.compareAndSet(false, true)) {
                observer.onResult(executionResult);
            }
        }

        public int compareTo(ExpiringObserver o) {
            long diff = expiryTime - o.expiryTime;
            if (diff == 0) {
                return 0;
            }
            return diff < 0 ? -1 : 1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expiryTime-System.currentTimeMillis(),TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return compareTo((ExpiringObserver)o);
        }

        public void expire() {
            if (onResultCalled.compareAndSet(false, true)) {
                observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.Timeout, "Executable did not complete in time")));
            }
        }
    }

    // package private for testing
    static class DefinedExecutable {
        private final OperationDefinition def;
        private final Executable exec;
        private final ExecutionTimingRecorder recorder;
        private final long maxExecutionTime;

        public DefinedExecutable(final OperationDefinition def, final Executable exec, final ExecutionTimingRecorder recorder, final long maxExecutionTime) {
            this.def = def;
            this.exec = exec;
            if (recorder != null) {
                this.recorder = recorder;
            } else {
                throw new IllegalArgumentException("recorder must be defined");
            }
            this.maxExecutionTime = maxExecutionTime;
        }

        long getMaxExecutionTime() {
            return maxExecutionTime;
        }
    }

    /**
     * Wrapper class to ensure that all timings are recorded with the ExecutionManager
     */
    private class ExecutionObserverWrapper implements ExecutionObserver {

        private final ExecutionObserver observer;
        private final ExecutionTimingRecorder recorder;
        private final OperationKey operationKey;
        private final long startTime;

        public ExecutionObserverWrapper(final ExecutionObserver observer, final ExecutionTimingRecorder recorder, OperationKey key) {
            this.observer = observer;
            this.recorder = recorder;
            this.operationKey = key;
            startTime = System.nanoTime();
        }

        @Override
        public void onResult(ExecutionResult result) {
            if (operationKey.getType() == OperationKey.Type.Request) {
                switch (result.getResultType()) {
                    case Fault:
                        recorder.recordFailure((System.nanoTime() - startTime)/1000000.0);
                        break;
                    case Success:
                        recorder.recordCall((System.nanoTime() - startTime)/1000000.0);
                        break;
                }
            }
            observer.onResult(result);
        }
    }

    @Override
    public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList) {
        this.preProcessorList = preProcessorList;
    }

    @Override
    public void setPostProcessors(List<ExecutionPostProcessor> postProcessorList) {
        this.postProcessorList = postProcessorList;
    }

    public void setIdentityResolver(IdentityResolver identityResolver) {
        if (identityResolver != null) {
            this.identityResolver = identityResolver;
        }
    }



    public Executable getExecutable(final OperationKey key) {
        final DefinedExecutable de = registry.get(key);

        if (de != null) {
            if (de.exec instanceof InterceptingExecutableWrapper) {
                return ((InterceptingExecutableWrapper) de.exec).getExec();
            }
            return de.exec;
        }
        return null;
    }

    // for testing
    DefinedExecutable getDefinedExecutable(final OperationKey key) {
        return registry.get(key);
    }
}
