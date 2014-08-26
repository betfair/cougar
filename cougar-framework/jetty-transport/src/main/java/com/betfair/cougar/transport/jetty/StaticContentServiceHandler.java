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
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.RequestTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.ExecutionContextFactory;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.ServletResponseFileStreamer;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@ManagedResource
public class StaticContentServiceHandler extends ContextHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentServiceHandler.class);

    private static final String VERSION_HEADER = "Cougar 2 - "+CougarVersion.getVersion();

    private static final String INFER_CONTENT_TYPE = "INFER";
    private static final String DEFAULT_CONTENT_TYPE = "text/html";


	private final Pattern IS_STATIC_CONTENT_PATH;
	private final String contextPath;
	private final String contentType;
    private final MediaType mediaType;

	private final AtomicLong numOK = new AtomicLong();
	private final AtomicLong numErrors = new AtomicLong();
	private final AtomicLong num404s = new AtomicLong();
    private final AtomicLong ioErrorsEncountered = new AtomicLong();

    private final String uuidHeader;
    private final String uuidParentsHeader;
    private final GeoLocationDeserializer deserializer;
    private final GeoIPLocator geoIPLocator;

    private int unknownCipherKeyLength;

    // if true then removes, otherwise escapes them with a backslash
    private boolean suppressCommasInAccessLog = true;

    private RequestLogger requestLogger;
    private static final String[][] CACHE_CONTROL_HEADER = {{"Cache-Control", "private, max-age=2592000"}};

    public StaticContentServiceHandler(
					String contextPath,
					String staticContentRegex,
					String contentType,
                    String uuidHeader,
                    String uuidParentsHeader,
                    GeoLocationDeserializer deserializer,
					GeoIPLocator geoIPLocator,
                    RequestLogger requestLogger,
                    boolean suppressCommasInAccessLog) {
		this.contextPath = contextPath;
		this.contentType = contentType;
        this.uuidHeader = uuidHeader;
        this.uuidParentsHeader = uuidParentsHeader;
        this.deserializer = deserializer;
        this.geoIPLocator = geoIPLocator;
        this.requestLogger = requestLogger;

        mediaType = contentType.equals(INFER_CONTENT_TYPE) ? null : MediaType.valueOf(contentType);
		IS_STATIC_CONTENT_PATH = Pattern.compile(staticContentRegex, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		LOGGER.debug("Static content stream handler for context path {} invoked for request path {}", getContextPath(), target);
		baseRequest.setHandled(true);
        final RequestTimer timer = new RequestTimer();
        response.setHeader("Server", VERSION_HEADER);

        long bytesWritten = 0;
        ResponseCode responseCode = ResponseCode.Ok;
		if (IS_STATIC_CONTENT_PATH.matcher(target).matches()) {
			try {
				InputStream rawStream = getClass().getResourceAsStream(target);
				if (rawStream != null) {
                    LOGGER.debug("Static content stream found for path {}", target);
                    bytesWritten = ServletResponseFileStreamer.getInstance().streamFileToResponse(rawStream, response,
                            HttpServletResponse.SC_OK, getContentType(contentType, target), CACHE_CONTROL_HEADER );
					numOK.incrementAndGet();
				}
				else {
					LOGGER.debug("Static content stream not found for path {}", target);
                    responseCode = ResponseCode.NotFound;
					bytesWritten = ServletResponseFileStreamer.getInstance().stream404ToResponse(response);
					num404s.incrementAndGet();
				}
            } catch (IOException e) {
                // Assume that the the output pipe is broken. Broken network connections are not
                // really exceptional and should not be reported by dumping the stack trace.
                // Instead a summary debug level log message with some relevant info
                ioErrorsEncountered.incrementAndGet();
                LOGGER.debug("Failed to marshall static data to the output channel.", e);
			} catch (Exception e) {
				LOGGER.error("Unexpected Exception thrown processing WSDL request", e);
                responseCode = ResponseCode.InternalError;
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				numErrors.incrementAndGet();
			}
		} else {
			LOGGER.debug("Static content stream did not match regex for path {}", target);
            responseCode = ResponseCode.NotFound;
            bytesWritten = ServletResponseFileStreamer.getInstance().stream404ToResponse(response);
			num404s.incrementAndGet();
		}
        logAccess(request, response, bytesWritten, responseCode, timer);
	}

    private String getContentType(String contentType, String target) {
        if (contentType.equals(INFER_CONTENT_TYPE)) {
            return null; // let the browser do the work...
        } else {
            return contentType;
        }
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long bytesWritten, ResponseCode responseCode, RequestTimer timer) {
        HttpCommand cmd = getHttpCommand(request, response, timer);
        int keyLength = 0;
        if (request.getScheme().equals("https")) {
            keyLength = SSLRequestUtils.getTransportSecurityStrengthFactor(request, unknownCipherKeyLength);
        }

        ExecutionContext ctx = ExecutionContextFactory.resolveExecutionContext(cmd, null, uuidHeader, uuidParentsHeader, deserializer, geoIPLocator, null, keyLength, false, new Date());
        requestLogger.logAccess(cmd, ctx, 0, bytesWritten, null, mediaType, responseCode);
    }

    private HttpCommand getHttpCommand(final HttpServletRequest request, final HttpServletResponse response, RequestTimer timer) {
        return new StaticHttpCommand(request, response, timer, suppressCommasInAccessLog);
    }

    @ManagedAttribute
	@Override
	public String getContextPath() {
		return contextPath;
	}

	@ManagedAttribute
	public long getNumOKRequests() {
		return numOK.longValue();
	}

	@ManagedAttribute
	public long getNumErrors() {
		return numErrors.longValue();
	}

	@ManagedAttribute
	public long getNumNotFound() {
		return num404s.longValue();
	}

    @ManagedAttribute
   	public String getContentType() {
   		return contentType;
   	}

    private static class StaticHttpCommand implements HttpCommand {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final RequestTimer timer;
        private final boolean suppressCommasInAccessLog;

        private StaticHttpCommand(HttpServletRequest request, HttpServletResponse response, RequestTimer timer, boolean suppressCommasInAccessLog) {
            this.request = request;
            this.response = response;
            this.timer = timer;
            this.suppressCommasInAccessLog = suppressCommasInAccessLog;
            timer.requestComplete();
        }

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }

        @Override
        public IdentityTokenResolver<?, ?, ?> getIdentityTokenResolver() {
            return null;
        }

        @Override
        public String getFullPath() {
            String pathInfo = (request.getPathInfo() ==  null ? "" : request.getPathInfo());
            // a comma can really mess with us since we use it as a delimiter in our logging..
            if (pathInfo.contains(",")) {
                if (suppressCommasInAccessLog) {
                    pathInfo = pathInfo.replace(",","");
                }
                else {
                    pathInfo = pathInfo.replace(",","\\,");
                }
            }
            return request.getContextPath() + pathInfo;
        }

        @Override
        public String getOperationPath() {
            return null; // No operation here
        }

        @Override
        public void onComplete() {
            // Errr, nope.
        }

        @Override
        public CommandStatus getStatus() {
            return CommandStatus.InProgress;
        }

        @Override
        public RequestTimer getTimer() {
            return timer;
        }

    }

    public void setUnknownCipherKeyLength(int unknownCipherKeyLength) {
        this.unknownCipherKeyLength = unknownCipherKeyLength;
    }

    @ManagedAttribute
    public int getUnknownCipherKeyLength() {
        return unknownCipherKeyLength;
    }

    @ManagedAttribute
    public boolean isSuppressCommasInAccessLog() {
        return suppressCommasInAccessLog;
    }

    @ManagedAttribute
    public boolean isEscapeCommasInAccessLog() {
        return !suppressCommasInAccessLog;
    }
}
