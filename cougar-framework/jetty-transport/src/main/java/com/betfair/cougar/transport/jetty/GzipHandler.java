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

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlets.GzipFilter;

public class GzipHandler extends AbstractHandler {

	private GzipFilter gzipFilter ;
	private Handler wrappedHandler;

	public GzipHandler(final int bufferSize,
					   final int minGzipSize,
					   final String excludedAgents,
					   Handler wrappedHandler) throws ServletException {
		this.wrappedHandler = wrappedHandler;
		this.gzipFilter = new GzipFilter();
		this.gzipFilter.init(new FilterConfig() {

			@Override
			public String getFilterName() {
				return "gzipFilter";
			}

			@Override
			public String getInitParameter(String name) {
				if ("bufferSize".equals(name)) return Integer.toString(bufferSize);
				if ("minGzipSize".equals(name)) return Integer.toString(minGzipSize);
				if ("excludedAgents".equals(name)) return excludedAgents;
				return null;
			}

			@Override
			public Enumeration getInitParameterNames() {
				return null;
			}

			@Override
			public ServletContext getServletContext() {
				return new ContextHandler.NoContext();
			}});
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Chain chain = new Chain(target, baseRequest, wrappedHandler);
		gzipFilter.doFilter(request, response, chain);
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
