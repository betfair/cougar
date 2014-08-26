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

package com.betfair.cougar.core.impl.ev;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.impl.logging.RequestLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the operation key to a service Executable. The resolver will resolve the executable
 * from the resolvers that are registered with it, then return an Executable that ensures the wrapped
 * executable receives a RequestContext, enabling event logging and other server-side functionality
 * not available through the ExecutionContext interface.
 *
 */
public class ServiceExecutableResolver extends CompoundExecutableResolverImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutableResolver.class);

	private EventLogger eventLogger;


	public void setEventLogger(EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	@Override
	public Executable resolveExecutable(OperationKey operationKey, ExecutionVenue ev) {
        if (!(ev instanceof ServiceRegisterableExecutionVenue)) {
            throw new IllegalStateException("I only support resolution from a service registerable EV");
        }
        ServiceRegisterableExecutionVenue srev = (ServiceRegisterableExecutionVenue) ev;
        ServiceLogManager manager = srev.getServiceLogManager(operationKey.getNamespace(), operationKey.getServiceName(), operationKey.getVersion());
		Executable executable = super.resolveExecutable(operationKey, ev);
		if (executable != null) {
			return new RequestContextExecutable(executable, manager, srev.getTracer());
		}
		return null;
	}

	private class RequestContextExecutable implements ExecutableWrapper {
		private final Executable executable;
		private final ServiceLogManager manager;
        private Tracer tracer;

        public RequestContextExecutable(Executable executable, ServiceLogManager manager, Tracer tracer) {
			this.executable = executable;
			this.manager = manager;
            this.tracer = tracer;
        }

        @Override
        public void execute(final ExecutionContext ctx,
                            final OperationKey key,
                            final Object[] args,
                            final ExecutionObserver observer,
                            final ExecutionVenue executionVenue,
                            final TimeConstraints timeConstraints) {
            final ExecutionContextAdapter ctxAdapter = new ExecutionContextAdapter(key, ctx, observer, manager, tracer);
            try {
                executable.execute(ctxAdapter, key, args, ctxAdapter, executionVenue, timeConstraints);
            } catch (CougarException e) {
                ctxAdapter.onResult(new ExecutionResult(e));
            } catch (Exception e) {
                ctxAdapter.onResult(new ExecutionResult(
                        new CougarServiceException(ServerFaultCode.ServiceRuntimeException,
                                "Exception thrown by service method",
                                e)));
            }
        }

        @Override
        public Executable getWrappedExecutable() {
            return executable;
        }

        @Override
        public <T extends Executable> T findChild(Class<T> clazz) {
            return ExecutableWrapperUtils.findChild(clazz, this);
        }
    }

	private class ExecutionContextAdapter implements RequestContext, ExecutionObserver {

		private final ExecutionObserver observer;
		private final OperationKey key;
		private final ExecutionContext original;
		private final ServiceLogManager manager;

		private RequestTimer timer = new RequestTimer();
		private List<LoggableEvent> loggableEvents = new ArrayList<LoggableEvent>();
		private LogExtension logExtension;
        private LogExtension connectedObjectLogExtension;
		private AtomicBoolean complete = new AtomicBoolean(false);
        private RequestContext originalRequestContext;
        private Tracer tracer;


        public ExecutionContextAdapter(final OperationKey key, final ExecutionContext original, final ExecutionObserver observer, ServiceLogManager manager, Tracer tracer) {
			this.key = key;
			this.original = original;
            this.tracer = tracer;
            if (original instanceof RequestContext) {
			    this.originalRequestContext = (RequestContext) original;
            }
			this.observer = observer;
			this.manager = manager;
		}

        @Override
        public void onResult(ExecutionResult result) {
            if (key.getType() == OperationKey.Type.Request && !complete.getAndSet(true)) {
                timer.requestComplete();
                switch (result.getResultType()) {
                    case Fault:
                    case Success:
                        logEvents(result);
                        break;
                }
            }
            if (key.getType() == OperationKey.Type.ConnectedObject && !complete.getAndSet(true)) {
                timer.requestComplete();
                validateConnectedObjectLogExtension(result);

            }
            observer.onResult(result);
        }

        private void validateConnectedObjectLogExtension(ExecutionResult executionResult) {
            // need to validate the connected object extension, even though the logging's not done here but in the transport. a bit yucky
            if (connectedObjectLogExtension == null) {
                if (manager.getConnectedObjectLogExtensionClass() != null) {
                    if (executionResult.getResult() instanceof ConnectedResponse) {
                        throw new CougarFrameworkException("Connected object log extension expected but not found for " + key);
                    }
                }
            }
            else {
                if (manager.getNumConnectedObjectLogExtensionFields() != connectedObjectLogExtension.getFieldsToLog().length) {
                    throw new CougarFrameworkException("Connected object log extension class defined "+
                            connectedObjectLogExtension.getFieldsToLog().length+" fields. Expected" + manager.getNumConnectedObjectLogExtensionFields());
                }
            }
        }

        @Override
		public GeoLocationDetails getLocation() {
			return original.getLocation();
		}

		@Override
		public Date getReceivedTime() {
			return original.getReceivedTime();
		}

        @Override
        public Date getRequestTime() {
            return original.getRequestTime();
        }

        @Override
		public RequestUUID getRequestUUID() {
			return original.getRequestUUID();
		}

        @Override
        public boolean traceLoggingEnabled() {
            return original.traceLoggingEnabled();
        }

        @Override
        public int getTransportSecurityStrengthFactor() {
            return original.getTransportSecurityStrengthFactor();
        }

        @Override
        public boolean isTransportSecure() {
            return original.isTransportSecure();
        }

        @Override
		public void addEventLogRecord(LoggableEvent record) {
            if (originalRequestContext != null) {
                originalRequestContext.addEventLogRecord(record);
            }
			loggableEvents.add(record);
		}

		@Override
		public void setRequestLogExtension(LogExtension extension) {
            if (originalRequestContext != null) {
                originalRequestContext.setRequestLogExtension(extension);
            }
			this.logExtension = extension;
		}

        @Override
        public void setConnectedObjectLogExtension(LogExtension extension) {
            if (originalRequestContext != null) {
                originalRequestContext.setConnectedObjectLogExtension(extension);
            }
            this.connectedObjectLogExtension = extension;
        }

        public LogExtension getConnectedObjectLogExtension() {
            if (connectedObjectLogExtension != null) {
                return connectedObjectLogExtension;
            }
            if (originalRequestContext != null) {
                return originalRequestContext.getConnectedObjectLogExtension();
            }
            return null;
        }

        @Override
		public void trace(String msg, Object... args) {
            tracer.trace(this, msg, args);
		}

		@Override
		public IdentityChain getIdentity() {
			return original.getIdentity();
		}

        public String toString() {
            StringBuilder sb = new StringBuilder("ExecutionContextAdaptor:");

            sb.append("geoLocationDetails=").append(getLocation()).append("|");
            sb.append("identityChain=").append(getIdentity()).append("|");
            sb.append("requestUUID=").append(getRequestUUID()).append("|");
            sb.append("receivedTime=").append(getReceivedTime()).append("|");
            sb.append("traceLoggingEnabled=").append(traceLoggingEnabled()).append("|");
            sb.append("requestLogExtension=").append(logExtension);
            sb.append("connectedObjectLogExtension=").append(connectedObjectLogExtension);

            return sb.toString();
        }

		private void logEvents(ExecutionResult executionResult) {
			String faultCode = "";
			if (executionResult.isFault() && executionResult.getFault().getFault() != null) {
				faultCode = executionResult.getFault().getFault().getErrorCode();
			}
            // now for the request logging
			Object [] fieldsToLog = null;
			if (logExtension == null) {
				if (manager.getLogExtensionClass() != null) {
					if (executionResult.isFault()) {
                        // As it was a fault, the service cannot have guaranteed to add the extension
                        // record, so we dummy one up with the correct number of fields
						fieldsToLog = new Object[manager.getNumLogExtensionFields()];
					} else {
						throw new CougarFrameworkException("Log extension expected but not found for " + key);
					}
				}
			} else {
				if (manager.getNumLogExtensionFields() != logExtension.getFieldsToLog().length) {
					throw new CougarFrameworkException("Log extension class defined "+
							logExtension.getFieldsToLog().length+" fields. Expected" + manager.getNumLogExtensionFields());
				}
				fieldsToLog = logExtension.getFieldsToLog();
			}
			RequestLogEvent operationEvent = new RequestLogEvent(
					manager.getLoggerName(),
					faultCode,
					timer.getReceivedTime(),
					key,
					original.getRequestUUID(),
					timer.getProcessTimeNanos());
			eventLogger.logEvent(operationEvent, fieldsToLog);
			for (LoggableEvent event : loggableEvents) {
				eventLogger.logEvent(event, null);
			}
		}
	}

}
