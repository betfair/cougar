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

package com.betfair.cougar.transport.jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.betfair.cougar.CougarVersion;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.ResponseCodeMapper;
import com.betfair.cougar.util.HeaderUtils;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class JettyHandler extends AbstractHandler {

	private final static CougarLogger logger = CougarLoggingUtils.getLogger(JettyHandler.class);
	private final TransportCommandProcessor<HttpCommand> commandProcessor;
    private final long MILLI=1000;
    private String protocolBindingRoot;
    private IdentityTokenResolverLookup identityTokenResolverLookup;

	private int timeoutInSeconds;
    private boolean suppressCommasInAccessLog;
    private static final String VERSION_HEADER = "Cougar 2 - "+CougarVersion.getVersion();

    public JettyHandler(final TransportCommandProcessor<HttpCommand> commandProcessor, boolean suppressCommasInAccessLog) {
        this(commandProcessor, null, null, suppressCommasInAccessLog);
    }


    public JettyHandler(final TransportCommandProcessor<HttpCommand> commandProcessor,
                        final JettyHandlerSpecification spec,
                        final IdentityTokenResolverLookup identityTokenResolverLookup,
                        boolean suppressCommasInAccessLog) {
		super();
		this.commandProcessor = commandProcessor;

        if (spec != null) {
            protocolBindingRoot = spec.getProtocolBindingUriPrefix();
        } else {
            protocolBindingRoot = "";
        }
        this.identityTokenResolverLookup = identityTokenResolverLookup;
        this.suppressCommasInAccessLog = suppressCommasInAccessLog;
    }

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader("Server", VERSION_HEADER);
		try {
            IdentityTokenResolver itr = null;
            if (identityTokenResolverLookup != null) {
                itr = identityTokenResolverLookup.lookupIdentityTokenResolver(baseRequest.getRequestURI());
            }
			JettyTransportCommand command = new JettyTransportCommand(request, response, itr);

			if (!command.getContinuation().isExpired()) {
				logger.log(Level.FINE, "Message Received at Jetty Handler for path %s", target);
				commandProcessor.process(command);
			} else {
				logger.log(Level.FINE, "Message Timeout at Jetty Handler for path %s", target);
				response.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			}
		} catch (CougarException ce) {
			logger.log(Level.WARNING, "Cougar Exception thrown processing request", ce);
			response.sendError(ResponseCodeMapper.getHttpResponseCode(ce.getResponseCode()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected Exception thrown processing request", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			// The request has been handled, whether or not the handling was a suspension.
			baseRequest.setHandled(true);
		}
	}

	public void setTimeoutInSeconds(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}

	protected class JettyTransportCommand implements HttpCommand, ContinuationListener {
        private static final String CERTIFICATE_ATTRIBUTE_NAME = "javax.servlet.request.X509Certificate";

        private String fullPath;
        private String operationPath;


		private final HttpServletRequest request;
		private final HttpServletResponse response;
        private final IdentityTokenResolver identityTokenResolver;
		private final Continuation continuation;
		private AtomicReference<CommandStatus> status;
		private RequestTimer timer = new RequestTimer();
        private X509Certificate[] clientX509CertificateChain;

		public JettyTransportCommand(final HttpServletRequest request, final HttpServletResponse response) {
            this(request, response, null);
        }

		public JettyTransportCommand(final HttpServletRequest request, final HttpServletResponse response, IdentityTokenResolver identityTokenResolver) {
			status = new AtomicReference<CommandStatus>(CommandStatus.InProcess);
			this.request = request;
			this.response = response;
            this.identityTokenResolver = identityTokenResolver;

            Object o = request.getAttribute(CERTIFICATE_ATTRIBUTE_NAME);
            if (o != null && o instanceof X509Certificate[]) {
                clientX509CertificateChain = (X509Certificate[])o;
            }

			HeaderUtils.setNoCache(response);
			continuation = ContinuationSupport.getContinuation(request);
			if (continuation.isInitial()) {
				continuation.setTimeout(MILLI * timeoutInSeconds );
				continuation.suspend(response);
				continuation.addContinuationListener(this);
			}

			buildPathInfo(request);
		}

        @Override
        public X509Certificate[] getClientX509CertificateChain() {
            return clientX509CertificateChain;
        }




		/**
		 * @see HttpCommand
		 */
		public Continuation getContinuation() {
			return continuation;
		}

		/**
		 * @see HttpCommand
		 */
		@Override
		public HttpServletRequest getRequest() {
			return request;
		}

		/**
		 * @see HttpCommand
		 */
		@Override
		public HttpServletResponse getResponse() {
			return response;
		}

        @Override
        public IdentityTokenResolver<?, ?, ?> getIdentityTokenResolver() {
            return identityTokenResolver;
        }

        /**
		 * @see HttpCommand
		 */
		@Override
		public void onComplete() {
			if (status.compareAndSet(CommandStatus.InProcess, CommandStatus.Complete)) {
				continuation.complete();
			}
		}

		/**
		 * @see HttpCommand
		 */
		@Override
		public CommandStatus getStatus() {
			return status.get();
		}

		/**
		 * @see ContinuationListener
		 */
		@Override
		public void onComplete(Continuation continuation) {
		}

		/**
		 * @see ContinuationListener
		 */
		@Override
		public void onTimeout(Continuation continuation) {
			status.set(CommandStatus.TimedOut);
		}

		@Override
		public RequestTimer getTimer() {
			return timer;
		}

		/**
		 * @see HttpCommand
		 */
		@Override
		public String getFullPath() {
            String ret = fullPath;
            // a comma can really mess with us since we use it as a delimiter in our logging..
            if (ret.contains(",")) {
                if (suppressCommasInAccessLog) {
                    ret = ret.replace(",","");
                }
                else {
                    ret = ret.replace(",","\\,");
                }
            }
	        return fullPath;
        }

		/**
		 * @see HttpCommand
		 */
		@Override
		public String getOperationPath() {
	        return operationPath;
        }
		/**
		 * getContextPath and getContextPath are defined in terms of servlets which we bypass completely.
		 * we may need to amend this implementation
         *
		 * @param request
		 * @return
		 */
        private final void buildPathInfo(final HttpServletRequest request) {
            fullPath = request.getContextPath() + (request.getPathInfo() ==  null ? "" : request.getPathInfo());

            // Strip the binding protocolBindingRoot off the front.
            if (protocolBindingRoot != null && protocolBindingRoot.length() > 0) {
                operationPath =  fullPath.substring(protocolBindingRoot.length());
            } else {
                operationPath =  fullPath;
            }
		}
	}

    public static interface IdentityTokenResolverLookup {
        IdentityTokenResolver lookupIdentityTokenResolver(String uri);
    }

    public static class SingletonIdentityTokenResolverLookup implements IdentityTokenResolverLookup {
        private IdentityTokenResolver identityTokenResolver;

        public SingletonIdentityTokenResolverLookup(IdentityTokenResolver identityTokenResolver) {
            this.identityTokenResolver = identityTokenResolver;
        }


        @Override
        public IdentityTokenResolver lookupIdentityTokenResolver(String uri) {
            return identityTokenResolver;
        }
    }

    public static class GeneralHttpIdentityTokenResolverLookup implements IdentityTokenResolverLookup {
        private Pattern regex;
        private Map<String, IdentityTokenResolver> serviceVersionToIdentityTokenResolverMap =
                new HashMap<String, IdentityTokenResolver>();

        public GeneralHttpIdentityTokenResolverLookup(String serviceContextRoot, JettyHandlerSpecification spec) {
            regex = Pattern.compile(serviceContextRoot + "/(v\\d+).*", Pattern.CASE_INSENSITIVE);

            for (Map.Entry<ServiceVersion, IdentityTokenResolver> entry : spec.getVersionToIdentityTokenResolverMap().entrySet()) {
                serviceVersionToIdentityTokenResolverMap.put("v" + entry.getKey().getMajor(), entry.getValue());
            }
        }

        public String extractVersion(String uri) {
            Matcher m = regex.matcher(uri);
            if (m.matches()) {
                return m.group(1).toLowerCase();
            }
            throw new CougarValidationException(ServerFaultCode.NoSuchService, "Uri [" + uri + "] did not contain a version");
        }


        @Override
        public IdentityTokenResolver lookupIdentityTokenResolver(String uri) {
            String version = extractVersion(uri);
            return serviceVersionToIdentityTokenResolverMap.containsKey(version) ? serviceVersionToIdentityTokenResolverMap.get(version) : null;
        }
    }
}
