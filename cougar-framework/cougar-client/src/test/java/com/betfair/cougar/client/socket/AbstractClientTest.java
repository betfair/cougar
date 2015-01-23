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

package com.betfair.cougar.client.socket;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionResult.ResultType;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.core.impl.transports.TransportRegistryImpl;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioConfig;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.transport.nio.ExecutionVenueNioServer;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static junit.framework.Assert.*;

/**
 * Unit test for the nio client
 */
public abstract class AbstractClientTest {
    private NioConfig cfg = new NioConfig();

    public static Collection<Object[]> protocolVersionParams() {
        byte minVersion = Byte.parseByte(System.getProperty("test.cougar.client.minVersion",String.valueOf(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED)));
        if (minVersion < 0) {
            minVersion = (byte) (CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED - minVersion);
        }
        byte maxVersion = Byte.parseByte(System.getProperty("test.cougar.client.maxVersion",String.valueOf(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED)));

        List<Object[]> ret = new ArrayList<>();
        for (byte b=minVersion; b<=maxVersion; b++) {
            ret.add(new Object[] {b});
        }
        return ret;
    }

    public static final OperationDefinition OPERATION_DEFINITION = new OperationDefinition() {

        @Override
        public OperationKey getOperationKey() {
            return key;
        }

        @Override
        public Parameter[] getParameters() {
            return OP_PARAMS;
        }

        @Override
        public ParameterType getReturnType() {
            return RETURN_PARAM_TYPE;
        }
    };

    private static final OperationKey key = new OperationKey(new ServiceVersion("v1.0"), "UnitTestService", "myUnitTestMethod");

    private static final ParameterType RETURN_PARAM_TYPE = new ParameterType(String.class, null);

    private static final Parameter[] OP_PARAMS = new Parameter[]{
            new Parameter("pass", new ParameterType(Boolean.class, null), true),
            new Parameter("messageId", new ParameterType(Integer.class, null), true),
            new Parameter("echoMe", new ParameterType(String.class, null), true)};

    public static final String BANG = "anticipated exception string";

    class SimpleExecutionContext implements ExecutionContext {

        @Override
        public GeoLocationDetails getLocation() {
            return new GeoLocationDetails() {
                @Override
                public String getRemoteAddr() {
                    return null;
                }

                @Override
                public List<String> getResolvedAddresses() {
                    return Collections.singletonList("9.15.58.62");
                }

                @Override
                public String getCountry() {
                    return null;
                }

                @Override
                public boolean isLowConfidenceGeoLocation() {
                    return false;
                }

                @Override
                public String getLocation() {
                    return null;
                }

                @Override
                public String getInferredCountry() {
                    return null;
                }
            };
        }

        @Override
        public IdentityChain getIdentity() {
            return null;
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
    }

    public static final String ECHO_STRING = "hello";
    String hostAddress = "127.0.0.1";

    @BeforeClass
    public static void suppressLogs() {
//        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }

    volatile int boundToPort;
    ExecutionVenueNioClient client;
    ExecutionVenueNioServer server;
    String connectionString;

    public void before(byte serverVersion) throws Exception {

        server = ServerClientFactory.createServer(hostAddress, 0, serverVersion, ServerClientFactory.getDefaultConfig());
        server.setServerExecutor(Executors.newCachedThreadPool());
        server.setSocketAcceptorProcessors(1);
        server.setTransportRegistry(new TransportRegistryImpl());
        server.start();
        server.setHealthState(true);

        boundToPort=server.getBoundPort();

        cfg.setNioLogger(new NioLogger("ALL"));



        connectionString = hostAddress + ":" + boundToPort;

        client = ServerClientFactory.createClient(connectionString);
        // wait for the client to complete the start
        client.start().get(100, TimeUnit.SECONDS);
    }

    public NioConfig getConfig() {
        return cfg;
    }


    public void after() throws Exception {
    	server.stop();
    	client.stop();

        // reset protocol versions
        CougarProtocol.setMinServerProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED);
        CougarProtocol.setMinClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED);
        CougarProtocol.setMaxServerProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
        CougarProtocol.setMaxClientProtocolVersion(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
    }

    void performRequestAsync(ExecutionVenueNioClient client, ClientTestExecutionObserver observer, Object[] args) throws IOException, InterruptedException {
        client.execute(new SimpleExecutionContext(), OPERATION_DEFINITION, args, observer, DefaultTimeConstraints.NO_CONSTRAINTS);
    }

    void performRequest(ExecutionVenueNioClient client, ClientTestExecutionObserver observer, Object[] args) throws IOException, InterruptedException {
        performRequestAsync(client, observer, args);

		try {
			observer.getExecutionResultFuture().get(30000000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e) {
			fail("Waiting observer never received a result: " + e.getMessage());
		}
    }


    void performRequest(ClientTestExecutionObserver observer, Object[] args) throws IOException, InterruptedException {
    	performRequest(client, observer, args);
    }
    void performRequestAsync(ClientTestExecutionObserver observer, Object[] args) throws IOException, InterruptedException {
    	performRequestAsync(client, observer, args);
    }




    // ######################################################################
    public class ExceptionCapturingObserver extends ClientTestExecutionObserver {
        public void assertResult() {
        	assertTrue( "expected a Fault", getExecutionResult() != null && getExecutionResult().getResultType() == ResultType.Fault);
        }
    }


    // ####################################################################
    public class ClientTestExecutionObserver implements ExecutionObserver {
        private volatile ExecutionResult executionResult;
        private String expectedString;
        private CougarException expectedException;
        private final Lock lock = new ReentrantLock();
        private Condition hasResult = lock.newCondition();

        public ClientTestExecutionObserver() {
        }


        public ClientTestExecutionObserver(String expected) {
            this.expectedString = expected;
        }

        public ClientTestExecutionObserver(CougarException expectedException) {
            this.expectedException = expectedException;
        }

        @Override
        public String toString() {
            return "ClientTestExecutionObserver[expected=" + expectedString + "]";
        }

        @Override
        public void onResult(ExecutionResult executionResult) {
        	lock.lock();
        	try {
        		this.executionResult = executionResult;
        		hasResult.signalAll();
        	}
        	finally {
        		lock.unlock();
        	}
        }

        public void assertResult() {
            if (expectedString != null) {
                assertEquals("Result was not successful: Fault="+executionResult.getFault(), ExecutionResult.ResultType.Success, executionResult.getResultType());
                assertEquals(expectedString, executionResult.getResult());
            }

            if (expectedException != null) {
                assertEquals(ExecutionResult.ResultType.Fault, executionResult.getResultType());
                assertEquals(expectedException.getFault().getDetail().getDetailMessage(),
                    executionResult.getFault().getFault().getDetail().getDetailMessage());
            } else if (executionResult != null) {
            	assertFalse("Unexpected fault" , ExecutionResult.ResultType.Fault == executionResult.getResultType());
            }
            assertTrue(  (executionResult == null) ||  ExecutionResult.ResultType.Subscription != executionResult.getResultType());
        }

        public boolean hasReceivedResult() {
            return executionResult != null;
        }

        public ExecutionResult getExecutionResult() {
	        return executionResult;
        }

        public FutureTask<ExecutionResult> getExecutionResultFuture()  {
			FutureTask<ExecutionResult> future = new HasResultCallable().asFuture();
			new Thread(future, "ExecutionResultFuture").start();
			return future;
        }

        // ###########################################################
        class HasResultCallable implements Callable<ExecutionResult> {
    		@Override
    		public ExecutionResult call() throws Exception {
    			lock.lock();
    			try {
    				if (executionResult == null) {
	    				if (!hasReceivedResult()) {
	    					hasResult.await();
	    				}
    				}
    				return executionResult;
    			} catch (InterruptedException e) {
    				throw e;
    			} finally {
    				lock.unlock();
    			}
    		}

    		public FutureTask<ExecutionResult> asFuture() {
    			return new FutureTask<ExecutionResult>(this);
    		}
    	}

    }





}
