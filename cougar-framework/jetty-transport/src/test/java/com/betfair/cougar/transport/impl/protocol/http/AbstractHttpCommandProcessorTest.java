/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.RequestTimer;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.transport.api.*;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.impl.CommandValidatorRegistry;
import com.betfair.cougar.transport.impl.protocol.http.rescript.RescriptOperationBindingTest;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.custommonkey.xmlunit.XMLTestCase;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractHttpCommandProcessorTest<CredentialContainer> {

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
			"MapOpFirstParam", ParameterType.create(HashMap.class, Integer.class, Double.class),
			false) };
	protected static final ParameterType mapOpReturn = ParameterType.create(HashMap.class, Integer.class, Double.class);

	protected static final OperationKey listOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "ListTestOp");
	protected static final Parameter[] listOpParams = new Parameter[] { new Parameter(
			"ListOpFirstParam", ParameterType.create(List.class, Date.class),
			false) };
	protected static final ParameterType listOpReturn = ParameterType.create(List.class, Date.class);

	protected static final OperationKey invalidOpKey = new OperationKey(
			new ServiceVersion(2, 1), "HTTPTest", "InvalidTestOp");
	protected static final Parameter[] invalidOpParams = new Parameter[] { new Parameter(
			"InvalidOpFirstParam", ParameterType.create(TestEnum.class, null),
			false) };
	protected static final ParameterType invalidOpReturn = ParameterType.create(TestEnum.class, null);

    protected static final OperationKey voidReturnOpKey = new OperationKey( new ServiceVersion(2, 1), "HTTPTest", "VoidReturnTestOp");
    protected static final Parameter[] voidReturnOpParams = new Parameter[] { new Parameter("VoidReturnOpFirstParam", ParameterType.create(TestEnum.class, null), true)};
    protected DehydratedExecutionContext context;

    public static enum TestEnum {
		TEST1
//        ,TEST2
//        ,UNRECOGNIZED_VALUE
	}

    protected XMLTestCase xmlTestCase = new XMLTestCase();
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected TestServletOutputStream testOut;
	protected List<String[]> faultMessages;
	protected TestEV ev;
	protected RequestLogger logger;
    protected Tracer tracer;
    protected CommandValidatorRegistry<HttpCommand> validatorRegistry = new CommandValidatorRegistry<>();
    protected DehydratedExecutionContextResolution contextResolution;
    protected LocalCommandProcessor commandProcessor;
    protected Protocol protocol;

    @BeforeClass
    public static void suppressLogging() {
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

	@Before
	public void init() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());

        logger = mock(RequestLogger.class);
        tracer = mock(Tracer.class);

        contextResolution = mock(DehydratedExecutionContextResolution.class);
        context = mock(DehydratedExecutionContext.class);
        when(contextResolution.resolveExecutionContext(eq(getProtocol()),any(HttpCommand.class),isCredentialContainer())).thenReturn(context);
        RequestUUID uuid = new RequestUUIDImpl();
        when(context.getRequestUUID()).thenReturn(uuid);

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

    protected abstract CredentialContainer isCredentialContainer();

    protected abstract Protocol getProtocol();

    protected void verifyTracerCalls(OperationKey expected) {
        final ArgumentCaptor<RequestUUID> captor = ArgumentCaptor.forClass(RequestUUID.class);
        final ArgumentCaptor<OperationKey> opKeyCaptor = ArgumentCaptor.forClass(OperationKey.class);

        if (expected != null) {
            InOrder inOrder = inOrder(tracer);
            inOrder.verify(tracer).start(captor.capture(), opKeyCaptor.capture());
            inOrder.verify(tracer).end(argThat(new BaseMatcher<RequestUUID>() {
                @Override
                public boolean matches(Object o) {
                    return o.equals(captor.getValue());
                }

                @Override
                public void describeTo(Description description) {
                }
            }));
        }
    }

    @Test(expected = PanicInTheCougar.class)
    public void testMultipleServiceBindSameVersion() {
        final ServiceVersion sv = new ServiceVersion("v3.2");
        final String serviceName = "testServiceName";

        ServiceBindingDescriptor sbd = new ServiceBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[0];
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
                return getProtocol();
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
        for (ServiceBindingDescriptor ignored : commandProcessor.getServiceBindingDescriptors()) {
            count++;
        }
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
        for (ServiceBindingDescriptor ignored : commandProcessor.getServiceBindingDescriptors()) {
            count++;
        }
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
	public void testCallsValidators() throws Exception {
        HttpCommand command = new TestHttpCommand(null, null);
        //noinspection unchecked
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
        //noinspection NullableProblems
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
        commandProcessor.setTracer(tracer);
	}

	protected class TestEV implements ExecutionVenue {

		private ExecutionObserver observer;
		private Object[] args;
		private HashMap<OperationKey, OperationDefinition> map = new HashMap<>();
		private int invokedCount = 0;

		public Object[] getArgs() {
			return args;
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

        @Override
        public boolean isFinished() {
            return (pos >= input.length());
        }

        @Override
        public boolean isReady() {
            return (pos < input.length());
        }

        @Override
        public void setReadListener(ReadListener readListener) {
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

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }

    protected HttpCommand createCommand(IdentityTokenResolver identityTokenResolver, Protocol protocol) {
        return new TestHttpCommand(identityTokenResolver, protocol);
    }

	protected class TestHttpCommand implements HttpCommand {

		private CommandStatus commandStatus = CommandStatus.InProgress;
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

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    };

	protected static class TestApplicationException extends CougarApplicationException {

        private final List<String[]> faultMessages;

        public TestApplicationException(ResponseCode code, String message, List<String[]> faultMessages) {
			super(code, message);
            this.faultMessages = faultMessages;
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

    private class LocalCommandProcessor extends AbstractHttpCommandProcessor<Void> {
        private boolean errorCalled;

        private LocalCommandProcessor() {
            super(Protocol.RESCRIPT,contextResolution,"X-RequestTimeout");
        }

        @Override
        protected CommandResolver<HttpCommand> createCommandResolver(final HttpCommand command, Tracer tracer) {
            return new CommandResolver<HttpCommand>() {
                @Override
                public DehydratedExecutionContext resolveExecutionContext() {
                    return context;
                }

                @Override
                public List<ExecutionCommand> resolveExecutionCommands() {
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
                    //noinspection unchecked
                    return commands;
                }
            };
        }

        @Override
        protected void writeErrorResponse(HttpCommand command, DehydratedExecutionContext context, CougarException e, boolean traceStarted) {
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
