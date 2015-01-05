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

/**
 * Jetty AbstractHandler acting as a request middleware to accept CORS requests and decorate the responses accordingly.
 * @see <a href="http://www.w3.org/TR/cors/">Cross-Origin Resource Sharing W3C Recommendation</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS>Mozilla HTTP access control</a>
 */
public class CrossOriginHandler extends AbstractHandler {

	private CrossOriginFilter crossOriginFilter;
    private ContinuationChain continuationChain = new ContinuationChain();

    /**
     * See {@link org.eclipse.jetty.servlets.CrossOriginFilter} for more information on these arguments.
     */
	public CrossOriginHandler(final String allowedOrigins, final String allowedMethods, final String allowedHeaders,
                              final String preflightMaxAge, final String allowCredentials, final String exposedHeaders)
            throws ServletException {

		this.crossOriginFilter = new CrossOriginFilter();
		this.crossOriginFilter.init(new FilterConfig() {

            @Override
            public String getFilterName() {
                return "crossOriginFilter";
            }

            @Override
            public String getInitParameter(String name) {
                if (CrossOriginFilter.ALLOWED_ORIGINS_PARAM.equals(name)) return allowedOrigins;
                if (CrossOriginFilter.ALLOWED_METHODS_PARAM.equals(name)) return allowedMethods;
                if (CrossOriginFilter.ALLOWED_HEADERS_PARAM.equals(name)) return allowedHeaders;
                if (CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM.equals(name)) return preflightMaxAge;
                if (CrossOriginFilter.ALLOW_CREDENTIALS_PARAM.equals(name)) return allowCredentials;
                if (CrossOriginFilter.EXPOSED_HEADERS_PARAM.equals(name)) return exposedHeaders;
                if (CrossOriginFilter.CHAIN_PREFLIGHT_PARAM.equals(name)) return "false";
                return null;
            }

            @Override
            public Enumeration getInitParameterNames() {
                return null;
            }

            @Override
            public ServletContext getServletContext() {
                return null;
            }
        });
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader("Server", "Cougar 2 - " + CougarVersion.getVersion());
        /*
         Mark the request as handled by default. Leave the responsibility to undo this statement (if necessary) to
         the request chain.
         This exists here to make sure the original {@link org.eclipse.jetty.servlets.CrossOriginFilter} is left
         pristine.
        */
        ((Request) request).setHandled(true);
        crossOriginFilter.doFilter(request, response, continuationChain);
	}

	public class ContinuationChain implements FilterChain {

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            // If the request chain got to here it is assumed it is not a pre flight request
            ((Request) request).setHandled(false);
		}
	}
}
