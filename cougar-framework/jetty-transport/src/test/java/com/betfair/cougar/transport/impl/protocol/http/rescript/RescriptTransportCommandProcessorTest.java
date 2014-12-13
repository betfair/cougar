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

package com.betfair.cougar.transport.impl.protocol.http.rescript;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.InvalidCredentialsException;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.Fault;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.cougar.marshalling.impl.databinding.DataBindingManager;
import com.betfair.cougar.marshalling.impl.databinding.DataBindingMap;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.TransportCommand.CommandStatus;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.*;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor.ParamSource;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpCommandProcessorTest;
import com.betfair.cougar.transport.impl.protocol.http.ContentTypeNormaliser;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit test for @see RescriptTransportCommandProcessor
 *
 */
public class RescriptTransportCommandProcessorTest extends AbstractHttpCommandProcessorTest<Void> {

	private OperationBindingDescriptor[] operationBindings;

	private ServiceBindingDescriptor serviceBinding = new HttpServiceBindingDescriptor() {

        private ServiceVersion serviceVersion = new ServiceVersion("v1.2");

        @Override
        public String getServiceContextPath() {
            return "/myservice/";
        }
		@Override
		public OperationBindingDescriptor[] getOperationBindings() {
			return operationBindings;
		}

        @Override
        public Protocol getServiceProtocol() {
            return Protocol.RESCRIPT;
        }

        @Override
        public String getServiceName() {
            return "RescriptTestService";
        }

        @Override
        public ServiceVersion getServiceVersion() {
            return serviceVersion;
        }
    };

	private RescriptTransportCommandProcessor rescriptCommandProcessor;
	private Marshaller marshaller;
	private UnMarshaller unmarshaller;
	private FaultMarshaller faultMarshaller;
	private ContentTypeNormaliser ctn;
    private RescriptIdentityTokenResolver credentialResolver;
    private TestHttpCommand command;


    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

    @Before
	public void init() throws Exception {
		super.init();
		List<RescriptParamBindingDescriptor> firstOpParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
		firstOpParamBindings.add(new RescriptParamBindingDescriptor("FirstOpFirstParam", ParamSource.QUERY));
		List<RescriptParamBindingDescriptor> mapOpParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
		mapOpParamBindings.add(new RescriptParamBindingDescriptor("MapOpFirstParam", ParamSource.BODY));
		List<RescriptParamBindingDescriptor> listOpParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
		listOpParamBindings.add(new RescriptParamBindingDescriptor("ListOpFirstParam", ParamSource.BODY));
		List<RescriptParamBindingDescriptor> invalidOpParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
		invalidOpParamBindings.add(new RescriptParamBindingDescriptor("InvalidOpFirstParam", ParamSource.QUERY));
        List<RescriptParamBindingDescriptor> voidReturnOpParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
        voidReturnOpParamBindings.add(new RescriptParamBindingDescriptor("VoidReturnOpFirstParam", ParamSource.QUERY));

		operationBindings = new OperationBindingDescriptor[] {
				new RescriptOperationBindingDescriptor(firstOpKey, "/FirstTestOp", "GET", firstOpParamBindings, TestResponse.class),
				new RescriptOperationBindingDescriptor(mapOpKey, "/MapOp", "POST", mapOpParamBindings, TestResponse.class, TestBody.class),
				new RescriptOperationBindingDescriptor(listOpKey, "/ListOp", "GET", listOpParamBindings, TestResponse.class, TestBody.class),
				new RescriptOperationBindingDescriptor(invalidOpKey, "/InvalidOp", "GET", invalidOpParamBindings, TestResponse.class),
                new RescriptOperationBindingDescriptor(voidReturnOpKey, "/VoidReturnOp", "GET", voidReturnOpParamBindings, null)};

		rescriptCommandProcessor = new RescriptTransportCommandProcessor(contextResolution,"X-RequestTimeout");
		init(rescriptCommandProcessor);
		ctn = mock(ContentTypeNormaliser.class);
		when(ctn.getNormalisedRequestMediaType(any(HttpServletRequest.class))).thenReturn(MediaType.APPLICATION_XML_TYPE);
		when(ctn.getNormalisedResponseMediaType(any(HttpServletRequest.class))).thenReturn(MediaType.APPLICATION_XML_TYPE);
		when(ctn.getNormalisedEncoding(any(HttpServletRequest.class))).thenReturn("utf-8");
		rescriptCommandProcessor.setContentTypeNormaliser(ctn);

        credentialResolver = mock(RescriptIdentityTokenResolver.class);
        when(credentialResolver.resolve(any(HttpServletRequest.class), any(X509Certificate[].class))).thenReturn(new ArrayList<IdentityToken>());

        rescriptCommandProcessor.setValidatorRegistry(validatorRegistry);

        command = new TestHttpCommand(credentialResolver, Protocol.RESCRIPT);

		DataBindingFactory dbf = mock(DataBindingFactory.class);
		marshaller = mock(Marshaller.class);
		unmarshaller = mock(UnMarshaller.class);
		faultMarshaller = mock(FaultMarshaller.class);
		when(dbf.getMarshaller()).thenReturn(marshaller);
		when(dbf.getUnMarshaller()).thenReturn(unmarshaller);
		when(dbf.getFaultMarshaller()).thenReturn(faultMarshaller);
		DataBindingMap dbm = new DataBindingMap();
		dbm.setFactory(dbf);
		dbm.setPreferredContentType(MediaType.APPLICATION_XML);
		HashSet<String> contentTypes = new HashSet<String>();
		contentTypes.add(MediaType.APPLICATION_XML);
		dbm.setContentTypes(contentTypes);
		DataBindingManager.getInstance().addBindingMap(dbm);

		rescriptCommandProcessor.bind(serviceBinding);
		rescriptCommandProcessor.onCougarStart();

	}

    @Override
    protected Void isCredentialContainer() {
        return (Void) isNull();
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.RESCRIPT;
    }

    /**
	 * Basic test with string parameters
	 * @throws Exception
	 */
	@Test
	public void testProcess() throws Exception {

		// Set up the input
        command.setPathInfo("/FirstTestOp");
		when(request.getParameter("FirstOpFirstParam")).thenReturn("hello");
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		rescriptCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());

		// Assert that we resolved the expected arguments
		Object[] args = ev.getArgs();
		assertNotNull(args);
		assertEquals(1, args.length);
		assertEquals("hello", args[0]);

		// Assert that the expected result is sent
		assertNotNull(ev.getObserver());
		ev.getObserver().onResult(new ExecutionResult("goodbye"));
		assertEquals(CommandStatus.Complete, command.getStatus());
		verify(response).setContentType(MediaType.APPLICATION_XML);

		InOrder inorder = inOrder(marshaller, logger);
		inorder.verify(marshaller).marshall(any(OutputStream.class), argThat(matchesResponse("goodbye")), eq("utf-8"), eq(false));
        inorder.verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
	}


	/**
	 * Tests exceptions
	 * @throws Exception
	 */
	@Test
	public void testProcess_OnException() throws Exception {

		// Set up the input
        command.setPathInfo("/FirstTestOp");
		when(request.getParameter("FirstOpFirstParam")).thenReturn("hello");
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		rescriptCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());
		assertNotNull(ev.getObserver());

		// Assert that the expected exception is sent
		ev.getObserver().onResult(new ExecutionResult(new CougarServiceException(
					ServerFaultCode.ServiceCheckedException, "Error in App",
					new TestApplicationException(ResponseCode.Forbidden, "TestError-123",faultMessages))));
		assertEquals(CommandStatus.Complete, command.getStatus());
		verify(response).setContentType(MediaType.APPLICATION_XML);
		ArgumentCaptor<Fault> faultCaptor = ArgumentCaptor.forClass(Fault.class);
		InOrder inorder = inOrder(faultMarshaller, logger);
		inorder.verify(faultMarshaller).marshallFault(any(OutputStream.class), faultCaptor.capture(), eq("utf-8"));
        inorder.verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));
		assertNotNull(faultCaptor.getValue());
		verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
		assertEquals("TestError-123", faultCaptor.getValue().getErrorCode());
		assertEquals(FaultCode.Client, faultCaptor.getValue().getFaultCode());

        verifyTracerCalls(firstOpKey);
	}

	/**
	 * Tests a client error response is sent for invalid input
	 * @throws Exception
	 */
	@Test
	public void testProcess_InvalidInput() throws Exception {

		// Set up the input
        command.setPathInfo("/InvalidOp");
		when(request.getParameter("InvalidOpFirstParam")).thenReturn("INVALID");
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		rescriptCommandProcessor.process(command);
		assertEquals(CommandStatus.Complete, command.getStatus());
		verify(response).setContentType(MediaType.APPLICATION_XML);
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

		InOrder inorder = inOrder(faultMarshaller, logger);
		inorder.verify(faultMarshaller).marshallFault(any(OutputStream.class), any(CougarFault.class), eq("utf-8"));
        inorder.verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(null);
	}

	@Test
	public void testProcess_InvalidContentType() throws Exception {
		// Set up the input
        command.setPathInfo("/InvalidOp");
		when(request.getParameter("InvalidOpFirstParam")).thenReturn(TestEnum.TEST1.toString());
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		rescriptCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());
		assertNotNull(ev.getObserver());

		//Now get ready to send a response, but the response type is invalid
		when(ctn.getNormalisedResponseMediaType(any(HttpServletRequest.class))).thenThrow(
				new CougarValidationException(ServerFaultCode.AcceptTypeNotValid, ""));
		ev.getObserver().onResult(new ExecutionResult("something"));

		//Verify we get the correct response status
		assertEquals(CommandStatus.Complete, command.getStatus());
		verify(response).setContentType(MediaType.APPLICATION_XML);
		verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

		//And verify that the error is marshalled then logged in that order
		InOrder inorder = inOrder(faultMarshaller, logger);
		inorder.verify(faultMarshaller).marshallFault(any(OutputStream.class), any(CougarFault.class), eq("utf-8"));
        inorder.verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(invalidOpKey);

	}

    @Test
    public void testProcess_void() throws InvalidCredentialsException {
        // Set up the input
        command.setPathInfo("/VoidReturnOp");
        when(request.getParameter("VoidReturnOpFirstParam")).thenReturn(TestEnum.TEST1.toString());
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        rescriptCommandProcessor.process(command);
        assertNotNull(ev.getObserver());

        ev.getObserver().onResult(new ExecutionResult());

        verify(response).setStatus(HttpServletResponse.SC_OK);

        verifyTracerCalls(voidReturnOpKey);
    }

    @Test
    public void testProcess_voidWithException() throws IOException, InvalidCredentialsException {
        // Set up the input
        command.setPathInfo("/VoidReturnOp");
        when(request.getParameter("VoidReturnOpFirstParam")).thenReturn("INVALID");
        when(request.getScheme()).thenReturn("http");

        //Resolve / run this command
        rescriptCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        verify(response).setContentType(MediaType.APPLICATION_XML);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        InOrder inorder = inOrder(faultMarshaller, logger);
        inorder.verify(faultMarshaller).marshallFault(any(OutputStream.class), any(CougarFault.class), eq("utf-8"));
        inorder.verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(null);
    }


	@Test(expected=PanicInTheCougar.class)
    public void testBindOperation() {
        //Ensure that we don't have more than one operation bound with the same uri path

        OperationDefinition op = Mockito.mock(OperationDefinition.class);
        when(op.getParameters()).thenReturn(new Parameter[0]);

        ExecutionVenue ev = Mockito.mock(ExecutionVenue.class);
        OperationKey key = Mockito.mock(OperationKey.class);
        when(ev.getOperationDefinition(key)).thenReturn(op);

        HttpServiceBindingDescriptor serviceDescriptor = new HttpServiceBindingDescriptor() {
            private ServiceVersion serviceVersion = new ServiceVersion("v42.1");
            @Override
            public String getServiceContextPath() {
                return "/I/v1.0";
            }
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];
            }
            @Override
            public Protocol getServiceProtocol() {
                return null;
            }

            @Override
            public String getServiceName() {
                return "AnotherServiceName";
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return serviceVersion;
            }
        };
        RescriptOperationBindingDescriptor op1 = new RescriptOperationBindingDescriptor(key, "url1", "GET", Collections.<RescriptParamBindingDescriptor>emptyList(), null);
        RescriptOperationBindingDescriptor op2 = new RescriptOperationBindingDescriptor(key, "url2", "POST", Collections.<RescriptParamBindingDescriptor>emptyList(), null);

        RescriptTransportCommandProcessor sut = new RescriptTransportCommandProcessor(contextResolution, "X-RequestTimeout");
        sut.setExecutionVenue(ev);
        sut.bindOperation(serviceDescriptor, op1);
        sut.bindOperation(serviceDescriptor, op2);

        sut.bindOperation(serviceDescriptor, op1);
        fail("Duplicate url binding forbidden, an exception should have been thrown");
    }

    @Test
    public void testSameOperationDifferentContextPaths() {
        // Ensure that allow more two of the same operation if the  are different

        OperationDefinition op = Mockito.mock(OperationDefinition.class);
        when(op.getParameters()).thenReturn(new Parameter[0]);

        ExecutionVenue ev = Mockito.mock(ExecutionVenue.class);
        OperationKey key = Mockito.mock(OperationKey.class);
        when(ev.getOperationDefinition(key)).thenReturn(op);

        HttpServiceBindingDescriptor serviceDescriptorV1 = new HttpServiceBindingDescriptor() {
            @Override
            public String getServiceContextPath() {
                return "/I/v1.0";
            }
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];
            }
            @Override
            public Protocol getServiceProtocol() {
                return null;
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion(1,0);
            }

            @Override
            public String getServiceName() {
                return "Wibble";
            }
        };
        HttpServiceBindingDescriptor serviceDescriptorV2 = new HttpServiceBindingDescriptor() {
            @Override
            public String getServiceContextPath() {
                return "/I/v2.0";
            }
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];
            }
            @Override
            public Protocol getServiceProtocol() {
                return null;
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion(2,0);
            }

            @Override
            public String getServiceName() {
                return "Wibble";
            }
        };
        RescriptOperationBindingDescriptor op1 = new RescriptOperationBindingDescriptor(key, "url1", "GET", Collections.<RescriptParamBindingDescriptor>emptyList(), null);
        RescriptTransportCommandProcessor sut = new RescriptTransportCommandProcessor(contextResolution, null);
        sut.setExecutionVenue(ev);
        sut.bindOperation(serviceDescriptorV1, op1);
        sut.bindOperation(serviceDescriptorV2, op1);
    }

    @Test
    public void createCommandResolver_NoTimeout() {
        // Set up the input
        command.setPathInfo("/FirstTestOp");
        when(request.getParameter("FirstOpFirstParam")).thenReturn("hello");
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        CommandResolver<HttpCommand> cr = rescriptCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertNull(constraints.getExpiryTime());
    }

    @Test
    public void createCommandResolver_WithTimeout() {
        // Set up the input
        command.setPathInfo("/FirstTestOp");
        when(request.getParameter("FirstOpFirstParam")).thenReturn("hello");
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        when(request.getHeader("X-RequestTimeout")).thenReturn("10000");
        when(context.getRequestTime()).thenReturn(new Date());
        CommandResolver<HttpCommand> cr = rescriptCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertNotNull(constraints.getExpiryTime());
    }

    @Test
    public void createCommandResolver_WithTimeoutAndOldRequestTime() {
        // Set up the input
        command.setPathInfo("/FirstTestOp");
        when(request.getParameter("FirstOpFirstParam")).thenReturn("hello");
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        when(request.getHeader("X-RequestTimeout")).thenReturn("10000");
        when(context.getRequestTime()).thenReturn(new Date(System.currentTimeMillis()-10001));
        CommandResolver<HttpCommand> cr = rescriptCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertTrue(constraints.getExpiryTime() < System.currentTimeMillis());
    }

	private ArgumentMatcher<RescriptResponse> matchesResponse(final Object responseValue) {
		return new ArgumentMatcher<RescriptResponse>() {
			@Override
			public boolean matches(Object argument) {
                assertTrue(argument instanceof RescriptResponse);
                assertEquals(responseValue, ((RescriptResponse)argument).getResult());
				return true;
			}
		};
	}

	public static class TestResponse implements RescriptResponse {
		private Object result;

		@Override
		public Object getResult() {
			return result;
		}

		@Override
		public void setResult(Object result) {
			this.result = result;
		}
	}

	public static class TestBody implements RescriptBody {

		private HashMap<String, Object> map = new  HashMap<String, Object>();

		@Override
		public Object getValue(String name) {
			return map.get(name);
		}

		public void put(String name, Object value) {
			map.put(name, value);
		}

	}

}
