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
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.CougarStartingGate;
import com.betfair.cougar.core.api.GateListener;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.*;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.HttpCommandProcessor;
import com.betfair.cougar.transport.impl.AbstractCommandProcessor;
import com.betfair.cougar.transport.impl.CommandValidatorRegistry;
import com.betfair.cougar.util.stream.ByteCountingInputStream;
import com.betfair.cougar.util.stream.LimitedByteCountingInputStream;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides common functionality for Http CommandProcessors, mainly around
 * resolving the ExecutionContext from an HttpCommand, and registration as a
 * GateListener.
 *
 */
public abstract class AbstractHttpCommandProcessor<CredentialsContainer> extends
		AbstractCommandProcessor<HttpCommand> implements HttpCommandProcessor,
		GateListener {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractHttpCommandProcessor.class);

    private static Pattern VERSION_REMOVAL_PATTERN = Pattern.compile("(/?.*/v\\d+)(?:\\.\\d+)?(/.*)?", Pattern.CASE_INSENSITIVE);

    protected interface IdentityTokenIOAdapter {
        void rewriteIdentityTokens(List<IdentityToken> identityTokens);
        boolean isRewriteSupported();
    }

    private DehydratedExecutionContextResolution contextResolution;
    private Protocol protocol;



    private final String requestTimeoutHeader;



	private String name;

	private int priority = 1;

    //This is a map of serviceName-v<Major Service Version#> to ServiceBindingDescriptor
    //it is used to provide a list of all binding descriptors, but also to ensure that
    //there is only one implementation of a service registered for each major
    //Service version
	private Map<String, ServiceBindingDescriptor> serviceBindingDescriptors = new HashMap<String, ServiceBindingDescriptor>();

    private RequestLogger requestLogger;

    private ContentTypeNormaliser contentTypeNormaliser;

    private CommandValidatorRegistry<HttpCommand> validatorRegistry;

    protected boolean hardFailEnumDeserialisation;

    protected long maxPostBodyLength;

	/**
	 *
     */
	protected AbstractHttpCommandProcessor(Protocol protocol, DehydratedExecutionContextResolution contextResolution, String requestTimeoutHeader) {
        this.protocol = protocol;
        this.contextResolution = contextResolution;
        this.requestTimeoutHeader = requestTimeoutHeader;
	}

	/**
	 * By setting the starting gate property this CommandProcessor will register
	 * itself with the CougarStartingGate
	 *
	 * @param startingGate
	 *            the starting gate for the application
	 */
	public void setStartingGate(CougarStartingGate startingGate) {
		startingGate.registerStartingListener(this);
	}

    public void setRequestLogger(RequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }

    @ManagedAttribute
    public boolean isHardFailEnumDeserialisation() {
        return hardFailEnumDeserialisation;
    }

    public void setHardFailEnumDeserialisation(boolean hardFailEnumDeserialisation) {
        this.hardFailEnumDeserialisation = hardFailEnumDeserialisation;
    }

    @Override
    @ManagedAttribute
    public String getName() {
        return name;
    }

	public void setName(String name) {
		this.name = name;
	}

	@Override
    @ManagedAttribute
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Adds the binding descriptor to a list for binding later. The actual
	 * binding occurs onCougarStart, to be implemented by subclasses, when it
	 * can be guaranteed that all services have been registered with the EV.
	 */
	@Override
	public void bind(ServiceBindingDescriptor bindingDescriptor) {
        String servicePlusMajorVersion=bindingDescriptor.getServiceName() +
                "-v" + bindingDescriptor.getServiceVersion().getMajor();

        if (serviceBindingDescriptors.containsKey(servicePlusMajorVersion)) {
                throw new PanicInTheCougar("More than one version of service [" + bindingDescriptor.getServiceName() +
                        "] is attempting to be bound for the same major version. The clashing versions are [" +
                        serviceBindingDescriptors.get(servicePlusMajorVersion).getServiceVersion() + ", " +
                        bindingDescriptor.getServiceVersion() +
                        "] - only one instance of a service is permissable for each major version");
        }
		serviceBindingDescriptors.put(servicePlusMajorVersion, bindingDescriptor);
	}

    /**
     * Get the list of command validators to be used to validate commands.
     */
    @Override
    protected List<CommandValidator<HttpCommand>> getCommandValidators() {
        return validatorRegistry.getValidators();
    }

	/**
	 * Returns all the ServiceBindindDescriptors registered via the bind method.
	 *
	 * @return
	 */
	protected Iterable<ServiceBindingDescriptor> getServiceBindingDescriptors() {
		return serviceBindingDescriptors.values();
	}

    protected void logAccess(final HttpCommand command,
            final ExecutionContext context, final long bytesRead,
            final long bytesWritten, final MediaType requestMediaType,
            final MediaType responseMediaType, final ResponseCode responseCode) {

        command.getTimer().requestComplete();
        requestLogger.logAccess(command, context, bytesRead, bytesWritten, requestMediaType, responseMediaType,responseCode);
    }



    /**
     * Resolves an HttpCommand to an ExecutionContext for the error logging scenario. This will
     * never throw an exception although it might return null. The process is:
     * <li>If a non null context is passed,us it</li>
     * <li>Otherwise try and resolve a context from the commmand</li>
     * <li>If that fail, return null</li>
     * @param ctx
     *            the previously resolved context
     * @param command
     *            contains the HttpServletRequest from which the contextual
     *            information is derived
     * @return the ExecutionContext, populated with information from the
     *         HttpCommend
     */
    protected DehydratedExecutionContext resolveContextForErrorHandling(DehydratedExecutionContext ctx, HttpCommand command) {
        if (ctx != null) return ctx;
        try {
            return contextResolution.resolveExecutionContext(protocol, command, null);
        } catch (RuntimeException e) {
            // Well that failed too... nothing to do but return null
            LOGGER.debug("Failed to resolve error execution context", e);
            return null;
        }
    }


    protected TimeConstraints readRawTimeConstraints(HttpServletRequest request) {
        Long timeout = null;
        if (requestTimeoutHeader != null) {
            String timeoutString = request.getHeader(requestTimeoutHeader);
            try {
                timeout = Long.parseLong(timeoutString);
            } catch (NumberFormatException nfe) {
                // will default to null
            }
        }
        if (timeout == null) {
            return DefaultTimeConstraints.NO_CONSTRAINTS;
        }
        return DefaultTimeConstraints.fromTimeout(timeout);
    }


    /**
   	 * Resolves an HttpCommand to an ExecutionContext, which provides contextual
   	 * information to the ExecutionVenue that the command will be executed in.
   	 *
   	 * @param http
   	 *            contains the HttpServletRequest from which the contextual
   	 *            information is derived
   	 * @return the ExecutionContext, populated with information from the
   	 *         HttpCommend
   	 */
    protected DehydratedExecutionContext resolveExecutionContext(HttpCommand http, CredentialsContainer cc) {
        return contextResolution.resolveExecutionContext(protocol, http, cc);
    }

//    private DehydratedExecutionContext resolveExecutionContextWithTokensAndRequestTime(HttpCommand command, List<IdentityToken> tokens, boolean ignoreSubsequentWritesOfIdentity, Date requestTime) {
//            int keyLength = 0;
//           return ExecutionContextFactory.resolveExecutionContext(command, tokens, uuidHeader, uuidParentsHeader, geoLocationDeserializer, geoIPLocator, inferredCountry, keyLength, ignoreSubsequentWritesOfIdentity, requestTime);
//   	}

    /**
     * Rewrites the caller's credentials back into the HTTP response. The main use case for this is
     * rewriting SSO tokens, which may change and the client needs to know the new value.
     *
     * @param tokens - the identity tokens to marshall
     * @param ioAdapter - the adapter to detail with the transport specific IO requirements
     */
    public void writeIdentity(List<IdentityToken> tokens, IdentityTokenIOAdapter ioAdapter) {
        if (ioAdapter != null && ioAdapter.isRewriteSupported()) {
            ioAdapter.rewriteIdentityTokens(tokens);
        }
    }

    /**
     * If an exception is received while writing a response to the client, it might be
     * because the that client has closed their connection. If so, the problem should be ignored.
     */
    protected CougarException handleResponseWritingIOException(Exception e, Class resultClass) {
        String errorMessage = "Exception writing "+ resultClass.getCanonicalName() +" to http stream";
        IOException ioe = getIOException(e);
        if (ioe == null) {
            CougarException ce;
            if (e instanceof CougarException) {
                ce = (CougarException)e;
            } else {
                ce = new CougarFrameworkException(errorMessage, e);
            }
            return ce;
        }

		//We arrive here when the output pipe is broken. Broken network connections are not
		//really exceptional and should not be reported by dumping the stack trace.
		//Instead a summary debug level log message with some relevant info
        incrementIoErrorsEncountered();
		LOGGER.debug(
				"Failed to marshall object of class {} to the output channel. Exception ({}) message is: {}",
				resultClass.getCanonicalName(),
				e.getClass().getCanonicalName(),
				e.getMessage()
		);
		return new CougarServiceException(ServerFaultCode.OutputChannelClosedCantWrite, errorMessage, e);
	}

	private IOException getIOException(Throwable e) {
		Set<Throwable> seen = new HashSet<Throwable>();

		while (e != null && !seen.contains(e)) {
			if (e instanceof IOException) {
				return (IOException) e;
			}
			seen.add(e);
			e = e.getCause();
		}
		return null;
    }

    protected void closeStream(OutputStream out) {
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close output stream", e);
        }
    }

    protected String stripMinorVersionFromUri(String uri) {
        Matcher m = VERSION_REMOVAL_PATTERN.matcher(uri);
        if (m.matches()) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.group(1));

            String group2 = m.group(2);
            if (group2 != null && group2.length() > 0) {
                sb.append(m.group(2));
            }

            return sb.toString();
        } else {
            LOGGER.warn("Unable to remove minor version from URI: [" + uri + "], returning unmodified");
            return uri;
        }
    }

    protected ByteCountingInputStream createByteCountingInputStream(InputStream is) {
        if (maxPostBodyLength == 0) {
            return new ByteCountingInputStream(is);
        }
        else {
            return new LimitedByteCountingInputStream(is, maxPostBodyLength);
        }
    }

    public ContentTypeNormaliser getContentTypeNormaliser() {
        return contentTypeNormaliser;
    }

    public void setContentTypeNormaliser(ContentTypeNormaliser contentTypeNormaliser) {
        this.contentTypeNormaliser = contentTypeNormaliser;
    }

    public void setValidatorRegistry(CommandValidatorRegistry<HttpCommand> validatorRegistry) {
        this.validatorRegistry = validatorRegistry;
    }

    // for test usage only
    CommandValidatorRegistry<HttpCommand> getValidatorRegistry() {
        return validatorRegistry;
    }

    @ManagedAttribute
    public long getMaxPostBodyLength() {
        return maxPostBodyLength;
    }

    public void setMaxPostBodyLength(long maxPostBodyLength) {
        this.maxPostBodyLength = maxPostBodyLength;
    }
}
