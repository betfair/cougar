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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.impl.CommandValidatorRegistry;
import com.betfair.cougar.transport.impl.protocol.http.rescript.RescriptOperationBindingTest;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import com.betfair.cougar.util.geolocation.SuspectNetworkList;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AbstractHttpCommandProcessorTest {
    private static final String AZ = "Azerbaijan";

    private static final String SERVICE_PATH = "/myservice/v1.0";
	protected static final OperationKey firstOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "FirstTestOp");
	protected static final Parameter[] firstOpParams = new Parameter[] { new Parameter(
			"FirstOpFirstParam", ParameterType.create(String.class, null),
			false) };
	protected static final ParameterType firstOpReturn = ParameterType.create(String.class,
			null);
	
	protected static final OperationKey mapOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "MapTestOp");
	protected static final Parameter[] mapOpParams = new Parameter[] { new Parameter(
			"MapOpFirstParam", ParameterType.create(HashMap.class, new Class[] {Integer.class, Double.class}),
			false) };
	protected static final ParameterType mapOpReturn = ParameterType.create(HashMap.class, new Class[] {Integer.class, Double.class});
	
	protected static final OperationKey listOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "ListTestOp");
	protected static final Parameter[] listOpParams = new Parameter[] { new Parameter(
			"ListOpFirstParam", ParameterType.create(List.class, new Class[] {Date.class}),
			false) };
	protected static final ParameterType listOpReturn = ParameterType.create(List.class, new Class[] {Date.class});

	protected static final OperationKey invalidOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "InvalidTestOp");
	protected static final Parameter[] invalidOpParams = new Parameter[] { new Parameter(
			"InvalidOpFirstParam", ParameterType.create(TestEnum.class, null),
			false) };
	protected static final ParameterType invalidOpReturn = ParameterType.create(TestEnum.class, null);

    protected static final OperationKey voidReturnOpKey = new OperationKey( new ServiceVersion(2, 1), "HTTPTest", "VoidReturnTestOp");
    protected static final Parameter[] voidReturnOpParams = new Parameter[] { new Parameter("VoidReturnOpFirstParam", ParameterType.create(TestEnum.class, null), true)};

	public static enum TestEnum {
		TEST1,
		TEST2,
        UNRECOGNIZED_VALUE
	}

    protected XMLTestCase xmlTestCase = new XMLTestCase();
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected TestServletOutputStream testOut;
	protected List<String[]> faultMessages;
	protected TestEV ev;
	protected RequestLogger logger;
	protected GeoIPLocator geoIPLocator;
	protected SuspectNetworkList suspectNetworks;
    protected CommandValidatorRegistry<HttpCommand> validatorRegistry = new CommandValidatorRegistry<HttpCommand>();
	
	protected LocalCommandProcessor commandProcessor;

    @BeforeClass
    public static void suppressLogging() {
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

	@Before
	public void init() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());

        logger = mock(RequestLogger.class);
		geoIPLocator = mock(GeoIPLocator.class);
		
		request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(SERVICE_PATH);
        when(request.getHeaderNames()).thenReturn(RescriptOperationBindingTest.enumerator(new ArrayList<String>().iterator()));


		response = mock(HttpServletResponse.class);
		testOut = new TestServletOutputStream();
		when(response.getOutputStream()).thenReturn(testOut);
		ev = new TestEV();
		ev.registerOperation(null, new SimpleOperationDefinition(firstOpKey, firstOpParams,firstOpReturn), null, null, 0);
		ev.registerOperation(null, new SimpleOperationDefinition(mapOpKey, mapOpParams, mapOpReturn), null, null, 0);
		ev.registerOperation(null, new SimpleOperationDefinition(listOpKey, listOpParams, listOpReturn), null, null, 0);
		ev.registerOperation(null, new SimpleOperationDefinition(invalidOpKey, invalidOpParams, invalidOpReturn), null, null, 0);
        ev.registerOperation(null, new SimpleOperationDefinition(voidReturnOpKey, voidReturnOpParams, null), null, null, 0);
        commandProcessor = new LocalCommandProcessor();
		init(commandProcessor);
	}

    @Test(expected = PanicInTheCougar.class)
    public void testMultipleServiceBindSameVersion() {
        final ServiceVersion sv = new ServiceVersion("v3.2");
        final String serviceName = "testServiceName";

        ServiceBindingDescriptor sbd = new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return sv;
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        commandProcessor.bind(sbd);
        commandProcessor.bind(sbd);
    }

    @Test(expected = PanicInTheCougar.class)
    public void testMultipleServiceBindingSameMajorVersion() {
        final String serviceName = "testServiceName";

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v3.2");
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v3.3");
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });
    }

    @Test
    public void testMultipleServiceBindingDifferentMajorVersion() {
        final String serviceName = "testServiceName";

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v1.2");
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v3.3");
            }

            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });

        int count=0;
        for (ServiceBindingDescriptor sbd : commandProcessor.getServiceBindingDescriptors()) count++;
        assertEquals(2, count);
    }

    @Test
    public void testMultipleServiceBindingDifferentService() {
        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v1.2");
            }

            @Override
            public String getServiceName() {
                return "service1";
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });

        commandProcessor.bind(new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v3.3");
            }

            @Override
            public String getServiceName() {
                return "service2";
            }

            @Override
            public Protocol getServiceProtocol() {
                return null;
            }
        });

        int count=0;
        for (ServiceBindingDescriptor sbd : commandProcessor.getServiceBindingDescriptors()) count++;
        assertEquals(2, count);
    }

    @Test
    public void testUriStrip() {
        String[][] textMatrix = {
           { "/service/v1.2",                           "/service/v1"},
           { "/service/V1.2",                           "/service/V1"},
           { "/service/v1.0/foo",                       "/service/v1/foo" },
           { "/service/v1/foo",                         "/service/v1/foo" },
           { "/service/v20.3/foo",                      "/service/v20/foo"},
           { "/v1/foo",                                 "/v1/foo"},
           { "/v20.3/foo",                              "/v20/foo"},
           { "/service/v1.3/foo?action=add&sky=blue",   "/service/v1/foo?action=add&sky=blue"}
        };

        for (String[] pair : textMatrix) {
            assertEquals(pair[1], commandProcessor.stripMinorVersionFromUri(pair[0]));
        }
    }

	@Test
	public void testResolveExecutionContext() throws Exception {
        HttpCommand command = new TestHttpCommand(null, null);
        when(request.getScheme()).thenReturn("http");
        GeoLocationDetails gld = Mockito.mock(GeoLocationDetails.class);
		//test an empty request
        List ipAddresses =  Collections.emptyList();
        when(geoIPLocator.getGeoLocation(null, ipAddresses, AZ)).thenReturn(gld);
		ExecutionContext context = commandProcessor.resolveExecutionContext(command, null, null);
		assertNotNull(context);
		assertNotNull(context.getRequestUUID());
		assertNotNull(context.getReceivedTime());
		assertNotNull(context.getLocation());

		//Test request contains uuid, id and remote address
		RequestUUIDImpl uuid = new RequestUUIDImpl();
		when(request.getHeader("X-UUID")).thenReturn(uuid.toString());
		when(request.getRemoteAddr()).thenReturn("1.2.3.4");
		when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("1.2.3.4", null), AZ)).thenReturn(gld);
		context = commandProcessor.resolveExecutionContext(command, null, null);
		assertNotNull(context);
		assertEquals(uuid, context.getRequestUUID());
		assertEquals(gld, context.getLocation());

		//Test request contains X-Forwarded-For header and resolves geo-location correctly
		when(request.getHeader("X-Forwarded-For")).thenReturn("10.20.30.40");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("10.20.30.40", null), AZ)).thenReturn(gld);
		context = commandProcessor.resolveExecutionContext(command, null, null);
		assertNotNull(context);
        assertEquals(gld, context.getLocation());
	}

    @Test
    public void testResolveExecutionContextWithoutCountryResolver() throws Exception {
        HttpCommand command = new TestHttpCommand(null, null);
        when(request.getScheme()).thenReturn("http");
        GeoLocationDetails gld = Mockito.mock(GeoLocationDetails.class);
        List ipAddresses = Collections.emptyList();
        when(geoIPLocator.getGeoLocation(null, ipAddresses, null)).thenReturn(gld);
        AbstractHttpCommandProcessor underTest = new AbstractHttpCommandProcessor(geoIPLocator, new DefaultGeoLocationDeserializer(), "X-UUID","X-RequestTimeout",new DontCareRequestTimeResolver()) {
            protected CommandResolver<HttpCommand> createCommandResolver(HttpCommand command) {return null;}
            protected void writeErrorResponse(HttpCommand command, ExecutionContextWithTokens context, CougarException e) {}
            public void onCougarStart() {}
        };
        ExecutionContext context = underTest.resolveExecutionContext(command, null, null);
        assertNotNull(context);
        assertNotNull(context.getLocation());
        assertNull(context.getLocation().getInferredCountry());

		//Test request contains uuid, id and remote address
		RequestUUIDImpl uuid = new RequestUUIDImpl();
		when(request.getHeader("X-UUID")).thenReturn(uuid.toString());
		when(request.getRemoteAddr()).thenReturn("1.2.3.4");
		when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("1.2.3.4", null), null)).thenReturn(gld);
		context = underTest.resolveExecutionContext(command, null, null);
		assertNotNull(context);
		assertEquals(uuid, context.getRequestUUID());
		assertEquals(gld, context.getLocation());
        assertNull(context.getLocation().getInferredCountry());

		//Test request contains X-Forwarded-For header and resolves geo-location correctly
		when(request.getHeader("X-Forwarded-For")).thenReturn("10.20.30.40");
        when(geoIPLocator.getGeoLocation("1.2.3.4", RemoteAddressUtils.parse("10.20.30.40", null), null)).thenReturn(gld);
		context = underTest.resolveExecutionContext(command, null, null);
		assertNotNull(context);
        assertEquals(gld, context.getLocation());
        assertNull(context.getLocation().getInferredCountry());
    }

	@Test
	public void testCallsValidators() throws Exception {
        HttpCommand command = new TestHttpCommand(null, null);
        CommandValidator<HttpCommand> validator = mock(CommandValidator.class);
        validatorRegistry.addValidator(validator);
        commandProcessor.process(command);
        assertFalse(commandProcessor.errorCalled());
        verify(validator).validate(any(HttpCommand.class));
	}

	@Test
	public void testStopsOnValidatorFail() throws Exception {
        HttpCommand command = new TestHttpCommand(null, null);
        CommandValidator<HttpCommand> validator = new CommandValidator<HttpCommand>() {
            @Override
            public void validate(HttpCommand command) throws CougarException {
                throw new CougarServiceException(ServerFaultCode.SecurityException, "wibble");
            }
        };
        validatorRegistry.addValidator(validator);
        commandProcessor.process(command);
        assertTrue(commandProcessor.errorCalled());
	}
	
	protected void init(AbstractHttpCommandProcessor commandProcessor) throws Exception {
		commandProcessor.setExecutor(new Executor() {
            @Override
            public void execute(Runnable runnable) {
                runnable.run();
            }
        });
		commandProcessor.setExecutionVenue(ev);
		commandProcessor.setRequestLogger(logger);
        commandProcessor.setValidatorRegistry(validatorRegistry);
        commandProcessor.setHardFailEnumDeserialisation(true);
	}
	
	protected class TestEV implements ExecutionVenue {
		
		private ExecutionObserver observer;
		private Object[] args;
		private OperationKey key;
		private HashMap<OperationKey, OperationDefinition> map = new HashMap<OperationKey, OperationDefinition>();
		private int invokedCount = 0;
		
		public Object[] getArgs() {
			return args;
		}

		public OperationKey getKey() {
			return key;
		}

		public ExecutionObserver getObserver() {
			return observer;
		}
		
		public int getInvokedCount() {
			return invokedCount;
		}

		@Override
		public void execute(ExecutionContext ctx, OperationKey key,
				Object[] args, ExecutionObserver observer, TimeConstraints clientExpiryTime) {
			invokedCount++;
			this.key = key;
			this.args = args;
			this.observer = observer;
		}

        @Override
        public void execute(final ExecutionContext ctx, final OperationKey key, final Object[] args, final ExecutionObserver observer, final Executor executor, final TimeConstraints clientExpiryTime) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    execute(ctx, key, args, observer, clientExpiryTime);
                }
            });
        }

        @Override
        public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder, long max) {
            map.put(def.getOperationKey(), def);
        }

        @Override
		public OperationDefinition getOperationDefinition(OperationKey key) {
			return map.get(key);
		}

		@Override
		public Set<OperationKey> getOperationKeys() {
			return map.keySet();
		}

		@Override
		public void setPostProcessors(
				List<ExecutionPostProcessor> preProcessorList) {
		}

		@Override
		public void setPreProcessors(
				List<ExecutionPreProcessor> preProcessorList) {
		}
		
	}
	
	protected class TestServletInputStream extends ServletInputStream {

		private String input;
		private int pos = 0;

		public TestServletInputStream(String input) {
			this.input = input;
		}

		@Override
		public int read() throws IOException {
			if (pos < input.length()) {
				return input.charAt(pos++);
			}
			return -1;
		}
	};

	protected class TestServletOutputStream extends ServletOutputStream {

		private StringBuffer output = new StringBuffer();

		@Override
		public void write(int character) throws IOException {
			output.append((char) character);
		}

		public String getOutput() {
			return output.toString();
		}
	}

    protected HttpCommand createCommand(IdentityTokenResolver identityTokenResolver, Protocol protocol) {
        return new TestHttpCommand(identityTokenResolver, protocol);
    }

	protected class TestHttpCommand implements HttpCommand {

		private CommandStatus commandStatus = CommandStatus.InProcess;
		private RequestTimer timer = new RequestTimer();
        private IdentityTokenResolver identityTokenResolver;
        private String pathInfo = "/test";
        private Protocol protocol;

        public TestHttpCommand(IdentityTokenResolver identityTokenResolver, Protocol protocol) {
            this.identityTokenResolver = identityTokenResolver;
            this.protocol = protocol;
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
            return this.identityTokenResolver;
        }

        @Override
		public CommandStatus getStatus() {
			return commandStatus;
		}

		@Override
		public void onComplete() {
			commandStatus = CommandStatus.Complete;
		}

		@Override
		public RequestTimer getTimer() {
			return timer;
		}

		@Override
        public String getFullPath() {
	        return "/foo"+getOperationPath();
        }

		@Override
        public String getOperationPath() {
            if (protocol == Protocol.SOAP) {
                return SERVICE_PATH;
            } else {
	            return SERVICE_PATH+pathInfo;
            }
        }
        @Override
        public X509Certificate[] getClientX509CertificateChain() {
            return null;
        }

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    };
	
	protected class TestApplicationException extends CougarApplicationException {

		public TestApplicationException(ResponseCode code, String message) {
			super(code, message);
		}

		@Override
		public List<String[]> getApplicationFaultMessages() {
			return faultMessages;
		}

		@Override
		public String getApplicationFaultNamespace() {
			return null;
		}
		
	}

    private class LocalCommandProcessor extends AbstractHttpCommandProcessor {
        private boolean errorCalled;

        private LocalCommandProcessor() {
            super(geoIPLocator, new DefaultGeoLocationDeserializer(), "X-UUID", new InferredCountryResolver<HttpServletRequest>() {
                public String inferCountry(HttpServletRequest input) { return AZ; }
            },"X-RequestTimeout",new DontCareRequestTimeResolver());
        }

        @Override
        protected CommandResolver<HttpCommand> createCommandResolver(final HttpCommand command) {
            return new CommandResolver<HttpCommand>() {
                @Override
                public ExecutionContextWithTokens resolveExecutionContext() {
                    return null;
                }

                @Override
                public Iterable<ExecutionCommand> resolveExecutionCommands() {
                    List commands = Arrays.asList(new ExecutionCommand() {
                        @Override
                        public OperationKey getOperationKey() {
                            return null;
                        }

                        @Override
                        public Object[] getArgs() {
                            return new Object[0];
                        }

                        @Override
                        public void onResult(ExecutionResult executionResult) {
                        }

                        @Override
                        public TimeConstraints getTimeConstraints() {
                            return DefaultTimeConstraints.NO_CONSTRAINTS;
                        }
                    });
                    return commands;
                }
            };
        }

        @Override
        protected void writeErrorResponse(HttpCommand command, ExecutionContextWithTokens context, CougarException e) {
            errorCalled = true;
        }

        @Override
        public void onCougarStart() {
        }

        private boolean errorCalled() {
            return errorCalled;
        }
    }
}
