/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.server;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import com.betfair.cougar.test.socket.tester.common.SimpleCredential;
import com.betfair.cougar.test.socket.tester.common.SimplePrincipal;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.Executor;

/**
 *
 */
public class TestServerExecutionVenue implements ExecutionVenue {

    private final Map<OperationKey, Executable> executables = new HashMap<>();
    private final Map<OperationKey, OperationDefinition> opDefs = new HashMap<>();

    @Override
    public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder, long maxExecutionTime) {
        executables.put(def.getOperationKey(), executable);
        opDefs.put(def.getOperationKey(), def);
    }

    @Override
    public OperationDefinition getOperationDefinition(OperationKey key) {
        return opDefs.get(key);
    }

    @Override
    public Set<OperationKey> getOperationKeys() {
        return executables.keySet();
    }

    @Override
    public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, TimeConstraints timeConstraints) {
        ctx = resolve(ctx);
        executables.get(key).execute(ctx,key,args,observer,this,timeConstraints);
    }

    @Override
    public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, Executor executor, TimeConstraints timeConstraints) {
        ctx = resolve(ctx);
        executables.get(key).execute(ctx,key,args,observer,this,timeConstraints);
    }

    private ExecutionContext resolve(ExecutionContext ctx) {
        if (ctx instanceof DehydratedExecutionContext) {
            DehydratedExecutionContext dtx = (DehydratedExecutionContext) ctx;

            List<IdentityToken> tokens = ((DehydratedExecutionContext) ctx).getIdentityTokens();
            List<Identity> identities = new ArrayList<>();
            for (final IdentityToken t : tokens) {
                identities.add(new Identity() {
                    @Override
                    public Principal getPrincipal() {
                        return new SimplePrincipal("unknown");
                    }

                    @Override
                    public Credential getCredential() {
                        return new SimpleCredential(t.getName(), t.getValue());
                    }
                });
            }
            // ensure the identity chain set in the context is immutable
            dtx.setIdentityChain(new IdentityChainImpl(identities));
            return dtx;
        }
        return ctx;
    }

    @Override
    public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPostProcessors(List<ExecutionPostProcessor> preProcessorList) {
        throw new UnsupportedOperationException();
    }
}
