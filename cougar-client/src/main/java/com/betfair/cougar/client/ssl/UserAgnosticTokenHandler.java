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

package com.betfair.cougar.client.ssl;

import org.apache.http.client.UserTokenHandler;
import org.apache.http.protocol.HttpContext;

/**
 * Usually you will need this when you use SSL connections with client authentication (2-way SSL).
 * Connections to CoUGAR data services are stateless even if a certificate is used for client
 * authentication (the server which acts as a client is authenticated not the end-user).
 */
public class UserAgnosticTokenHandler implements UserTokenHandler {

    @Override
    public Object getUserToken(HttpContext context) {
        // that's all we need
        return null;
    }
}