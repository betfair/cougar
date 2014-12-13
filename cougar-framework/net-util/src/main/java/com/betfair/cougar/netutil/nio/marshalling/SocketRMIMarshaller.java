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

package com.betfair.cougar.netutil.nio.marshalling;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.TranscriptionException;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.core.impl.security.CertInfoExtractor;
import com.betfair.cougar.core.impl.security.CommonNameCertInfoExtractor;
import com.betfair.cougar.core.impl.security.SSLAwareTokenResolver;
import com.betfair.cougar.marshalling.api.socket.RemotableMethodInvocationMarshaller;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.api.protocol.socket.InvocationRequest;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Concrete implementation of @See RemotableMethodInvocationMarshaller class to
 * transcribe requests/responses over the the binary socket transport
 */
public class SocketRMIMarshaller implements RemotableMethodInvocationMarshaller {

    private final IdentityTokenResolver<CougarObjectInput, CougarObjectOutput, X509Certificate[]> identityTokenResolver;
    private boolean hardFailEnumDeserialisation;
    private DehydratedExecutionContextResolution contextResolution;

    // For client side, as the GeoIpLocator/cert regex & request time resolver is only necessary server side.
    public SocketRMIMarshaller() {
    	this(new CommonNameCertInfoExtractor(), null);
    }

    public SocketRMIMarshaller(CertInfoExtractor certInfoExtractor, DehydratedExecutionContextResolution contextResolution) {
        this.identityTokenResolver = new SSLAwareTokenResolver<CougarObjectInput, CougarObjectOutput, X509Certificate[]>(certInfoExtractor) {
                @Override
                public List<IdentityToken> resolve(CougarObjectInput input, X509Certificate[] certificateChain) {
                    List<IdentityToken> tokens = new ArrayList<IdentityToken>();
                    try {
                        attachCertInfo(tokens, certificateChain);
                    } catch (NamingException e) {
                        throw new CougarFrameworkException("Unable to resolve cert info", e);
                    }
                    try {
                        int size = input.readInt();
                        for (int i = 0; i < size; i++) {
                            tokens.add(new IdentityToken(input.readString(), input.readString()));
                        }
                        return tokens;
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Problem resolving IdentityTokens from CougarObjectInput...", e);
                    }
                }
                @Override
                public void rewrite(List<IdentityToken> tokens, CougarObjectOutput output) {
                    try {
                        if (tokens == null) {
                            output.writeInt(0);
                            return;
                        }
                        output.writeInt(tokens.size());
                        for (IdentityToken token : tokens) {
                            output.writeString(token.getName());
                            output.writeString(token.getValue());
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Problem transcribing IdentityTokens to CougarObjectOutput...", e);
                    }
                }
                @Override
                public boolean isRewriteSupported() {
                    return true;
                }
            };
        this.contextResolution = contextResolution;
    }

    public static class InvocationResponseImpl implements InvocationResponse {
        private final Object result;
        private final CougarException exception;

        public InvocationResponseImpl(final Object result) {
            this(result, null);
        }

        public InvocationResponseImpl(final Object result, final CougarException exception) {
            this.result = result;
            this.exception = exception;
        }

        public void recreate(ExecutionObserver observer, ParameterType returnType, long size) {
            if (isSuccess()) {
                if (returnType.getImplementationClass().equals(EnumWrapper.class)) {
                    observer.onResult(new ExecutionResult(new EnumWrapper(returnType.getComponentTypes()[0].getImplementationClass(), (String)result)));
                }
                else {
                    observer.onResult(new ExecutionResult(result));
                }
            } else {
                observer.onResult(new ExecutionResult(exception));
            }
        }

        public boolean isSuccess() {
            return exception == null;
        }

        public Object getResult() {
            return result;
        }

        public CougarException getException() {
            return exception;
        }
    }

    @Override
	public void writeInvocationRequest(InvocationRequest request, CougarObjectOutput out, IdentityResolver identityResolver, Map<String,String> additionalParams, byte protocolVersion) throws IOException {
        // todo: decide if we want app protocol versioning too?
        // note that new additions to the app protocol must be backwards compatible from the client side, the server side response may be breaking since it knows what
        // the client version is..
//        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
//            out.writeBytes(new byte[] {CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS});
//        }
		writeExecutionContext(request.getExecutionContext(), out, identityResolver, additionalParams, protocolVersion);
		writeOperationKey(request.getOperationKey(), out);
		writeArgs(request.getParameters(), request.getArgs(), out);
        writeTimeConstraints(request.getTimeConstraints(), out, protocolVersion);
	}

    @Override
	public void writeInvocationResponse(InvocationResponse response, CougarObjectOutput out, byte protocolVersion) throws IOException {
		out.writeBoolean(response.isSuccess());
		if (response.isSuccess()) {
            // make sure we serialise enum responses as strings.. unless of course we're on an old version
            if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS && response.getResult() != null && Enum.class.isAssignableFrom(response.getResult().getClass())) {
                out.writeObject(((Enum)response.getResult()).name());
            }
            else {
			    out.writeObject(response.getResult());
            }
		} else {
			out.writeObject(response.getException().getServerFaultCode());
			if (response.getException().getFault() != null) {
				out.writeObject(response.getException().getFault().getDetail());
			} else {
				out.writeObject(null);
			}
		}
	}

    @Override
	public InvocationResponse readInvocationResponse(ParameterType resultType, CougarObjectInput in) throws IOException {
        EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
		try {
			boolean success = in.readBoolean();
			if (success) {
	            //A void return type is still marked by a value (which signifies null) on the wire, see
	            //TranscriptionStreamFactoryImpl for details
				Object result = in.readObject();
				return new InvocationResponseImpl(result);
			} else {
				ServerFaultCode code = (ServerFaultCode)in.readObject();
				FaultDetail faultDetail = (FaultDetail) in.readObject();
				if (faultDetail != null) {
	                if (faultDetail.getCause() != null) {
	                    if (faultDetail.getCause() instanceof CougarApplicationException) {
	                        return new InvocationResponseImpl(null, new CougarClientException(code, faultDetail.getDetailMessage(), (CougarApplicationException)faultDetail.getCause()));
	                    } else {
	                        return new InvocationResponseImpl(null, new CougarClientException(code, faultDetail.getDetailMessage(), faultDetail.getCause()));
	                    }
	                }
	                else {
                        FaultCode faultCode = code == ServerFaultCode.ServiceCheckedException ? FaultCode.Server : code.getResponseCode().getFaultCode();
	                    return new InvocationResponseImpl(null, new CougarClientException(code, faultCode + " fault received from remote server: "+code,
                                new CougarClientException(code, faultDetail.getDetailMessage())
                        ));
	                }
				}
	            else {
				    return new InvocationResponseImpl(null, new CougarClientException(code, "No detailed message available"));
	            }
			}
		}
		catch (ClassNotFoundException e) {
			throw new TranscriptionException(e);
		}
		catch (IOException e) {
			throw new TranscriptionException(e);
		}
	}

	private void writeOperationKey(OperationKey operationKey, CougarObjectOutput out) throws IOException {
		out.writeInt(operationKey.getVersion().getMajor());
		out.writeInt(operationKey.getVersion().getMinor());
		out.writeString(operationKey.getServiceName());
		out.writeString(operationKey.getOperationName());
	}

	public OperationKey readOperationKey(CougarObjectInput in) throws IOException {
        EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
		return new OperationKey(
				new ServiceVersion(in.readInt(), in.readInt()),
				in.readString(),
				in.readString());
	}

	void writeArgs(Parameter [] argTypes, Object [] args, CougarObjectOutput out) throws IOException {
		out.writeInt(argTypes.length);
		for (int i=0;i<argTypes.length; i++) {
			out.writeString(argTypes[i].getName());
		}
		out.writeObject(args);
	}

    void writeTimeConstraints(TimeConstraints timeConstraints, CougarObjectOutput out, byte protocolVersion) throws IOException {
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
            boolean haveTimeConstraints = timeConstraints.getTimeRemaining() != null;
            out.writeBoolean(haveTimeConstraints);
            if (haveTimeConstraints) {
                out.writeLong(timeConstraints.getTimeRemaining());
            }
        }
    }

	public Object [] readArgs(Parameter [] argTypes, CougarObjectInput in) throws IOException {
        EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
		try {
			int numArgs = in.readInt();
			String[] paramNames = new String[numArgs];
			for (int i=0;i<numArgs;i++) {
				paramNames[i] = in.readString();
			}
			return ArgumentMatcher.getArgumentValues(argTypes, paramNames, (Object[])in.readObject());
		}
		catch (IOException e) {
			throw new TranscriptionException(e);
		}
		catch (ClassNotFoundException e) {
			throw new TranscriptionException(e);
		}
	}

	private void writeExecutionContext(ExecutionContext ctx, CougarObjectOutput out, IdentityResolver identityResolver, Map<String,String> additionalParams, byte protocolVersion) throws IOException {
		writeGeoLocation(ctx.getLocation(), out, protocolVersion);
		writeIdentity(ctx.getIdentity(), out, identityResolver);
        writeRequestUUID(ctx.getRequestUUID(), out, protocolVersion);
        writeReceivedTime(ctx.getReceivedTime(), out);
        out.writeBoolean(ctx.traceLoggingEnabled());
        writeRequestTime(out, protocolVersion);
        writeAdditionalParams(additionalParams, out, protocolVersion);
    }

    void writeAdditionalParams(Map<String,String> additionalParams, CougarObjectOutput out, byte protocolVersion) throws IOException {
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
            // when we have some additional params, this will send num keys and then key followed by value for each (as strings)
            if (additionalParams != null) {
                out.writeInt(additionalParams.size());
                for (String key : additionalParams.keySet()) {
                    out.writeString(key);
                    out.writeString(additionalParams.get(key));
                }
            }
            else {
                out.writeInt(0);
            }
        }
    }

    void writeRequestTime(CougarObjectOutput out, byte protocolVersion) throws IOException {
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
            out.writeLong(System.currentTimeMillis());
        }
    }

    private DehydratedExecutionContext resolveExecutionContext(CougarObjectInput in, GeoLocationParameters geo, List<IdentityToken> tokens, int transportSecurityStrengthFactor, byte protocolVersion) throws IOException {


        final String uuid = readRequestUuidString(in);
        final Date receivedTime = readReceivedTime(in);

        final boolean traceEnabled = in.readBoolean();

        final Long requestTime = protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS ? in.readLong() : System.currentTimeMillis();

        Map<String,String> additionalParams = new HashMap<>();
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
            int numExtraParams = in.readInt();
            for (int i=0; i<numExtraParams; i++) {
                additionalParams.put(in.readString(),in.readString());
            }
        }

        SocketContextResolutionParams params = new SocketContextResolutionParams(tokens, uuid, geo, receivedTime, traceEnabled, transportSecurityStrengthFactor, requestTime, additionalParams);

        return contextResolution.resolveExecutionContext(Protocol.SOCKET, params, null);

    }

    @Override
    public DehydratedExecutionContext readExecutionContext(CougarObjectInput in, String remoteAddress, X509Certificate[] clientCertChain, int transportSecurityStrengthFactor, byte protocolVersion) throws IOException {
        EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
        // this has to be first as the protocol requires it
        final GeoLocationParameters geo = readGeoLocation(in, remoteAddress, protocolVersion);

        List<IdentityToken> tokens = identityTokenResolver.resolve(in, clientCertChain);

        return resolveExecutionContext(in, geo, tokens, transportSecurityStrengthFactor, protocolVersion);
	}

    @Override
    public TimeConstraints readTimeConstraintsIfPresent(CougarObjectInput in, byte protocolVersion) throws IOException {
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_TIME_CONSTRAINTS) {
            boolean haveTimeConstraints = in.readBoolean();
            if (haveTimeConstraints) {
                long timeout = in.readLong();
                // this is the 'raw' time constraint which will be combined with the resolved client request time in the command resolver
                return DefaultTimeConstraints.fromTimeout(timeout);
            }
        }
        return DefaultTimeConstraints.NO_CONSTRAINTS;
    }

    void writeIdentity(IdentityChain identity, CougarObjectOutput out, IdentityResolver identityResolver) throws IOException {
		List<IdentityToken> identityTokens = null;
		if (identityResolver != null) {
            identityTokens = identityResolver.tokenise(identity);
		}
		identityTokenResolver.rewrite(identityTokens, out);
	}

    GeoLocationParameters readGeoLocation(CougarObjectInput in, String remoteAddress, byte protocolVersion) throws IOException {
        // The current implementation is analogous to an http connection, so the IP
        // must be provided, but everything else is created on the server. This might
        // become a problem if non-IP based clients are required, but there are no
        // requirements for them currently so to guess how they will work is pointless.
        String resolvedAddresses = in.readString();
        List<String> addressList = RemoteAddressUtils.parse(null, resolvedAddresses);
        String inferredCountry = null;
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            inferredCountry = in.readString();
        }

        // no remote address for socket protocol.
        return new GeoLocationParameters(remoteAddress, addressList, inferredCountry);
	}

	void writeGeoLocation(GeoLocationDetails geo, CougarObjectOutput out, byte protocolVersion) throws IOException {
        // See comment about reading geo location.
        String resolvedAddresses = RemoteAddressUtils.externaliseWithLocalAddresses(geo.getResolvedAddresses());
        out.writeString(resolvedAddresses);
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            out.writeString(geo.getInferredCountry());
        }
	}

    private String readRequestUuidString(CougarObjectInput in) throws IOException {
        if (in.readBoolean()) {
            return in.readString();
        } else {
            return null;
        }
    }

    void writeRequestUUID(RequestUUID uuid, CougarObjectOutput out, byte protocolVersion) throws IOException {
        if (uuid != null) {
            out.writeBoolean(true);
            if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
                out.writeString(uuid.getNewSubUUID().toString());
            }
            else {
                out.writeString(uuid.getNewSubUUID().getLocalUUIDComponent());
            }
        } else {
            out.writeBoolean(false);
        }
    }

    private Date readReceivedTime(CougarObjectInput in) throws IOException {
        if (in.readBoolean()) {
            Long ticks = in.readLong();
            return new Date(ticks);
        }
        return null;
    }

    void writeReceivedTime(Date receivedTime, CougarObjectOutput out) throws IOException {
        if (receivedTime != null) {
            out.writeBoolean(true);
            out.writeLong(receivedTime.getTime());
        } else {
            out.writeBoolean(false);
        }
    }

    @ManagedAttribute
    public boolean isHardFailEnumDeserialisation() {
        return hardFailEnumDeserialisation;
    }

    public void setHardFailEnumDeserialisation(boolean hardFailEnumDeserialisation) {
        this.hardFailEnumDeserialisation = hardFailEnumDeserialisation;
    }
}
