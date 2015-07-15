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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.security.SSLAwareTokenResolver;

/**
 *
 */
public abstract class ClientAuthExecutable implements Executable {
    private boolean needsClientAuth;

    protected ClientAuthExecutable(boolean needsClientAuth) {
        this.needsClientAuth = needsClientAuth;
    }

    protected boolean checkClientAuth(ExecutionContext ctx, ExecutionObserver observer) {
        if (needsClientAuth) {
            boolean found = false;
            for (Identity id : ctx.getIdentity().getIdentities()) {
                if (id.getCredential() != null && id.getCredential().getName().equals(SSLAwareTokenResolver.SSL_CERT_INFO)) {
                    found = id.getCredential().getValue() != null;
                    if (found) {
                        break;
                    }
                }
            }
            if (!found) {
                observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.SecurityException,"missing credentials")));
                return false;
            }
        }
        return true;
    }
}
