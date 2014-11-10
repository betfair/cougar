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

import com.betfair.cougar.CougarVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlets.GzipFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class CrossOriginHandler extends AbstractHandler {

	private CrossOriginFilter corsFilter;
	private Handler wrappedHandler;


    private static final String VERSION_HEADER = "Cougar 2 - "+ CougarVersion.getVersion();

	public CrossOriginHandler(final String origins, final String methods, final String headers,
                              final Boolean allowCredentials, final String maxAge, Handler wrappedHandler)
            throws ServletException {

		this.wrappedHandler = wrappedHandler;
		this.corsFilter = new CrossOriginFilter();
		this.corsFilter.init(new FilterConfig() {

			@Override
			public String getFilterName() {
				return "gzipFilter";
			}

			@Override
			public String getInitParameter(String name) {

                switch(name) {
                    case CrossOriginFilter.ALLOWED_METHODS_PARAM:
                        return methods;
                    case CrossOriginFilter.ALLOWED_ORIGINS_PARAM:
                        return origins;
                    case CrossOriginFilter.ALLOWED_HEADERS_PARAM:
                        return headers;
                    case CrossOriginFilter.ALLOW_CREDENTIALS_PARAM:
                        return Boolean.toString(allowCredentials);
                    case CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM:
                        return maxAge;
                    case CrossOriginFilter.CHAIN_PREFLIGHT_PARAM:
                        return Boolean.toString(false);
                    default:
                        return null;
                }
			}

			@Override
			public Enumeration getInitParameterNames() {
				return null;
			}

			@Override
			public ServletContext getServletContext() {
				return new ContextHandler.NoContext();
			}
        });
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader("Server", CougarVersion.getVersionString());
        Chain chain = new Chain(target, baseRequest, wrappedHandler);
        corsFilter.doFilter(request, response, chain);
        ((Request) request).setHandled(true);
	}

	private static class Chain implements FilterChain {

		private Handler handler;
		private Request baseRequest;
		private String target;

		public Chain(String target, Request baseRequest, Handler wrappedHandler) {
			this.target = target;
			this.baseRequest = baseRequest;
			this.handler = wrappedHandler;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			handler.handle(target, baseRequest, (HttpServletRequest)request, (HttpServletResponse)response);
		}

	}
}
