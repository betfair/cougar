/*
 * Copyright 2014, The Sporting Exchange Limited
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

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Provides common ExecutionVenue functionality.
 */
public class BaseExecutionVenue implements ExecutionVenue {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExecutionVenue.class);

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
        LOGGER.info("Registered operation: {}", key);
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
        if (ctx instanceof DehydratedExecutionContext) {
            DehydratedExecutionContext contextWithTokens = (DehydratedExecutionContext) ctx;
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
                        throw new CougarFrameworkException(sfc, "Credentials supplied were invalid", e);
                    }
                    throw new CougarFrameworkException(ServerFaultCode.SecurityException, "Credentials supplied were invalid", e);
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
    public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, ExecutionObserver observer, TimeConstraints timeConstraints) {
        final DefinedExecutable de = registry.get(key);
        if (de == null) {
            LOGGER.debug("Not request logging request to URI: {} as no operation was found", key.toString());
            observer.onResult(new ExecutionResult(new CougarFrameworkException(ServerFaultCode.NoSuchOperation, "Operation not found: "+key.toString())));
        } else {
            long serverExpiryTime = de.maxExecutionTime == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + de.maxExecutionTime;
            long clientExpiryTime = timeConstraints.getExpiryTime() == null ? Long.MAX_VALUE : timeConstraints.getExpiryTime();
            long expiryTime = Math.min(clientExpiryTime, serverExpiryTime);
            if (expiryTime == Long.MAX_VALUE) {
                expiryTime = 0;
            }
            if (!(observer instanceof ExpiringObserver)) {
                final ExpiringObserver expiringObserver = new ExpiringObserver(observer, expiryTime);
                if (expiringObserver.expires()) {
                    registerExpiringObserver(expiringObserver);
                }
                observer = expiringObserver;
            }
            observer = new ExecutionObserverWrapper(observer, de.recorder, key);

            try {
                ExecutionContext contextToUse = resolveIdentitiesIfRequired(ctx);
                de.exec.execute(contextToUse, key, args, observer, this, timeConstraints);
            } catch (CougarException e) {
                observer.onResult(new ExecutionResult(e));
            } catch (Exception e) {
                observer.onResult(new ExecutionResult(
                        new CougarFrameworkException(ServerFaultCode.ServiceRuntimeException,
                                "Exception thrown by service method",
                                e)));
            }
        }
        if (observer instanceof ExpiringObserver) {
            deregisterExpiringObserver((ExpiringObserver) observer);
        }
    }

    @Override
    public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, final ExecutionObserver observer, final Executor executor, final TimeConstraints timeConstraints) {
        if (timeConstraints == null) {
            throw new IllegalArgumentException("Time constraints may not be null");
        }

        final DefinedExecutable de = registry.get(key);
        final InterceptingExecutableWrapper interceptingExecutableWrapper = ExecutableWrapperUtils.findChild(InterceptingExecutableWrapper.class, de.exec);
        // this has to be a new list since the InterceptionUtils.execute call below is allowed to (expected to even) mutate the second list passed in
        final List<ExecutionPreProcessor> remainingProcessors = new ArrayList<>(preProcessorList);

        final Runnable execution = new Runnable() {
            @Override
            public void run() {
                long serverExpiryTime = de.maxExecutionTime == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + de.maxExecutionTime;
                long clientExpiryTimeCopy = timeConstraints.getExpiryTime() == null ? Long.MAX_VALUE : timeConstraints.getExpiryTime();
                long expiryTime = Math.min(clientExpiryTimeCopy, serverExpiryTime);
                if (expiryTime == Long.MAX_VALUE) {
                    expiryTime = 0;
                }
                final ExpiringObserver expiringObserver = new ExpiringObserver(observer, expiryTime);
                if (expiringObserver.expires()) {
                    registerExpiringObserver(expiringObserver);
                }
                final TimeConstraints timeConstraints = expiryTime == 0 ? DefaultTimeConstraints.NO_CONSTRAINTS : DefaultTimeConstraints.fromExpiryTime(expiryTime);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // this gets run on the executor thread, so will be visible in the wrapper when executed
                        if (interceptingExecutableWrapper != null) {
                            interceptingExecutableWrapper.setUnexecutedPreProcessorsForThisThread(remainingProcessors);
                        }
                        execute(ctx, key, args, expiringObserver, timeConstraints);
                    }
                });
            }
        };

        InterceptionUtils.execute(preProcessorList, remainingProcessors, ExecutionRequirement.PRE_QUEUE, execution, ctx, key, args, observer);
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
                observer.onResult(new ExecutionResult(new CougarFrameworkException(ServerFaultCode.Timeout, "Executable did not complete in time")));
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

    // for testing
    DefinedExecutable getDefinedExecutable(final OperationKey key) {
        return registry.get(key);
    }
}
