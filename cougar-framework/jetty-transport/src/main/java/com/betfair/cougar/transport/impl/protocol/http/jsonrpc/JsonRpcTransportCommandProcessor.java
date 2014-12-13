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

package com.betfair.cougar.transport.impl.protocol.http.jsonrpc;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.*;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.CougarInternalOperations;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.marshalling.impl.databinding.json.JSONBindingFactory;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.transport.api.protocol.http.ExecutionContextFactory;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpCommandProcessor;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.ev.OperationKey.Type;
import com.betfair.cougar.core.api.fault.Fault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.TransportCommand;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.ResponseCodeMapper;
import com.betfair.cougar.transport.impl.protocol.http.jsonrpc.JsonRpcOperationBinding.JsonRpcParam;
import com.betfair.cougar.util.stream.ByteCountingInputStream;
import com.betfair.cougar.util.stream.ByteCountingOutputStream;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class JsonRpcTransportCommandProcessor extends AbstractHttpCommandProcessor<Void> {
    private static Logger LOGGER = LoggerFactory.getLogger(JsonRpcTransportCommandProcessor.class);

	private static final int PARSE_ERROR = -32700;
	private static final int INVALID_REQUEST = -32600;
	private static final int METHOD_NOT_FOUND = -32601;
	private static final int INVALID_PARAMS = -32602;
	private static final int INTERNAL_ERROR = -32603;
	private static final int SERVER_ERROR = -32099;

	private static final JavaType BATCH_REQUEST_TYPE = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, JsonRpcRequest.class);
	private static final JavaType SINGLE_REQUEST_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(JsonRpcRequest.class);

	private ObjectMapper mapper;

	private Map<String, JsonRpcOperationBinding> bindings = new HashMap<String, JsonRpcOperationBinding>();

    // package private for testing
    static final String IDENTITY_RESOLUTION_NAMESPACE = null;
    // package private for testing
    static final OperationDefinition IDENTITY_RESOLUTION_OPDEF = new SimpleOperationDefinition(
            CougarInternalOperations.RESOLVE_IDENTITIES,
            new Parameter[0], ParameterType.create(Void.class)
    );
    // package private for testing
    static final Executable IDENTITY_RESOLUTION_EXEC = new Executable() {
        @Override
        public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints expiry) {
            observer.onResult(new ExecutionResult(null));
        }
    };
    // package private for testing
    static final ExecutionTimingRecorder IDENTITY_RESOLUTION_TIMING_RECORDER = new NullExecutionTimingRecorder();


    public JsonRpcTransportCommandProcessor(DehydratedExecutionContextResolution contextResolution, String requestTimeoutHeader, ObjectMapper mapper) {
        super(Protocol.JSON_RPC, contextResolution, requestTimeoutHeader);
        setName("JsonRpcTransportCommandProcessor");
        this.mapper = mapper;
    }

	public JsonRpcTransportCommandProcessor(DehydratedExecutionContextResolution contextResolution, String requestTimeoutHeader, JSONBindingFactory jsonBindingFactory) {
		this(contextResolution,requestTimeoutHeader,jsonBindingFactory.createBaseObjectMapper());
	}

	@Override
	public void onCougarStart() {
        boolean operationsBound = false;
		for (ServiceBindingDescriptor bindingDescriptor : getServiceBindingDescriptors()) {
			for (OperationBindingDescriptor opDesc : bindingDescriptor.getOperationBindings()) {
				if (bindOperation(getOperationDefinition(opDesc.getOperationKey()))) {
                    operationsBound = true;
                }
			}
		}

        // if some operations were bound then register in our "special" executable for resolving identities
        // thread-safely prior to batch executions
        if (operationsBound) {
            getExecutionVenue().registerOperation(IDENTITY_RESOLUTION_NAMESPACE,IDENTITY_RESOLUTION_OPDEF,IDENTITY_RESOLUTION_EXEC, IDENTITY_RESOLUTION_TIMING_RECORDER, 0);
        }
	}

	public boolean bindOperation(OperationDefinition operationDefinition) {
		if (operationDefinition!=null) {
			OperationKey key = operationDefinition.getOperationKey();
			if (key.getType()==Type.Request) {
				//build method name
				JsonRpcOperationBinding binding = new JsonRpcOperationBinding(operationDefinition);
				bindings.put(stripMinorVersionFromUri(binding.getJsonRpcMethod()), binding);
                return true;
			}
		}
        return false;
	}

	@Override
	protected CommandResolver<HttpCommand> createCommandResolver(final HttpCommand http, final Tracer tracer) {
        final DehydratedExecutionContext context = resolveExecutionContext(http, null);
        tracer.start(context.getRequestUUID(), CougarInternalOperations.BATCH_CALL);


		final List<JsonRpcRequest> requests = new ArrayList<>();
		final List<ExecutionCommand> commands = new LinkedList<>();
		final List<JsonRpcResponse> responses = new ArrayList<>();

		JsonNode root;
		ByteCountingInputStream iStream = null;
		try {
			iStream = createByteCountingInputStream(http.getRequest().getInputStream());
            try {
                EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
                root = mapper.readTree(iStream);
                final long bytesRead = iStream.getCount();
                final boolean isBatch = root.isArray();
                if (isBatch) {
                    requests.addAll((List<JsonRpcRequest>)mapper.convertValue(root, BATCH_REQUEST_TYPE));
                } else {
                    JsonRpcRequest rpc = mapper.convertValue(root, SINGLE_REQUEST_TYPE);
                    requests.add(rpc);
                }

                if (requests.isEmpty()) {
                    writeErrorResponse(http, context, new CougarValidationException(ServerFaultCode.NoRequestsFound, "No Requests found in rpc call"), true);
                } else {
                    final TimeConstraints realTimeConstraints = DefaultTimeConstraints.rebaseFromNewStartTime(context.getRequestTime(), readRawTimeConstraints(http.getRequest()));
                    for (final JsonRpcRequest rpc : requests) {
                        final JsonRpcOperationBinding binding = bindings.get(stripMinorVersionFromUri(rpc.getMethod().toLowerCase()));
                        if (binding!=null) {
                            try {
                                JsonRpcParam [] paramDefs = binding.getJsonRpcParams();
                                final Object [] args = new Object[paramDefs.length];
                                for (int i=0;i<paramDefs.length;i++) {
                                    JsonNode paramValue = rpc.getParams().isArray() ? rpc.getParams().get(i) : rpc.getParams().get(paramDefs[i].getName());
                                    JavaType javaType = paramDefs[i].getJavaType();
                                    args[i] = mapper.convertValue(paramValue, javaType);
                                    // complex types are handled by the mapper, but for some reason, direct enums are not
                                    if (javaType.isEnumType() && args[i] != null && ((Enum)args[i]).name().equals("UNRECOGNIZED_VALUE")) {
                                        throw new IllegalArgumentException(new Exception(new EnumDerialisationException("UNRECOGNIZED_VALUE is not allowed as an input")));
                                    }
                                }
                                commands.add(new ExecutionCommand() {//2nd: index 0, 3rd: index 1, 4th: index 2
                                    @Override
                                    public void onResult(ExecutionResult executionResult) {
                                        JsonRpcResponse response = buildExecutionResultResponse(rpc, executionResult);
                                        synchronized(responses) {
                                            responses.add(response);
                                            writeResponseIfComplete(http, context, isBatch, requests, responses, bytesRead, tracer);
                                        }
                                    }
                                    @Override
                                    public OperationKey getOperationKey() {
                                        return binding.getOperationDefinition().getOperationKey();
                                    }
                                    @Override
                                    public Object[] getArgs() {
                                        return args;
                                    }

                                    @Override
                                    public TimeConstraints getTimeConstraints() {
                                        return realTimeConstraints;
                                    }
                                });
                            } catch (Exception e) {
                                if (e instanceof IllegalArgumentException && e.getCause() != null && (e.getCause().getCause()==null || e.getCause().getCause() instanceof EnumDerialisationException)) {
                                    responses.add(JsonRpcErrorResponse.buildErrorResponse(rpc, new JsonRpcError(INVALID_PARAMS, ServerFaultCode.ServerDeserialisationFailure.getDetail(), null)));
                                }
                                else {
                                    responses.add(JsonRpcErrorResponse.buildErrorResponse(rpc, new JsonRpcError(INVALID_PARAMS, ServerFaultCode.MandatoryNotDefined.getDetail(), null)));
                                }
                                writeResponseIfComplete(http, context, isBatch, requests, responses, bytesRead, tracer);
                            }
                        } else {
                            responses.add(JsonRpcErrorResponse.buildErrorResponse(rpc, new JsonRpcError(METHOD_NOT_FOUND, ServerFaultCode.NoSuchOperation.getDetail(), null)));
                            writeResponseIfComplete(http, context, isBatch, requests, responses, bytesRead, tracer);
                        }
                    }
                }
            } catch (Exception ex) {
                //This happens when there was a problem reading
                //deal with case where every request was bad
                writeErrorResponse(http, context, CougarMarshallingException.unmarshallingException("json",ex,false), true);
                commands.clear();
            }

			//return command resolver irrespective of whether it is empty so the top level processor doesn't error
			return new CommandResolver<HttpCommand>() {
				@Override
				public List<ExecutionCommand> resolveExecutionCommands() {
					return commands;
				}

                @Override
                public DehydratedExecutionContext resolveExecutionContext() {
                    return context;
                }
            };
		} catch (Exception e) {
            throw CougarMarshallingException.unmarshallingException("json", "Unable to resolve requests for json-rpc", e, false);
		} finally {
			try {
                if (iStream != null) {
                    iStream.close();
                }
			} catch (IOException ignored) {
				ignored.printStackTrace();
			}
		}
	}

    @Override
    public void process(HttpCommand command) {
        boolean traceStarted = false;
        incrementCommandsProcessed();
        DehydratedExecutionContext ctx = null;
        try {
            validateCommand(command);
            final CommandResolver<HttpCommand> resolver = createCommandResolver(command, tracer);
            traceStarted = true;
            ctx = resolver.resolveExecutionContext();

            final TimeConstraints realTimeConstraints = DefaultTimeConstraints.rebaseFromNewStartTime(ctx.getRequestTime(), readRawTimeConstraints(command.getRequest()));
            final DehydratedExecutionContext finalCtx = ctx;
            ExecutionCommand resolveCommand = new ExecutionCommand() {
                @Override
                public OperationKey getOperationKey() {
                    return IDENTITY_RESOLUTION_OPDEF.getOperationKey();
                }

                @Override
                public Object[] getArgs() {
                    return new Object[0];
                }

                @Override
                public void onResult(ExecutionResult executionResult) {
                    Iterable<ExecutionCommand> batchCalls = resolver.resolveExecutionCommands();
                    if (executionResult.isFault()) {
                        for (ExecutionCommand exec : batchCalls) {
                            exec.onResult(executionResult);
                        }
                    }
                    // now we have an ExecutionContext that's correctly filled..
                    else {
                        // this has to be an ExecutionContext and not a DehydratedExecutionContext to ensure that
                        // BaseExecutionVenue doesn't try to re-resolve
                        ExecutionContext context = ExecutionContextFactory.resolveExecutionContext(finalCtx, finalCtx.getIdentity());
                        for (ExecutionCommand exec : resolver.resolveExecutionCommands()) {
                            ExecutionContext subContext = createCallContext(context);
                            tracer.start(subContext.getRequestUUID(), exec.getOperationKey());
                            ExecutionCommand newCommand = createTraceableSubCommand(exec, tracer, subContext.getRequestUUID());
                            executeCommand(newCommand, context);
                        }
                    }
                }

                @Override
                public TimeConstraints getTimeConstraints() {
                    return realTimeConstraints;
                }
            };
            executeCommand(resolveCommand, ctx);
        } catch (CougarException ex) {
            //this indicates an exception beyond the normal flow occurred
            //We can only deal with this by sending a batch fail message
            //Normal business thrown exceptions should not be handled by this call
            writeErrorResponse(command, ctx, ex, traceStarted);
        } catch (Throwable ex) {
            //We cannot let any exception percolate beyond this point as the conventional error response
            //publication mechanism doesn't work cleanly for JSON-RPC
            writeErrorResponse(command, ctx, new CougarServiceException(ServerFaultCode.ServiceRuntimeException, ex.getMessage()), traceStarted);
        }
    }

    private ExecutionCommand createTraceableSubCommand(final ExecutionCommand exec, final Tracer tracer, final RequestUUID requestUUID) {
        return new ExecutionCommand() {
            @Override
            public OperationKey getOperationKey() {
                return exec.getOperationKey();
            }

            @Override
            public Object[] getArgs() {
                return exec.getArgs();
            }

            @Override
            public TimeConstraints getTimeConstraints() {
                return exec.getTimeConstraints();
            }

            @Override
            public void onResult(ExecutionResult executionResult) {
                tracer.end(requestUUID);
                exec.onResult(executionResult);
            }
        };
    }

    private ExecutionContext createCallContext(final ExecutionContext context) {
        final RequestUUID callUuid = context.getRequestUUID().getNewSubUUID();
        return new ExecutionContext() {
            @Override
            public GeoLocationDetails getLocation() {
                return context.getLocation();
            }

            @Override
            public IdentityChain getIdentity() {
                return context.getIdentity();
            }

            @Override
            public RequestUUID getRequestUUID() {
                return callUuid;
            }

            @Override
            public Date getReceivedTime() {
                return context.getReceivedTime();
            }

            @Override
            public Date getRequestTime() {
                return context.getRequestTime();
            }

            @Override
            public boolean traceLoggingEnabled() {
                return context.traceLoggingEnabled();
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return context.getTransportSecurityStrengthFactor();
            }

            @Override
            public boolean isTransportSecure() {
                return context.isTransportSecure();
            }
        };
    }


    /**
     * Please note this should only be used when the JSON rpc call itself fails - the
     * answer will not contain any mention of the requests that caused the failure,
     * nor their ID
     * @param command the command that caused the error
     * @param context
     * @param error
     * @param traceStarted
     */
	@Override
	public void writeErrorResponse(HttpCommand command, DehydratedExecutionContext context, CougarException error, boolean traceStarted) {
        try {
            incrementErrorsWritten();
            final HttpServletResponse response = command.getResponse();
            try {
                long bytesWritten = 0;
                if(error.getResponseCode() != ResponseCode.CantWriteToSocket) {

                    ResponseCodeMapper.setResponseStatus(response, error.getResponseCode());
                    ByteCountingOutputStream out = null;
                    try {
                        int jsonErrorCode = mapServerFaultCodeToJsonErrorCode(error.getServerFaultCode());
                        JsonRpcError rpcError = new JsonRpcError(jsonErrorCode, error.getFault().getErrorCode(), null);
                        JsonRpcErrorResponse jsonRpcErrorResponse = JsonRpcErrorResponse.buildErrorResponse(null, rpcError);

                        out = new ByteCountingOutputStream(response.getOutputStream());
                        mapper.writeValue(out, jsonRpcErrorResponse);
                        bytesWritten = out.getCount();
                    } catch (IOException ex) {
                        handleResponseWritingIOException(ex, error.getClass());
                    } finally {
                        closeStream(out);
                    }
                } else {
                    LOGGER.debug("Skipping error handling for a request where the output channel/socket has been prematurely closed");
                }
                logAccess(command,
                        resolveContextForErrorHandling(context, command), -1,
                        bytesWritten, MediaType.APPLICATION_JSON_TYPE,
                        MediaType.APPLICATION_JSON_TYPE, error.getResponseCode());
            } finally {
                command.onComplete();
            }
        }
        finally {
            if (context != null && traceStarted) {
                tracer.end(context.getRequestUUID());
            }
        }
    }

 	public boolean writeResponseIfComplete(HttpCommand command, DehydratedExecutionContext context, boolean isBatch, List<JsonRpcRequest> requests, List<JsonRpcResponse> responses, long bytesRead, Tracer tracer) {
        if (requests.size()==responses.size()) {
            try {
                final HttpServletResponse response = command.getResponse();
                final IdentityTokenResolver<HttpServletRequest,HttpServletResponse, X509Certificate[]> tokenResolver =
                        (IdentityTokenResolver<HttpServletRequest,HttpServletResponse, X509Certificate[]>) command.getIdentityTokenResolver();
                if (command.getStatus() == TransportCommand.CommandStatus.InProgress) {
                    try {
                        ResponseCodeMapper.setResponseStatus(response, ResponseCode.Ok);
                        if (context != null && context.getIdentity() != null && tokenResolver != null) {
                            writeIdentity(context.getIdentityTokens(), new IdentityTokenIOAdapter() {
                                @Override
                                public void rewriteIdentityTokens(List<IdentityToken> identityTokens) {
                                    tokenResolver.rewrite(identityTokens, response);
                                }

                                @Override
                                public boolean isRewriteSupported() {
                                    return tokenResolver.isRewriteSupported();
                                }
                            });
                        }
                        response.setContentType(MediaType.APPLICATION_JSON);
                        ByteCountingOutputStream out = null;
                        try {
                            out = new ByteCountingOutputStream(response.getOutputStream());
                            mapper.writeValue(out, isBatch ? responses : responses.get(0));
                        } finally {
                            closeStream(out);
                        }

                        logAccess(command,
                                context, bytesRead,
                                out.getCount(), MediaType.APPLICATION_JSON_TYPE,
                                MediaType.APPLICATION_JSON_TYPE, ResponseCode.Ok);

                    } catch (Exception e) {
                        writeErrorResponse(command, context, handleResponseWritingIOException(e, JsonRpcResponse.class), false); // it has been started but we'll call end below
                    } finally {
                        command.onComplete();
                    }
                }
                return true;
            }
            finally {
                tracer.end(context.getRequestUUID());
            }
        }
        else {
            return false;
        }
    }

    private JsonRpcResponse buildExecutionResultResponse(JsonRpcRequest rpc, ExecutionResult executionResult) {
        JsonRpcResponse response = null;
        if (executionResult.getResultType() == ExecutionResult.ResultType.Success) {
            response = JsonRpcSuccessResponse.buildSuccessResponse(rpc, executionResult.getResult());
        } else if (executionResult.getResultType() == ExecutionResult.ResultType.Fault) {
            Fault fault = executionResult.getFault().getFault();
            HashMap<String,Object> detailMap = new HashMap<String,Object>();
            FaultDetail detail = fault.getDetail();
            if (FaultController.getInstance().isDetailedFaults()) {
                detailMap.put("trace", detail.getStackTrace());
                detailMap.put("message", detail.getDetailMessage());
            }
            List<String[]> faultMessages = detail.getFaultMessages();
            if (faultMessages != null) {
                detailMap.put("exceptionname", detail.getFaultName());
                HashMap<String,Object> paramMap = new HashMap<String,Object>();
                detailMap.put(detail.getFaultName(), paramMap);
                for (String[] msg: faultMessages) {
                    paramMap.put(msg[0], msg[1]);
                }
            }

            int jsonErrorCode = mapServerFaultCodeToJsonErrorCode(executionResult.getFault().getServerFaultCode());
            JsonRpcError error = new JsonRpcError(jsonErrorCode, fault.getErrorCode(), !detailMap.isEmpty() ? detailMap : null);

            response = JsonRpcErrorResponse.buildErrorResponse(rpc, error);
        }
        return response;
    }

    private int mapServerFaultCodeToJsonErrorCode(ServerFaultCode serverFaultCode) {
        int jsonErrorCode;
        switch (serverFaultCode) {
            case MandatoryNotDefined:
                jsonErrorCode = INVALID_PARAMS;
                break;

            case ServiceRuntimeException:
                jsonErrorCode = INTERNAL_ERROR;
                break;

            case NoRequestsFound:
                jsonErrorCode = INVALID_REQUEST;
                break;

            case ContentTypeNotValid:
            case ServerDeserialisationFailure:
                jsonErrorCode = PARSE_ERROR;
                break;

            default:
                jsonErrorCode = SERVER_ERROR;
        }
        return jsonErrorCode;
    }
}
