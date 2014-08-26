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

package com.betfair.cougar.transport.jetty;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpInput;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;


public class MockJettyRequest extends org.eclipse.jetty.server.Request {
    private boolean handled;
	private final Map<String, String> overrides =  new HashMap<String, String>();

    public MockJettyRequest() {
        super(null, null);

		// Stick some defaults in
		overrides.put("Content-Type", "text/xml");
		overrides.put("Accept", "text/xml");

	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	@Override
	public String getContentType() {
		return getHeader("Content-Type");
	}


	@Override
	public String getMethod() {
		String result = overrides.get("Method");
		if (result == null) {
			result = super.getMethod();
		}
		return result;
	}

	@Override
	public String getRequestURI() {
		String result = overrides.get("RequestURI");
		if (result == null) {
			result = super.getRequestURI();
		}
		return result;	}

	public String getHeader(String name) {
		if (overrides.containsKey(name))
			return overrides.get(name);

		return null;
	}

    @Override
    public String getRemoteAddr() {
        return null;
    }

    public void setOverride(String name, String value) {
		overrides.put(name, value);
	}

	@Override
	public String getPathInfo() {
		return "/";
	}


}
