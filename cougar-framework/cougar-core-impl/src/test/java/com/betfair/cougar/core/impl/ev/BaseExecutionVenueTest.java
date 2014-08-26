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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ExecutionContext;
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
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
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
	private static final InterceptorResult FORCE_ON_EXCEPTION = new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new CougarFrameworkException(ServerFaultCode.SecurityException,"I EXPECT TO FAIL"));

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
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

        @Override
        public ExecutionRequirement getExecutionRequirement() {
            return ExecutionRequirement.EXACTLY_ONCE;
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

        @Override
        public ExecutionRequirement getExecutionRequirement() {
            return ExecutionRequirement.EXACTLY_ONCE;
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

        @Override
        public ExecutionRequirement getExecutionRequirement() {
            return ExecutionRequirement.EXACTLY_ONCE;
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

        @Override
        public ExecutionRequirement getExecutionRequirement() {
            return ExecutionRequirement.EXACTLY_ONCE;
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
		public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
			throw new IllegalStateException("Failure can now be an option!");
		}
	};


	private Executable succeedingExecutable = new Executable() {
		@Override
		public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
			observer.onResult(new ExecutionResult(null));
		}
	};

    private IdentityResolver newIdentityResolver = new IdentityResolver() {
        @Override
        public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
        }

        @Override
        public List<IdentityToken> tokenise(IdentityChain chain) {
            return null;
        }
    };

    private IdentityResolver newIdentityResolverWithToken = new IdentityResolver() {
        @Override
        public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
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
            public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
                throw ice;
            }

            @Override
            public List<IdentityToken> tokenise(IdentityChain chain) {
                return null;
            }
        };
    }

	private BaseExecutionVenue bev;
	private List<ExecutionPreProcessor> preProcessorList;
	private List<ExecutionPostProcessor> postProcessorList;
	private DehydratedExecutionContext mockExecutionContext;
	private OperationKey mockOperationKey;
	private Object[] args = new Object[0];
	private OperationDefinition mockOperationDef;
	private ExecutionTimingRecorder mockTimingRecorder;
	private Executable mockExecutable;
	private RequestUUID uuid = new RequestUUIDImpl();

	@Before
	public void setup() {
        bev = new BaseExecutionVenue();
        preProcessorList = new ArrayList<ExecutionPreProcessor>();
        postProcessorList = new ArrayList<ExecutionPostProcessor>();
		bev.setPreProcessors(preProcessorList);
		bev.setPostProcessors(postProcessorList);
        mockExecutionContext = mock(DehydratedExecutionContext.class);

        mockOperationKey = new OperationKey(new ServiceVersion(1,0), "SomeService", "someOperation");
		mockOperationDef = new SimpleOperationDefinition(mockOperationKey, new Parameter[0], new ParameterType(Void.class, new ParameterType[0]));
		mockExecutable = mock(Executable.class);
        mockTimingRecorder = mock(ExecutionTimingRecorder.class);


		when(mockExecutionContext.getRequestUUID()).thenReturn(uuid);
	}

	@Test
	public void testForceOnExceptionForPostProcessorWhenExecutablePasses() {
		postProcessorList.add(forceOnExceptionMockPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver, DefaultTimeConstraints.NO_CONSTRAINTS);
	}

	@Test
	public void testOnExceptionCalledWhenPreProcessorFails() {
		preProcessorList.add(exceptionThrowingPreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

    @Test
    public void testOnExceptionWithServiceCheckedException() {
        preProcessorList.add(checkedExceptionThrowingPreProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, cougarApplicationExceptionResultExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
    }

	@Test
	public void testOnResultCalledWhenPreProcessorPasses() {
		preProcessorList.add(continuePreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

	@Test
	public void testExecutableNotCalledWhenPreProcessorSaySo() {
		preProcessorList.add(forceOnResultPreProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

    @Test
    public void onlyOncePreProcessorViaExecutorExecuteMethod() {
        ExecutionPreProcessor preProcessor = mock(ExecutionPreProcessor.class);
        when(preProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.EXACTLY_ONCE);
        when(preProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));
        preProcessorList.add(preProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver, thisThreadExecutor(), DefaultTimeConstraints.NO_CONSTRAINTS);
        verify(preProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

    @Test
    public void everyPlacePreProcessorViaExecutorExecuteMethod() {
        ExecutionPreProcessor preProcessor = mock(ExecutionPreProcessor.class);
        when(preProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.EVERY_OPPORTUNITY);
        when(preProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));
        preProcessorList.add(preProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver, thisThreadExecutor(), DefaultTimeConstraints.NO_CONSTRAINTS);
        verify(preProcessor, times(2)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

    @Test
    public void preQueueOnlyPreProcessorViaExecutorExecuteMethod() {
        // processors are executed in order, so if the second is executed, but not the first then we're good
        ExecutionPreProcessor preExecuteProcessor = mock(ExecutionPreProcessor.class);
        when(preExecuteProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.PRE_EXECUTE);
        when(preExecuteProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));
        preProcessorList.add(preExecuteProcessor);

        // the second won't execute since we stopped things
        ExecutionPreProcessor preQueueProcessor = mock(ExecutionPreProcessor.class);
        when(preQueueProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.PRE_QUEUE);
        when(preQueueProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.FORCE_ON_RESULT, null));
        preProcessorList.add(preQueueProcessor);

        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver, thisThreadExecutor(), DefaultTimeConstraints.NO_CONSTRAINTS);

        verify(preQueueProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
        verify(preExecuteProcessor, times(0)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

    @Test
    public void preExecuteOnlyPreProcessorViaExecutorExecuteMethod() {
        // we know from the previous test that if we bomb out at queue that the execute one isn't run
        // so this time allow the execution through from the queue and we should get an execute
        ExecutionPreProcessor preExecuteProcessor = mock(ExecutionPreProcessor.class);
        when(preExecuteProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.PRE_EXECUTE);
        when(preExecuteProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));
        preProcessorList.add(preExecuteProcessor);

        ExecutionPreProcessor preQueueProcessor = mock(ExecutionPreProcessor.class);
        when(preQueueProcessor.getExecutionRequirement()).thenReturn(ExecutionRequirement.PRE_QUEUE);
        when(preQueueProcessor.invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class))).thenReturn(new InterceptorResult(InterceptorState.CONTINUE));
        preProcessorList.add(preQueueProcessor);

        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver, thisThreadExecutor(), DefaultTimeConstraints.NO_CONSTRAINTS);

        verify(preQueueProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
        verify(preExecuteProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

	@Test
	public void testFailingPostProcessorCallsOnExceptionWhenExecutableCompletesOK() {
		postProcessorList.add(exceptionThrowingPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

    @Test
    public void testFailingPostProcessorCallsOnServiceCheckedExceptionWhenExecutableCompletesOK() {
        postProcessorList.add(checkedServiceExceptionThrowingPostProcessor);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, cougarApplicationExceptionResultExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
    }

	@Test
	public void testSucceedingPostProcessorCallsOnResultWhenExecutableCompletesOK() {
		postProcessorList.add(forceOnResultPostProcessor);
		bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

	@Test
	public void testPostProcessorForcesOnResultWhenExcecutableFails() {
		postProcessorList.add(forceOnResultPostProcessor);
		bev.registerOperation(null, mockOperationDef, failingExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnExceptionExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

	@Test
	public void testPostProcessorForcesOnExceptionWhenExcecutableFails() {
		postProcessorList.add(forceOnExceptionMockPostProcessor);
		bev.registerOperation(null, mockOperationDef, failingExecutable, mockTimingRecorder, 0);
		bev.execute(mockExecutionContext, mockOperationKey, args, failOnResultExecutionObserver,DefaultTimeConstraints.NO_CONSTRAINTS);
	}

	@Test
	public void testNoOperation() {
		ExecutionObserver observer = mock(ExecutionObserver.class);

		bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

		ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
		verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

		assertEquals(ServerFaultCode.NoSuchOperation, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());

	}

    @Test
    public void testNamepacedServiceNotSpecifiedInCallFail() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation("MyNamespace", mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

        assertEquals(ServerFaultCode.NoSuchOperation, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void testServiceNamespaceSpecifiedInCallFail() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.execute(mockExecutionContext, new OperationKey(mockOperationKey, "MyNamespace"), args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

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

        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.registerOperation("foo", mockOperationDef, mockExecutableFoo, mockTimingRecorderFoo, 0);
        bev.registerOperation("bar", mockOperationDef, mockExecutableBar, mockTimingRecorderBar, 0);


        // Test no namespace
        bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);
        verify(mockExecutable).execute(any(ExecutionContext.class), eq(mockOperationKey), any(Object[].class), any(ExecutionObserver.class), eq(bev), eq(DefaultTimeConstraints.NO_CONSTRAINTS));

        // Test foo
        OperationKey key = new OperationKey(mockOperationKey, "foo");
        bev.execute(mockExecutionContext, key, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);
        verify(mockExecutableFoo).execute(any(ExecutionContext.class), eq(key), any(Object[].class), any(ExecutionObserver.class), eq(bev), eq(DefaultTimeConstraints.NO_CONSTRAINTS));

        // Test bar
        OperationKey barKey = new OperationKey(mockOperationKey, "bar");
        bev.execute(mockExecutionContext, barKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);
        verify(mockExecutableBar).execute(any(ExecutionContext.class), eq(barKey), any(Object[].class), any(ExecutionObserver.class), eq(bev), eq(DefaultTimeConstraints.NO_CONSTRAINTS));
    }

    @Test
    public void testNewIdentityResolution() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, succeedingExecutable, mockTimingRecorder, 0);
        bev.setIdentityResolver(newIdentityResolver);


        DehydratedExecutionContext context = new DehydratedExecutionContext() {
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
            public Date getRequestTime() {
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
        bev.execute(context, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Success, observerCaptor.getValue().getResultType());

        assertEquals(0, context.getIdentityTokens().size());
    }

    @Test
    public void testNewIdentityResolutionWithTokenWriteback() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, succeedingExecutable, mockTimingRecorder, 0);
        bev.setIdentityResolver(newIdentityResolverWithToken);

        DehydratedExecutionContext context = new DehydratedExecutionContext() {
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
            public Date getRequestTime() {
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
        bev.execute(context, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Success, observerCaptor.getValue().getResultType());

        assertEquals(1, context.getIdentityTokens().size());
    }

    @Test
    public void testNewIdentityResolutionFailsInspecific() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.setIdentityResolver(failingGenericIdentityResolver(null));

        bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Fault, observerCaptor.getValue().getResultType());
        assertEquals(ServerFaultCode.SecurityException, observerCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void testNewIdentityResolutionFailsSpecific() {
        ExecutionObserver observer = mock(ExecutionObserver.class);
        bev.registerOperation(null, mockOperationDef, mockExecutable, mockTimingRecorder, 0);
        bev.setIdentityResolver(failingGenericIdentityResolver(CredentialFaultCode.BannedLocation));

        bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> observerCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(observerCaptor.capture());
        assertNotNull(observerCaptor.getValue());
        assertEquals(ExecutionResult.ResultType.Fault, observerCaptor.getValue().getResultType());
        assertEquals(ServerFaultCode.BannedLocation, observerCaptor.getValue().getFault().getServerFaultCode());
    }

    @Test
    public void expiringExecutable() {
        ExecutionObserver observer = mock(ExecutionObserver.class);

        bev.registerOperation(null, mockOperationDef, new Executable() {
            @Override
            public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer, ExecutionVenue executionVenue, TimeConstraints timeConstraints) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {}
            }
        }, mockTimingRecorder, 1000);
        bev.start();
        bev.execute(mockExecutionContext, mockOperationKey, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);

        ArgumentCaptor<ExecutionResult> executionResultArgumentCaptor = ArgumentCaptor.forClass(ExecutionResult.class);
        verify(observer).onResult(executionResultArgumentCaptor.capture());

        assertNotNull(executionResultArgumentCaptor.getValue());
        assertEquals(executionResultArgumentCaptor.getValue().getResultType(), ExecutionResult.ResultType.Fault);

        assertEquals(ServerFaultCode.Timeout, executionResultArgumentCaptor.getValue().getFault().getServerFaultCode());

    }

    private Executor thisThreadExecutor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }

}
