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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.TransportCommand.CommandStatus;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.util.ServletResponseFileStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.IOException;

/**
 * Command processor handles invalid service context path. Sends a 404 status and logs the request.
 */
@ManagedResource
public class ServiceNotFoundHttpCommandProcessor extends AbstractHttpCommandProcessor<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceNotFoundHttpCommandProcessor.class);

    public ServiceNotFoundHttpCommandProcessor(DehydratedExecutionContextResolution contextResolution, String requestTimeoutHeader) {
        super(Protocol.RESCRIPT, contextResolution, requestTimeoutHeader);
        setName("ServiceNotFoundHttpCommandProcessor");
        setPriority(0);
    }

	@Override
	protected CommandResolver<HttpCommand> createCommandResolver(
            HttpCommand command, Tracer tracer) {
		throw new CougarServiceException(ServerFaultCode.NoSuchService, "Service does not exist");
	}

	@Override
	protected void writeErrorResponse(HttpCommand command,
                                      DehydratedExecutionContext context, CougarException e, boolean traceStarted) {
        try {
            if (command.getStatus() == CommandStatus.InProgress) {
                try {
                    int bytesWritten = ServletResponseFileStreamer.getInstance().stream404ToResponse(command.getResponse());
                    logAccess(command, resolveContextForErrorHandling(context, command), 0, bytesWritten, null, null, ResponseCode.NotFound);
                } catch (IOException ex) {
                    LOGGER.error("Unable to write error response", ex);
                } finally {
                    command.onComplete();
                    // no attempt to stop tracing here since it will never have started due to the timing of the call to createCommandResolver()
                }
            }
        }
        finally {
            if (context != null && traceStarted) {
                tracer.end(context.getRequestUUID());
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
