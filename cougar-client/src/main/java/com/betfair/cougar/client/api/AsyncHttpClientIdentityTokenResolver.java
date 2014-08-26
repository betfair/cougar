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

package com.betfair.cougar.client.api;

import com.betfair.cougar.api.security.IdentityTokenResolver;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

import java.security.cert.X509Certificate;


/**
 * <code>AsyncHttpClientIdentityTokenResolver</code> describes contract
 * for IdentityTokenResolver which should be used for asynchronous http client.
 */
public interface AsyncHttpClientIdentityTokenResolver extends IdentityTokenResolver<Response, Request, X509Certificate[]> {
}
