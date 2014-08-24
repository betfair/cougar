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

package com.betfair.cougar.baseline.security;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.security.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class GeneralIdentityResolver implements IdentityResolver {

    @Override
    public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
        if (ctx.getIdentityTokens() != null && ctx.getIdentityTokens().size() > 0) {
            for (final IdentityToken tk: ctx.getIdentityTokens()) {
                String tokenValue = tk.getValue();
                if (tokenValue.startsWith("INVALID")) {
                    // To run an invalid credentials test with a custom fault code, input should be of the form INVALID-CredentialFaultCode
                    if (tokenValue.contains("-") && tokenValue.length() > "INVALID-".length()) {
                        CredentialFaultCode credentialFaultCode = CredentialFaultCode.valueOf(tokenValue.substring(tokenValue.indexOf("-")+1));
                        throw new InvalidCredentialsException(tk.getName() + " is invalid", credentialFaultCode);
                    }
                    else {
                       throw new InvalidCredentialsException(tk.getName() + "is invalid");
                    }
                }
                chain.addIdentity(new Identity() {
                    @Override
                    public Principal getPrincipal() {
                        return new Principal() {
                            @Override
                            public String getName() {
                                return "PRINCIPAL: " + tk.getName();
                            }
                        };
                    }

                    @Override
                    public Credential getCredential() {
                        return new Credential() {
                            @Override
                            public String getName() {
                                return "CREDENTIAL: " + tk.getName();
                            }

                            @Override
                            public Object getValue() {
                                return tk.getValue();
                            }
                        };
                    }

                    public String toString() {
                        StringBuilder sb = new StringBuilder("Identity_");
                        sb.append("Principal=").append(getPrincipal().getName()).append("|");
                        sb.append("Credential=").append(getCredential().getName());
                        return sb.toString();
                    }
                });
            }
        }
    }

    @Override
    public List<IdentityToken> tokenise(IdentityChain chain) {
        if (chain != null) {
            List<IdentityToken> tokens = new ArrayList<IdentityToken>();
            for(Identity id: chain.getIdentities()) {
                tokens.add(new IdentityToken(id.getPrincipal().getName().substring(11), (String)id.getCredential().getValue()));
            }
            return tokens;
        }
        return null;
    }
}
