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

package com.betfair.cougar.transport.api.protocol.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.transport.api.TransportCommand;

import java.security.cert.X509Certificate;

/**
 * HTTP specific TransportCommand. Provides access to the underlying
 * HttpServletRequest and HttpServletResponse.
 *
 */
public interface HttpCommand<A, B, C> extends TransportCommand {

	/**
	 * Gets the underlying request for this command.
	 * @return
	 */
	public HttpServletRequest getRequest();

	/**
	 * Gets the underlying response for this command.
	 * @return
	 */
	public HttpServletResponse getResponse();

    /**
     * Returns the identity token resolver
     * @return
     */
    public IdentityTokenResolver<A, B, C> getIdentityTokenResolver();


    /**
     * Returns the full path for this command.
     * @return
     */
	public String getFullPath();

    /**
     * Returns the operation path for this command (the full path minus the context binding, if any)
     */
    public String getOperationPath();
}
