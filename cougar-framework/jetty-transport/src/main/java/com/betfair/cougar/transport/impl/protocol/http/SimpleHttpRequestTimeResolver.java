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

import com.betfair.cougar.transport.impl.SimpleRequestTimeResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 *
 */
public class SimpleHttpRequestTimeResolver extends SimpleRequestTimeResolver<HttpServletRequest> {

    private String requestTimeHeader;

    public SimpleHttpRequestTimeResolver(String requestTimeHeader, boolean clientTimeSynchronizedWithServer) {
        super(clientTimeSynchronizedWithServer);
        this.requestTimeHeader = requestTimeHeader;
    }

    @Override
    protected Date readRequestTime(HttpServletRequest input) {
        Long time = null;
        if (requestTimeHeader != null && input.getHeader(requestTimeHeader) != null) {
            try {
                time = Long.parseLong(input.getHeader(requestTimeHeader));
            }
            catch (NumberFormatException nfe) {
                // defaults to null
            }
        }
        if (time != null) {
            return new Date(time);
        }
        return null;
    }
}
