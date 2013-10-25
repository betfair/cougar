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

package com.betfair.cougar.transport.impl.protocol.http.jsonrpc;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutionTimingRecorder;
import com.betfair.cougar.core.api.ev.NullExecutionTimingRecorder;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionPostProcessor;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.ev.BaseExecutionVenue;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.logging.EventLogDefinition;
import com.betfair.cougar.logging.EventLoggingRegistry;
import com.betfair.cougar.marshalling.impl.databinding.json.JSONBindingFactory;
import com.betfair.cougar.marshalling.impl.databinding.json.JSONDateFormat;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.TransportCommand;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.jsonrpc.JsonRpcOperationBindingDescriptor;
import com.betfair.cougar.transport.impl.AbstractCommandProcessor;
import com.betfair.cougar.transport.impl.CommandValidatorRegistry;
import com.betfair.cougar.transport.impl.protocol.http.ContentTypeNormaliser;
import com.betfair.cougar.transport.impl.protocol.http.DefaultGeoLocationDeserializer;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executor;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for @See JsonRpcTransportCommandProcessor
 */
public class JsonRpcTransportCommandProcessorTest  {
    public static final String       AZ            = "Azerbaijan";

    public static final String       SERVICE_NAME  = "TestingService";
    public static final ServiceVersion       SERVICE_VERSION  = new ServiceVersion("v1.0");

    public static final String       OP_NAME       = "characterCount";
    public static final OperationKey TEST_OP_KEY   = new OperationKey(SERVICE_VERSION, SERVICE_NAME, OP_NAME);

    public static final String       OP_NAME2      = "dateEcho";
    public static final OperationKey TEST_OP2_KEY  = new OperationKey(SERVICE_VERSION, SERVICE_NAME, OP_NAME2);

    private LocalJsonRpcCommandProcessor commandProcessor;
    private ObjectMapper objectMapper;
    private JSONDateFormat jdf = new JSONDateFormat();
    protected RequestLogger logger;
    GeoIPLocator geoIPLocator;
    protected CommandValidatorRegistry<HttpCommand> validatorRegistry = new CommandValidatorRegistry<HttpCommand>();
    private ExecutionVenue ev;

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }


    @Before
    public void init() {
        geoIPLocator = mock(GeoIPLocator.class);
        ContentTypeNormaliser ctn = mock(ContentTypeNormaliser.class);
        EventLoggingRegistry mockEventLoggingRegistry = mock(EventLoggingRegistry.class);
        EventLogDefinition logDef = mock(EventLogDefinition.class);
        logger = mock(RequestLogger.class);

        when(ctn.getNormalisedRequestMediaType(any(HttpServletRequest.class))).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(ctn.getNormalisedResponseMediaType(any(HttpServletRequest.class))).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(mockEventLoggingRegistry.getInvokableLogger(anyString())).thenReturn(logDef);
        when(logDef.getLogName()).thenReturn("");


        commandProcessor = new LocalJsonRpcCommandProcessor();
        commandProcessor.setContentTypeNormaliser(ctn);
        commandProcessor.setRequestLogger(logger);
        commandProcessor.setValidatorRegistry(validatorRegistry);
        commandProcessor.setExecutor(new Executor() {
            @Override
            public void execute(Runnable runnable) {
                runnable.run();
            }
        });
        commandProcessor.setExecutionVenue(ev = mock(ExecutionVenue.class));




        objectMapper = new ObjectMapper();

    }

    private void bindOperations() {
        bindOperations(ev, true);
    }

    private void bindOperations(ExecutionVenue ev, boolean mocked) {
        // register the ops in the ev - well, sort of
        OperationDefinition def1 = new OperationDefinition() {

            @Override
            public OperationKey getOperationKey() {
                return TEST_OP_KEY;
            }

            @Override
            public Parameter[] getParameters() {
                return new Parameter[] { new Parameter("message", ParameterType.create(String.class, null), true),
                        new Parameter("count", ParameterType.create(Integer.class, null), true)};
            }

            @Override
            public ParameterType getReturnType() {
                return null;
            }
        };
        OperationDefinition def2 = new OperationDefinition() {

            @Override
            public OperationKey getOperationKey() {
                return TEST_OP2_KEY;
            }

            @Override
            public Parameter[] getParameters() {
                return new Parameter[] {
                        new Parameter("date", ParameterType.create(Date.class, null), true)
                };
            }

            @Override
            public ParameterType getReturnType() {
                return ParameterType.create(Date.class, null);
            }
        };

        if (mocked) {
            when(ev.getOperationDefinition(TEST_OP_KEY)).thenReturn(def1);
            when(ev.getOperationDefinition(TEST_OP2_KEY)).thenReturn(def2);
        }
        else {
            Executable noop = new Executable() {
                @Override
                public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue) {
                    observer.onResult(new ExecutionResult(null));
                }
            };
            ExecutionTimingRecorder nullMgr = new NullExecutionTimingRecorder();
            ev.registerOperation(null, def1, noop, nullMgr);
            ev.registerOperation(null, def2, noop, nullMgr);
        }

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[]{
                        new JsonRpcOperationBindingDescriptor(TEST_OP_KEY),
                        new JsonRpcOperationBindingDescriptor(TEST_OP2_KEY)
                };
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return SERVICE_VERSION;
            }

            @Override
            public String getServiceName() {
                return SERVICE_NAME;
            }

            @Override
            public Protocol getServiceProtocol() {
                return Protocol.JSON_RPC;
            }
        });
        commandProcessor.onCougarStart();
    }

    @Test
    public void ensureIdentityResolverBound() {
        bindOperations();

        verify(ev, times(1)).registerOperation(JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_NAMESPACE,
                                               JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_OPDEF,
                                               JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_EXEC,
                                               JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_TIMING_RECORDER);
//        verify(ev, times(1)).execute(any(ExecutionContextWithTokens.class), eq(JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_OPDEF.getOperationKey()), eq(new Object[0]), any(ExecutionObserver.class));
    }

    @Test
    public void ensureNoIdentityResolverBoundWhenNoOperations() {
        commandProcessor.onCougarStart();

        verify(ev, times(0)).registerOperation(JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_NAMESPACE,
                JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_OPDEF,
                JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_EXEC,
                JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_TIMING_RECORDER);
//        verify(ev, times(0)).execute(any(ExecutionContextWithTokens.class), eq(JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_OPDEF.getOperationKey()), eq(new Object[0]), any(ExecutionObserver.class));
    }

    @Test
    public void ensureSeperateCallMadeToResolveIdentity() throws IOException {
        TestBaseExecutionVenue realEv = new TestBaseExecutionVenue();
        commandProcessor.setExecutionVenue(realEv);
        bindOperations(realEv, false);

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestTimer mockTimer = mock(RequestTimer.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);
        when(command.getTimer()).thenReturn(mockTimer);

        //Also note that we're mixing the case of the method call up - JSON rpc implementation
        //matches operations in a case insensitive fashion
        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [\"Hello\", 333], \"id\": 1}";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);
        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        commandProcessor.process(command);

        assertEquals(2, realEv.requests.size());
        ExecutionRequest req1 = realEv.requests.get(0);
        assertTrue(ExecutionContextWithTokens.class.isAssignableFrom(req1.ctx.getClass()));
        assertEquals(JsonRpcTransportCommandProcessor.IDENTITY_RESOLUTION_OPDEF.getOperationKey(), req1.key);
        assertEquals(0, req1.args.length);
        ExecutionRequest req2 = realEv.requests.get(1);
        assertFalse(ExecutionContextWithTokens.class.isAssignableFrom(req2.ctx.getClass()));
        assertEquals(TEST_OP_KEY, req2.key);
    }

    @Test
    public void testCreateCommandResolverSingleOp() throws IOException {
        bindOperations();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        HttpCommand mockedCommand = mock(HttpCommand.class);
        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [\"Hello\", 333], \"id\": 1}";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandResolver<HttpCommand> resolver = commandProcessor.createCommandResolver(mockedCommand);
        verify(mockedCommand).getIdentityTokenResolver();
        verify(tokenResolver).resolve(request, null);
        assertNotNull(resolver);
        Iterable<ExecutionCommand> cmds = resolver.resolveExecutionCommands();
        assertNotNull(cmds);
        assertTrue(cmds.iterator().hasNext());

        ExecutionCommand executionCommand = cmds.iterator().next();
        assertNotNull(executionCommand);

        Object[] args = executionCommand.getArgs();
        assertTrue(args.length == 2);
        assertEquals(args[0], "Hello");
        assertEquals(args[1], 333);

        OperationKey actual = executionCommand.getOperationKey();
        assertEquals(TEST_OP_KEY, actual);

        ExecutionContext ctx = resolver.resolveExecutionContext();
        assertNotNull(ctx);

        //Now we need to ensure that it attempts to write to the response when result is called
        executionCommand.onResult(new ExecutionResult());

        verify(response).getOutputStream();
        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testCreateCommandResolverBatched() throws IOException, ParseException {
        bindOperations();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        HttpCommand mockedCommand = mock(HttpCommand.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        String body="[{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME  + "\", \"params\": [\"Hello\", 333], \"id\": \"1\"}," +
                     "{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME2 + "\", \"params\": [\"2009-01-01T00:00:00.333Z\"], \"id\": \"yyyy\"}]";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandResolver<HttpCommand> resolver = commandProcessor.createCommandResolver(mockedCommand);
        assertNotNull(resolver);
        Iterable<ExecutionCommand> cmds = resolver.resolveExecutionCommands();
        assertNotNull(cmds);
        assertTrue(cmds.iterator().hasNext());

        Iterator<ExecutionCommand> iter = cmds.iterator();

        //first command should be the OP_NAME
        ExecutionCommand executionCommand = iter.next();
        assertNotNull(executionCommand);
        assertEquals(executionCommand.getOperationKey(), TEST_OP_KEY);
        executionCommand.onResult(new ExecutionResult());

        assertTrue(iter.hasNext());

        //Second should be OP_NAME2 - dateEcho
        executionCommand = iter.next();
        assertNotNull(executionCommand);
        assertEquals(executionCommand.getOperationKey(), TEST_OP2_KEY);

        Object[] args = executionCommand.getArgs();
        assertTrue(args.length == 1);

        //Very quick marshalling test for dates
        Calendar gc = new GregorianCalendar(2009, 0, 1, 0, 0, 0);
        gc.set(Calendar.MILLISECOND, 333);
        Date d = gc.getTime();
        assertEquals(args[0], d);

        //Now we need to ensure that it attempts to write to the response when result is called
        executionCommand.onResult(new ExecutionResult(d));
        String written = tos.getCapturedOutputStream();


        int messageSep = written.indexOf("},{");
        assertTrue(messageSep > -1);
        String firstResponse = written.substring(1, messageSep + 1);
        Map m = parseAndValidateResult(firstResponse, "1");
        Object o = m.get("result");
        assertNull(o);

        String secondResponse = written.substring(messageSep + 2, written.length() - 1);
        m = parseAndValidateResult(secondResponse, "yyyy");
        Object result = m.get("result");
        assertNotNull(result);
        Date resultDate = jdf.parse(result.toString());
        assertEquals(resultDate, d);

        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testExceptionalResultUnBatched() throws IOException {
        bindOperations();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        HttpCommand mockedCommand = mock(HttpCommand.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME  + "\", \"params\": [\"Hello\", 333], \"id\": \"fail\"}";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandResolver<HttpCommand> resolver = commandProcessor.createCommandResolver(mockedCommand);
        assertNotNull(resolver);
        Iterable<ExecutionCommand> cmds = resolver.resolveExecutionCommands();
        assertNotNull(cmds);
        assertTrue(cmds.iterator().hasNext());
        Iterator<ExecutionCommand> iter = cmds.iterator();
        ExecutionCommand executionCommand = iter.next();
        assertNotNull(executionCommand);
        assertEquals(executionCommand.getOperationKey(), TEST_OP_KEY);

        executionCommand.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.MandatoryNotDefined, "Field x is not defined")));

        String written = tos.getCapturedOutputStream();
        Map m = parseAndValidateResult(written, "fail");

        assertTrue(m.containsKey("error"));
        Map errorMap = (Map)m.get("error");

        assertTrue(errorMap.containsKey("code"));
        assertTrue(((Integer)errorMap.get("code")) <= -32099);

        assertTrue(errorMap.containsKey("message"));
        assertTrue(((String)errorMap.get("message")).startsWith("DSC-"));

        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testBatchMixedSuccessAndFailure() throws IOException {
        bindOperations();

        //Try one call that works, one to a method that doesn't exist and another with a mandatory param missing

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        HttpCommand mockedCommand = mock(HttpCommand.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        //Also note that we're mixing the case of the method call up - JSON rpc implementation
        //matches operations in a case insensitive fashion
        String body="[{ \"method\": \"thisMethodDoesntExist/v1.0/nonExistent\", \"params\": [], \"id\": \"1\"}," +
                     "{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [], \"id\": \"2\"}," +
                     "{ \"method\": \"" + SERVICE_NAME.toLowerCase() + "/v1.0/" + OP_NAME.toUpperCase()  + "\", \"params\": [\"Hello\", 333], \"id\": \"3\"}]";

        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandResolver<HttpCommand> resolver = commandProcessor.createCommandResolver(mockedCommand);
        assertNotNull(resolver);
        Iterable<ExecutionCommand> cmds = resolver.resolveExecutionCommands();
        assertNotNull(cmds);
        assertTrue(cmds.iterator().hasNext());
        Iterator<ExecutionCommand> iter = cmds.iterator();

        //This should lead to two executionCommands - the method that doesn't exist shouldn't
        //result in an ExecutionCommand

        ExecutionCommand cmd2 = iter.next();
        ExecutionCommand cmd3 = iter.next();

        assertNotNull(cmd2);
        assertNotNull(cmd3);

        assertFalse(iter.hasNext());

        //Throw a mandatory param exception
        cmd2.onResult(new ExecutionResult(new CougarValidationException(ServerFaultCode.MandatoryNotDefined, "Mandatory params not defined")));

        //Successful void result
        cmd3.onResult(new ExecutionResult());

        String written = tos.getCapturedOutputStream();
        List<Map> batchedResult = (List<Map>)objectMapper.readValue(written, List.class);

        Map result1 = findBatchedResultById(batchedResult, "1");
        assertNotNull(result1);

        assertTrue(result1.containsKey("jsonrpc"));
        assertTrue(result1.get("jsonrpc").equals("2.0"));

        assertFalse(result1.containsKey("result"));
        assertTrue(result1.containsKey("error"));
        Map error = (Map)result1.get("error");
        assertNotNull(error);

        assertTrue(error.containsKey("code"));
        //http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal
        //lists errors codes etc.
        assertEquals(-32601, error.get("code"));


        //Result 2 - missing mandatory params
        Map result2 = findBatchedResultById(batchedResult, "2");
        assertNotNull(result2);

        assertTrue(result2.containsKey("jsonrpc"));
        assertTrue(result2.get("jsonrpc").equals("2.0"));
        assertFalse(result2.containsKey("result"));
        assertTrue(result2.containsKey("error"));
        error = (Map)result2.get("error");
        assertNotNull(error);
        assertTrue(error.containsKey("code"));
        assertEquals(-32602, error.get("code"));

        //Result 3 - everything fine.
        Map result3 = findBatchedResultById(batchedResult, "3");
        assertNotNull(result3);
        assertTrue(result3.containsKey("jsonrpc"));
        assertTrue(result3.get("jsonrpc").equals("2.0"));
        assertTrue(result3.containsKey("result"));
        assertFalse(result3.containsKey("error"));

        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }


    @Test
    public void testBatchBadRequest() throws IOException {
        bindOperations();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        HttpCommand mockedCommand = mock(HttpCommand.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        String body="[{ \"this is never gonna parse\"}]";

        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandResolver<HttpCommand> resolver = commandProcessor.createCommandResolver(mockedCommand);


        //What should happen is a total parse failure and something should be written with parse error -32700
        String written = tos.getCapturedOutputStream();

        Map result = objectMapper.readValue(written, Map.class);
        Map error = (Map)result.get("error");
        assertTrue(error.containsKey("code"));
        assertEquals(-32700, error.get("code"));

        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testResolveExecutionContext() throws Exception {
        bindOperations();

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);

        GeoLocationDetails gld = Mockito.mock(GeoLocationDetails.class);
        //test an empty request
        List resolvedAddresses = Collections.emptyList();
        when(geoIPLocator.getGeoLocation(null, resolvedAddresses, AZ)).thenReturn(gld);
        ExecutionContext context = commandProcessor.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertNotNull(context.getRequestUUID());
        assertNotNull(context.getReceivedTime());
        assertNotNull(context.getLocation());

        //Test request contains uuid, id and remote address
        RequestUUIDImpl uuid = new RequestUUIDImpl();
        when(request.getHeader("X-UUID")).thenReturn(uuid.toString());
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4"), AZ)).thenReturn(gld);
        context = commandProcessor.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertEquals(uuid, context.getRequestUUID());
        assertEquals(gld, context.getLocation());

        //Test request contains X-Forwarded-For header and resolves geo-location correctly
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.20.30.40");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("10.20.30.40", "10.20.30.40"), AZ)).thenReturn(gld);
        context = commandProcessor.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertEquals(gld, context.getLocation());
    }

    @Test
    public void testResolveExecutionContextWithoutCountryResolver() throws Exception {
        bindOperations();

        LocalJsonRpcCommandProcessor commandProcessorWithoutCountryResolver =
                new LocalJsonRpcCommandProcessor(geoIPLocator, new DefaultGeoLocationDeserializer(), "X-UUID");

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);

        GeoLocationDetails gld = Mockito.mock(GeoLocationDetails.class);
        //test an empty request
        List resolvedAddresses = Collections.emptyList();
        when(geoIPLocator.getGeoLocation(null, resolvedAddresses, null)).thenReturn(gld);
        ExecutionContext context = commandProcessorWithoutCountryResolver.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertNotNull(context.getRequestUUID());
        assertNotNull(context.getReceivedTime());
        assertNotNull(context.getLocation());
        assertNull(context.getLocation().getInferredCountry());

        //Test request contains uuid, id and remote address
        RequestUUIDImpl uuid = new RequestUUIDImpl();
        when(request.getHeader("X-UUID")).thenReturn(uuid.toString());
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4"), null)).thenReturn(gld);
        context = commandProcessorWithoutCountryResolver.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertEquals(uuid, context.getRequestUUID());
        assertEquals(gld, context.getLocation());
        assertNull(context.getLocation().getInferredCountry());

        //Test request contains X-Forwarded-For header and resolves geo-location correctly
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.20.30.40");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("10.20.30.40", "10.20.30.40"), null)).thenReturn(gld);
        context = commandProcessorWithoutCountryResolver.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertEquals(gld, context.getLocation());
        assertNull(context.getLocation().getInferredCountry());
    }


    @Test
    public void testCallsValidators() throws Exception {
        bindOperations();

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestTimer mockTimer = mock(RequestTimer.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);
        when(command.getTimer()).thenReturn(mockTimer);

        CommandValidator<HttpCommand> validator = mock(CommandValidator.class);
        validatorRegistry.addValidator(validator);

        //Also note that we're mixing the case of the method call up - JSON rpc implementation
        //matches operations in a case insensitive fashion
        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [\"Hello\", 333], \"id\": 1}";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);
        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        commandProcessor.process(command);
        assertFalse(commandProcessor.errorCalled);
        verify(validator).validate(any(HttpCommand.class));
    }

    @Test
    public void testStopsOnValidatorFail() throws Exception {
        bindOperations();

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestTimer mockTimer = mock(RequestTimer.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);
        when(command.getTimer()).thenReturn(mockTimer);
        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        CommandValidator<HttpCommand> validator = new CommandValidator<HttpCommand>() {
            @Override
            public void validate(HttpCommand command) throws CougarException {
                throw new CougarServiceException(ServerFaultCode.SecurityException, "wibble");
            }
        };
        validatorRegistry.addValidator(validator);
        commandProcessor.process(command);
        assertTrue(commandProcessor.errorCalled);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testIOExceptionDuringMapping() throws Exception {
        bindOperations();

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        ServletInputStream is = mock(ServletInputStream.class);
        when(request.getInputStream()).thenReturn(is);
        when(is.read()).thenThrow(new IOException("i/o error"));
        when(is.read((byte[])any())).thenThrow(new IOException("i/o error"));
        when(is.read((byte[])any(),anyInt(),anyInt())).thenThrow(new IOException("i/o error"));

        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestTimer mockTimer = mock(RequestTimer.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);
        when(command.getTimer()).thenReturn(mockTimer);
        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        commandProcessor.process(command);
        assertTrue(commandProcessor.errorCalled);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testIOExceptionDueToTooMuchData() throws Exception {
        bindOperations();

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");

        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [\"Hello\", 333], \"id\": 1}";
        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);
        commandProcessor.setMaxPostBodyLength(10);

        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestTimer mockTimer = mock(RequestTimer.class);
        when(command.getRequest()).thenReturn(request);
        when(command.getResponse()).thenReturn(response);
        when(command.getTimer()).thenReturn(mockTimer);
        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        commandProcessor.process(command);
        assertTrue(commandProcessor.errorCalled);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    @Test
    public void testRandomExecutionFailure() throws IOException {
        bindOperations();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        HttpServletResponse response = mock(HttpServletResponse.class);
        IdentityTokenResolver tokenResolver = mock(IdentityTokenResolver.class);
        HttpCommand mockedCommand = mock(HttpCommand.class);
        RequestTimer mockTimer = mock(RequestTimer.class);

        commandProcessor.setExecutionVenue(new RandomFailureEV());

        when(mockedCommand.getRequest()).thenReturn(request);
        when(mockedCommand.getResponse()).thenReturn(response);
        when(mockedCommand.getIdentityTokenResolver()).thenReturn(tokenResolver);
        when(mockedCommand.getStatus()).thenReturn(TransportCommand.CommandStatus.InProcess);
        when(mockedCommand.getTimer()).thenReturn(mockTimer);

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                //Inline executor ...
                command.run();
            }
        };

        commandProcessor.setExecutor(executor);

        String body="{ \"method\": \"" + SERVICE_NAME + "/v1.0/" + OP_NAME + "\", \"params\": [\"hello\", 321], \"id\": \"1\"}";


        TestInputStream tis = new TestInputStream(new ByteArrayInputStream(body.getBytes("UTF-8")));
        when(request.getInputStream()).thenReturn(tis);

        TestOutputStream tos = new TestOutputStream();
        when(response.getOutputStream()).thenReturn(tos);

        commandProcessor.process(mockedCommand);
        String written = tos.getCapturedOutputStream();

        Map result = objectMapper.readValue(written, Map.class);
        Map error = (Map)result.get("error");
        assertTrue(error.containsKey("code"));
        assertEquals(-32603, error.get("code"));

        verify(logger).logAccess(eq(mockedCommand), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
    }

    /**
     * Tests that out of range numbers aren't shoe-horned into ints for non body parameters.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInRangeIntegerForNonBodyParam() throws IOException {
        bindOperations();

        ObjectMapper mapper = JSONBindingFactory.createBaseObjectMapper();
        String request = "{\"params\": [21474836470]}"; // If successfully narrowed to int, would be -10
        JsonNode root = mapper.readTree(new ByteArrayInputStream(request.getBytes()));
        JsonRpcRequest rpc = mapper.convertValue(root, TypeFactory.fastSimpleType(JsonRpcRequest.class));
        JsonNode paramValue = rpc.getParams().get(0);
        mapper.convertValue(paramValue, TypeFactory.fastSimpleType(Integer.class));
    }

    /**
     * Tests that out of range numbers aren't shoe-horned into ints for body parameters.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInRangeIntegerForBodyParam() throws IOException {
        bindOperations();

        ObjectMapper mapper = JSONBindingFactory.createBaseObjectMapper();
        String request =  "{\"params\": [{\"integer\":21474836470}]}";
        JsonNode root = mapper.readTree(new ByteArrayInputStream(request.getBytes()));
        JsonRpcRequest rpc = mapper.convertValue(root, TypeFactory.fastSimpleType(JsonRpcRequest.class));
        JsonNode paramValue = rpc.getParams().get(0);
        BodyType result = (BodyType) mapper.convertValue(paramValue, TypeFactory.type(BodyType.class));
    }

    /**
     * Tests that out of range numbers aren't shoe-horned into ints for non body parameters.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInRangeLongForNonBodyParam() throws IOException {
        bindOperations();

        ObjectMapper mapper = JSONBindingFactory.createBaseObjectMapper();
        String request = "{\"params\": [92233720368547758080]}"; // If successfully narrowed to long, would be 0
        JsonNode root = mapper.readTree(new ByteArrayInputStream(request.getBytes()));
        JsonRpcRequest rpc = mapper.convertValue(root, TypeFactory.fastSimpleType(JsonRpcRequest.class));
        JsonNode paramValue = rpc.getParams().get(0);
        System.out.println(mapper.convertValue(paramValue, TypeFactory.fastSimpleType(Long.class)));
    }

    /**
     * Tests that out of range numbers aren't shoe-horned into ints for body parameters.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testInRangeLongForBodyParam() throws IOException {
        bindOperations();

        ObjectMapper mapper = JSONBindingFactory.createBaseObjectMapper();
        String request =  "{\"params\": [{\"looong\":92233720368547758080}]}";
        JsonNode root = mapper.readTree(new ByteArrayInputStream(request.getBytes()));
        JsonRpcRequest rpc = mapper.convertValue(root, TypeFactory.fastSimpleType(JsonRpcRequest.class));
        JsonNode paramValue = rpc.getParams().get(0);
        BodyType result = (BodyType) mapper.convertValue(paramValue, TypeFactory.type(BodyType.class));
    }

    public static class BodyType {
        private Integer integer;
        private Long looong;
        public Integer getInteger() {
            return integer;
        }
        public void setInteger(Integer integer) {
            this.integer = integer;
        }
        public Long getLooong() {
            return looong;
        }
        public void setLooong(Long looong) {
            this.looong = looong;
        }
    }

    private Map findBatchedResultById(List<Map> batchedResults, String idToSearchFor) {
        for (Map batchedResult : batchedResults) {
            if (batchedResult.containsKey("id") && batchedResult.get("id").equals(idToSearchFor)) {
                return batchedResult;
            }
        }
        return null;
    }


    private Map parseAndValidateResult(String jsonResponse, String id) throws IOException {
        Map map = (Map)objectMapper.readValue(jsonResponse, Map.class);

        //A success result should always include jsonrpc 2.0
        assertTrue(map.containsKey("jsonrpc"));
        assertEquals("2.0", map.get("jsonrpc"));


        //Should also always include the original ID
        assertTrue(map.containsKey("id"));
        assertEquals(id, map.get("id"));

        return map;
    }

    
    public void testBatchedWithFailures() {

        ContentTypeNormaliser ctn = mock(ContentTypeNormaliser.class);

        
    }


    private static class TestOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream delegate;

        public TestOutputStream () {
            this.delegate = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        public String getCapturedOutputStream() {
            return new String(delegate.toByteArray());
        }
    }

    private static class TestInputStream extends ServletInputStream {
        private InputStream delegate;

        public TestInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }
    }

    private static class NonsenseTCP extends AbstractCommandProcessor {
        @Override
        protected CommandResolver createCommandResolver(TransportCommand command) {
            return null;
        }

        @Override
        protected void writeErrorResponse(TransportCommand command, ExecutionContextWithTokens context, CougarException e) {
        }

        @Override
        public void bind(ServiceBindingDescriptor serviceBindingDescriptor) {
        }

        @Override
        protected List<CommandValidator<HttpCommand>> getCommandValidators() {
            return Collections.EMPTY_LIST;
        }
    }

    private static class RandomFailureEV implements ExecutionVenue {

        @Override
        public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder) {
        }

        @Override
        public OperationDefinition getOperationDefinition(OperationKey key) {
            return null;
        }

        @Override
        public Set<OperationKey> getOperationKeys() {
            return null;
        }

        @Override
        public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer) {
            throw new NullPointerException("BANG");
        }

        @Override
        public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList) {
        }

        @Override
        public void setPostProcessors(List<ExecutionPostProcessor> preProcessorList) {
        }
    }

    private class LocalJsonRpcCommandProcessor extends JsonRpcTransportCommandProcessor {
        private boolean errorCalled;
        public LocalJsonRpcCommandProcessor() {
            super(geoIPLocator, new DefaultGeoLocationDeserializer(), "X-UUID", new InferredCountryResolver<HttpServletRequest>() {
                public String inferCountry(HttpServletRequest input) { return AZ;}
            });
        }

        public LocalJsonRpcCommandProcessor(GeoIPLocator geoIPLocator, GeoLocationDeserializer deserializer, String uuidHeader) {
            super(geoIPLocator, deserializer, uuidHeader, null);
        }

        @Override
        public void writeErrorResponse(HttpCommand command, ExecutionContextWithTokens context, CougarException e) {
            errorCalled = true;
            super.writeErrorResponse(command, context, e);
        }

        @Override
        public  <Req, Cert> ExecutionContextWithTokens resolveExecutionContext(HttpCommand http, Req req, Cert certs) {
            return super.resolveExecutionContext(http, req, certs);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    private class TestBaseExecutionVenue extends BaseExecutionVenue {
        private List<ExecutionRequest> requests = new ArrayList<ExecutionRequest>();
        @Override
        public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer) {
            requests.add(new ExecutionRequest(ctx, key, args, observer));
            super.execute(ctx, key, args, observer);
        }
    }
    private class ExecutionRequest {
        ExecutionContext ctx; OperationKey key; Object[] args; ExecutionObserver observer;

        private ExecutionRequest(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer) {
            this.ctx = ctx;
            this.key = key;
            this.args = args;
            this.observer = observer;
        }
    }
}

