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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.security.InferredCountryResolver;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.RequestTimeResolver;
import com.betfair.cougar.transport.api.TransportCommand.CommandStatus;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.util.ServletResponseFileStreamer;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Command processor handles invalid service context path. Sends a 404 status and logs the request.
 *
 */
@ManagedResource

public class ServiceNotFoundHttpCommandProcessor extends AbstractHttpCommandProcessor {
    public static final String FILE_NOT_FOUND_PAGE = "/errorpages/404.html";

    private static final CougarLogger logger = CougarLoggingUtils.getLogger(ServiceNotFoundHttpCommandProcessor.class);


	public ServiceNotFoundHttpCommandProcessor(GeoIPLocator geoIPLocator,
                                               GeoLocationDeserializer deserializer, String uuidHeader, String requestTimeoutHeader, RequestTimeResolver requestTimeResolver) {
		this(geoIPLocator, deserializer, uuidHeader, requestTimeoutHeader, requestTimeResolver, null);
	}

    public ServiceNotFoundHttpCommandProcessor(GeoIPLocator geoIPLocator,
			GeoLocationDeserializer deserializer, String uuidHeader, String requestTimeoutHeader, RequestTimeResolver requestTimeResolver, InferredCountryResolver<HttpServletRequest> resolver) {
		super(geoIPLocator, deserializer, uuidHeader, requestTimeoutHeader, requestTimeResolver, resolver);
		setName("ServiceNotFoundHttpCommandProcessor");
		setPriority(0);
	}

	@Override
	protected CommandResolver<HttpCommand> createCommandResolver(
			HttpCommand command) {
		throw new CougarServiceException(ServerFaultCode.NoSuchService, "Service does not exist");
	}

	@Override
	protected void writeErrorResponse(HttpCommand command,
			ExecutionContextWithTokens context, CougarException e) {
		if (command.getStatus() == CommandStatus.InProcess) {
            try {
                int bytesWritten = ServletResponseFileStreamer.getInstance().stream404ToResponse(command.getResponse());
                logAccess(command, resolveContextForErrorHandling(context, command), 0, bytesWritten, null, null, ResponseCode.NotFound);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unable to write error response", ex);
            } finally {
                command.onComplete();
            }
		}
	}

	@Override
	public void bind(ServiceBindingDescriptor operation) {
		
	}

	@Override
	public void onCougarStart() {
		
	}

}
