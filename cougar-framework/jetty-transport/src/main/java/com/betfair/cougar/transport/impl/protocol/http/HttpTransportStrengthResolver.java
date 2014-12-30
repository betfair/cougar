/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.SingleComponentResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.jetty.SSLRequestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Default HTTP resolver for transport strength. Delegates to SSLRequestUtils.getTransportSecurityStrengthFactor.
 */
public class HttpTransportStrengthResolver<Ignore> extends SingleComponentResolver<HttpCommand, Ignore> {
    private int unknownCipherKeyLength;

    public HttpTransportStrengthResolver(int unknownCipherKeyLength) {
        super(DehydratedExecutionContextComponent.TransportSecurityStrengthFactor);
        this.unknownCipherKeyLength = unknownCipherKeyLength;
    }

    @Override
    public void resolve(HttpCommand httpCommand, Ignore ignore, DehydratedExecutionContextBuilder builder) {
        int keyLength = 0;
        if (httpCommand.getRequest().getScheme().equals("https")) {
            keyLength = SSLRequestUtils.getTransportSecurityStrengthFactor(httpCommand.getRequest(), unknownCipherKeyLength);
        }
        builder.setTransportSecurityStrengthFactor(keyLength);
    }
}
