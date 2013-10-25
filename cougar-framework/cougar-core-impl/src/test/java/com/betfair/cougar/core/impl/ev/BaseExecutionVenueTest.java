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

package com.betfair.cougar.core.impl.ev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.api.security.CredentialFaultCode;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.InvalidCredentialsException;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.logging.EventLogger;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.logging.RequestLogEvent;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseExecutionVenueTest {

	private static final InterceptorResult CONTINUE = new InterceptorResult(InterceptorState.CONTINUE, null);
	private static final InterceptorResult FORCE_ON_RESULT = new InterceptorResult(InterceptorState.FORCE_ON_RESULT, "THIS IS A FORCED PASS");
	private static final InterceptorResult FORCE_ON_EXCEPTION = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new CougarServiceException(ServerFaultCode.SecurityException,"I EXPECT TO FAIL"));

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

    private class EVTestException extends CougarApplicationException {

        public EVTestException(ResponseCode code, String exceptionCode) {
            super(code, exceptionCode);
        }

        @Override
        public List<String[]> getApplicationFaultMessages() {
            return null;
        }

        @Override
        public String getApplicationFaultNamespace() {
            return null;
        }
    }

    private ExecutionPreProcessor checkedExceptionThrowingPreProcessor = new ExecutionPreProcessor() {
        @Override
        public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
            return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                    new EVTestException(ResponseCode.Forbidden, "This is what we wanted"));
        }

        @Override
        public String getName() {
            return "checkedExceptionThrowingPreProcessor";
        }
    };

	/** 
	 * PRE-PROCESSORS
	 */
	private ExecutionPreProcessor exceptionThrowingPreProcessor = new ExecutionPreProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
			throw new UnsupportedOperationException("This is expected");
		}

		@Override
		public String getName() {
			return "exceptionThrowingPreProcessor";
		}
	};

	private ExecutionPreProcessor continuePreProcessor = new ExecutionPreProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
			return CONTINUE;
		}

		@Override
		public String getName() {
			return "continuePreProcessor";
		}
	};

	private ExecutionPreProcessor forceOnResultPreProcessor = new ExecutionPreProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
			return FORCE_ON_RESULT;
		}

		@Override
		public String getName() {
			return "forceOnResultPreProcessor";
		}
	};

	/**
	 * POST-PROCESSORS
	 */
	
	private ExecutionPostProcessor exceptionThrowingPostProcessor = new ExecutionPostProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionResult result) {
			throw new UnsupportedOperationException("This is expected");
		}
		@Override
		public String getName() {
			return "exceptionThrowingPostProcessor";
		}
	};

    private ExecutionPostProcessor checkedServiceExceptionThrowingPostProcessor = new ExecutionPostProcessor() {
        @Override
        public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionResult result) {
            return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                    new EVTestException(ResponseCode.Forbidden, "This is what we wanted"));
        }
        @Override
        public String getName() {
            return "checkedServiceExceptionThrowingPostProcessor";
        }
    };

	private ExecutionPostProcessor forceOnResultPostProcessor = new ExecutionPostProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionResult result) {
			return FORCE_ON_RESULT;
		}
		@Override
		public String getName() {
			return "forceOnResultPostProcessor";
		}
	};

	private ExecutionPostProcessor forceOnExceptionMockPostProcessor = new ExecutionPostProcessor() {
		@Override
		public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionResult result) {
			return FORCE_ON_EXCEPTION;
		}
		@Override
		public String getName() {
			return "forceOnExceptionMockPostProcessor";
		}
	};
	/**
	 * EXECUTION-OBSERVERS
	 */
	
	private ExecutionObserver failOnResultExecutionObserver = new ExecutionObserver() {
		public void onResult(ExecutionResult result) {
            switch (result.getResultType()) {
                case Success:
                case Subscription:
        			fail("Expected ExecutionResult of Fault");
                    break;
                case Fault:
                    break;
            }
		}
    };


    private ExecutionObserver cougarApplicationExceptionResultExecutionObserver = new ExecutionObserver() {
        public void onResult(ExecutionResult result) {
            switch (result.getResultType()) {
                case Success:
                case Subscription:
                    fail("Expected ExecutionResult of Fault");
                    break;
                case Fault:
                    //This is what we want, but we must confirm that it is possible to correctly construct the CAE
                    String className = result.getFault().getFault().getDetail().getDetailMessage();
                    assertEquals("EVTestException", className);
                    break;
            }
        }
    };


	private ExecutionObserver failOnExceptionExecutionObserver = new ExecutionObserver() {
        public void onResult(ExecutionResult result) {
            switch (result.getResultType()) {
                case Fault:
                case Subscription:
                    fail("Expected ExecutionResult of Success");
                    break;
                case Success:
                    break;
            }
        }
    };

	
	private Executable failingExecutable = new Executable() {
		@Override
		public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue) {
			throw new IllegalStateException("Failure can now be an option!");
		}
	};


	private Executable succeedingExecutable = new Executable() {
		@Override
		public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue) {
			observer.onResult(new ExecutionResult(null));
		}
	};

    private IdentityResolver newIdentityResolver = new IdentityResolver() {
        @Override
        public void resolve(IdentityChain chain, ExecutionContextWithTokens ctx) throws InvalidCredentialsException {
        }

        @Override
        public List<IdentityToken> tokenise(IdentityChain chain) {
            return null;
        }
    };

    private IdentityResolver newIdentityResolverWithToken = new IdentityResolver() {
        @Override
        public void resolve(IdentityChain chain, ExecutionContextWithTokens ctx) throws InvalidCredentialsException {
        }

        @Override
        public List<IdentityToken> tokenise(IdentityChain chain) {
            return Arrays.asList(new IdentityToken("Key", "Value"));
        }
    };

    private IdentityResolver failingGenericIdentityResolver(CredentialFaultCode cfc) {
        final InvalidCredentialsException ice = cfc != null ? new InvalidCredentialsException("", cfc) : new InvalidCredentialsException("");
        return  new IdentityResolver() {
            @Override
            public void resolve(IdentityChain chain, ExecutionContextWithTokens ctx) throws InvalidCredentialsException {
                throw ice;
            }

            @Override
            public List<IdentityToken> tokenise(IdentityChain chain) {
                return null;
            }
        };
    }
	
	private BaseExecutionVenue bev = new BaseExecutionVenue();
	private List<ExecutionPreProcessor> preProcessorList = new ArrayList<ExecutionPreProcessor>();
	private List<ExecutionPostProcessor> postProcessorList = new ArrayList<ExecutionPostProcessor>();
	private ExecutionContextWithTokens mockExecutionContext = mock(ExecutionContextWithTokens.class);
	private OperationKey mockOperationKey;
	private Object[] args = new Object[0];
	private OperationDefinition mockOperationDef;
//	private ServiceLogManager mockServiceLogManager;
	private ExecutionTimingRecorder mockTimingRecorder;
	private Executable mockExecutable;
	private EventLogger eventLogger;
	private RequestUUID uuid = new RequestUUIDImpl();
	
	@Before
	public void setup() {
		bev.setPreProcessors(preProcessorList);
		bev.setPostProcessors(postProcessorList);
		preProcessorList.clear();
		postProcessorList.clear();

        mockOperationKey = new OperationKey(new ServiceVersion(1,0), "SomeService", "someOperation");
		mockOperationDef = new SimpleOperationDefinition(mockOperationKey, new Parameter[0], new ParameterType(Void.class, new ParameterType[0]));
		mockExecutable = mock(Executable.class);
//		mockServiceLogManager = mock(ServiceLogManager.class);
        mockTimingRecorder = mock(ExecutionTimingRecorder.class);

		eventLogger = mock(EventLogger.class);
		bev.setEventLogger(eventLogger);

		when(mockExecutionContext.getRequestUUID()).thenReturn(uuid);
	}

	@Test
	public void testForceOnExceptionForPostProcessorWhenExecutablePasses() {
		postProcessorList.add(forceOnExceptionMockPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver);
	}
	
	@Test
	public void testOnExceptionCalledWhenPreProcessorFails() {
		preProcessorList.add(exceptionThrowingPreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver);
	}

    @Test
    public void testOnExceptionWithServiceCheckedException() {
        preProcessorList.add(checkedExceptionThrowingPreProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.execute(mockExecutionContext, mockOperationKey, args, cougarApplicationExceptionResultExecutionObserver);
    }
	
	@Test
	public void testOnResultCalledWhenPreProcessorPasses() {
		preProcessorList.add(continuePreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver);
	}

	@Test
	public void testExecutableNotCalledWhenPreProcessorSaySo() {
		preProcessorList.add(forceOnResultPreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver);
	}
	
	@Test
	public void testFailingPostProcessorCallsOnExceptionWhenExecutableCompletesOK() {
		postProcessorList.add(exceptionThrowingPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver);
	}

    @Test
    public void testFailingPostProcessorCallsOnServiceCheckedExceptionWhenExecutableCompletesOK() {
        postProcessorList.add(checkedServiceExceptionThrowingPostProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.execute(mockExecutionContext, mockOperationKey, args, cougarApplicationExceptionResultExecutionObserver);
    }

	@Test
	public void testSucceedingPostProcessorCallsOnResultWhenExecutableCompletesOK() {
		postProcessorList.add(forceOnResultPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver);
	}
	
	@Test
	public void testPostProcessorForcesOnResultWhenExcecutableFails() {
		postProcessorList.add(forceOnResultPostProcessor);
		bev.registerOperation(null, mockOperationDef, failingExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver);
	}
	
	@Test
	public void testPostProcessorForcesOnExceptionWhenExcecutableFails() {
		postProcessorList.add(forceOnExceptionMockPostProcessor);
		bev.registerOperation(null, mockOperationDef, failingExecutable, mockTimingRecorder);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver);
	}
	
	@Test
	public void testNoOperation() {
		ExecutionObserver observer = mock(ExecutionObserver.class);
		
		bev.execute(mockExecutionContext, mockOperationKey, args, observer);
		
		ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
		verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

		assertEquals(ServerFaultCode.NoSuchOperation, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());

	}

    @Test
    public void testNamepacedServiceNotSpecifiedInCallFail() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation("MyNamespace", mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.execute(mockExecutionContext, mockOperationKey, args, observer);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

        assertEquals(ServerFaultCode.NoSuchOperation, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void testServiceNamespaceSpecifiedInCallFail() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.execute(mockExecutionContext, new OperationKey(mockOperationKey, "MyNamespace"), args, observer);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

        assertEquals(ServerFaultCode.NoSuchOperation, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void testSameOperationMultiRegisteredWithDifferentNamspaces() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        final ExecutionTimingRecorder mockTimingRecorderFoo = mock(ExecutionTimingRecorder.class);
        final Executable mockExecutableFoo = mock(Executable.class);

        final ExecutionTimingRecorder mockTimingRecorderBar = mock(ExecutionTimingRecorder.class);
        final Executable mockExecutableBar = mock(Executable.class);

        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.registerOperation("foo", mockOperationDef, mockExecutableFoo, mockTimingRecorderFoo);
        bev.registerOperation("bar", mockOperationDef, mockExecutableBar, mockTimingRecorderBar);


        // Test no namespace
        bev.execute(mockExecutionContext, mockOperationKey, args, observer);
        verify(mockExecutable).execute(any(ExecutionContext.class), eq(mockOperationKey), any(Object[].class), any(ExecutionObserver.class), eq(bev));

        // Test foo
        OperationKey key = new OperationKey(mockOperationKey, "foo");
        bev.execute(mockExecutionContext, key, args, observer);
        verify(mockExecutableFoo).execute(any(ExecutionContext.class), eq(key), any(Object[].class), any(ExecutionObserver.class), eq(bev));

        // Test bar
        OperationKey barKey = new OperationKey(mockOperationKey, "bar");
        bev.execute(mockExecutionContext, barKey, args, observer);
        verify(mockExecutableBar).execute(any(ExecutionContext.class), eq(barKey), any(Object[].class), any(ExecutionObserver.class), eq(bev));
    }

    @Test
    public void testNewIdentityResolution() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, succeedingExecutable, mockTimingRecorder);
        bev.setIdentityResolver(newIdentityResolver);


        ExecutionContextWithTokens context = new ExecutionContextWithTokens() {
            private List<IdentityToken> tokens = new ArrayList<IdentityToken>();
            private IdentityChain chain;
            @Override
            public List<IdentityToken> getIdentityTokens() {
                return tokens;
            }

            @Override
            public void setIdentityChain(IdentityChain chain) {
                this.chain = chain;
            }

            @Override
            public GeoLocationDetails getLocation() {
                return null;
            }

            @Override
            public IdentityChain getIdentity() {
                return chain;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return null;
            }

            @Override
            public Date getReceivedTime() {
                return null;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return false;
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return 0;
            }

            @Override
            public boolean isTransportSecure() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        bev.execute(context, mockOperationKey, args, observer);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Success, observerCaptor.getValue().getResultType());

        assertEquals(0, context.getIdentityTokens().size());
    }

    @Test
    public void testNewIdentityResolutionWithTokenWriteback() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, succeedingExecutable, mockTimingRecorder);
        bev.setIdentityResolver(newIdentityResolverWithToken);

        ExecutionContextWithTokens context = new ExecutionContextWithTokens() {
            private List<IdentityToken> tokens = new ArrayList<IdentityToken>();
            private IdentityChain chain;
            @Override
            public List<IdentityToken> getIdentityTokens() {
                return tokens;
            }

            @Override
            public void setIdentityChain(IdentityChain chain) {
                this.chain = chain;
            }

            @Override
            public GeoLocationDetails getLocation() {
                return null;
            }

            @Override
            public IdentityChain getIdentity() {
                return chain;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return null;
            }

            @Override
            public Date getReceivedTime() {
                return null;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return false;
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return 0;
            }

            @Override
            public boolean isTransportSecure() {
                return false;
            }
        };
        bev.execute(context, mockOperationKey, args, observer);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Success, observerCaptor.getValue().getResultType());

        assertEquals(1, context.getIdentityTokens().size());
    }

    @Test
    public void testNewIdentityResolutionFailsInspecific() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.setIdentityResolver(failingGenericIdentityResolver(null));

        bev.execute(mockExecutionContext, mockOperationKey, args, observer);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Fault, observerCaptor.getValue().getResultType());
        assertEquals(ServerFaultCode.SecurityException, observerCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void testNewIdentityResolutionFailsSpecific() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder);
        bev.setIdentityResolver(failingGenericIdentityResolver(CredentialFaultCode.BannedLocation));

        bev.execute(mockExecutionContext, mockOperationKey, args, observer);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Fault, observerCaptor.getValue().getResultType());
        assertEquals(ServerFaultCode.BannedLocation, observerCaptor.getValue().getFault().getServerFaultCode());
    }

	private void verifyEventLog(String loggerName, int numExtensionFields, CougarException exception) {
		ArgumentCaptor<RequestLogEvent> loggerCaptor = ArgumentCaptor.forClass(RequestLogEvent.class);
		if (numExtensionFields == 0) {
			verify(eventLogger).logEvent(loggerCaptor.capture(), eq((Object[])null));
		} else {
			ArgumentCaptor<Object[]> extensionFieldsCaptor = ArgumentCaptor.forClass(Object[].class);
			verify(eventLogger).logEvent(loggerCaptor.capture(), extensionFieldsCaptor.capture());
			assertNotNull(extensionFieldsCaptor.getValue());
			assertEquals(numExtensionFields, extensionFieldsCaptor.getValue().length);
		}
		assertNotNull(loggerCaptor.getValue());
		assertEquals(loggerName, loggerCaptor.getValue().getLogName());
		Object[] fieldsToLog = loggerCaptor.getValue().getFieldsToLog();
		assertNotNull(fieldsToLog);
		assertTrue(fieldsToLog[0] instanceof Date);
		assertEquals(uuid, fieldsToLog[1]);
		assertEquals("1.1", fieldsToLog[2]);
		assertEquals("MockOperation", fieldsToLog[3]);
		assertEquals(exception.getFault().getErrorCode(), fieldsToLog[4]);
		assertEquals(0l, fieldsToLog[5]);
		
	}

}
