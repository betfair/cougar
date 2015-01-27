/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.tests.clienttests;

import com.betfair.baseline.v2.BaselineClient;
import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.enumerations.PreOrPostInterceptorException;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextImpl;
import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.InvalidCredentialsException;
import com.betfair.cougar.baseline.security.GeneralIdentityResolver;
import com.betfair.cougar.core.impl.CougarSpringCtxFactoryImpl;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CougarClientWrapper {

    public static enum UnderlyingTransport {
        HTTP, Socket
    }

    public static enum TransportType {
        RESCRIPT("rescriptTransport", false, false, false, UnderlyingTransport.HTTP),
        SECURE_RESCRIPT("secureRescriptTransport", true, false, false, UnderlyingTransport.HTTP),
        CLIENT_AUTH_RESCRIPT("secureRescriptTransportWithClientAuth", true, true, false, UnderlyingTransport.HTTP),
        ASYNC_RESCRIPT("asyncRescriptTransport", false, false, true, UnderlyingTransport.HTTP),
        SECURE_ASYNC_RESCRIPT("secureAsyncRescriptTransport", true, false, true, UnderlyingTransport.HTTP),
        CLIENT_AUTH_ASYNC_RESCRIPT("secureAsyncRescriptTransportWithClientAuth", true, true, true, UnderlyingTransport.HTTP),
        SOCKET("socketTransport", false, false, true, UnderlyingTransport.Socket),
        SECURE_SOCKET("secureSocketTransport", true, false, true, UnderlyingTransport.Socket),
        CLIENT_AUTH_SOCKET("secureSocketTransportWithClientAuth", true, true, true, UnderlyingTransport.Socket);

        private String clientName;
        private boolean secure;
        private boolean clientAuth;
        private UnderlyingTransport underlyingTransport;
        private boolean async;

        private TransportType(String clientName, boolean secure, boolean clientAuth, boolean async, UnderlyingTransport underlyingTransport) {
            this.clientName = clientName;
            this.secure = secure;
            this.clientAuth = clientAuth;
            this.underlyingTransport = underlyingTransport;
            this.async = async;
        }

        public String getClientName() {
            return clientName;
        }

        public boolean isSecure() {
            return secure;
        }

        public boolean isClientAuth() {
            return clientAuth;
        }

        public UnderlyingTransport getUnderlyingTransport() {
            return underlyingTransport;
        }

        public boolean isAsync() {
            return async;
        }
    }

	private BaselineSyncClient client;
	private BaselineClient asyncClient;
	private ExecutionContext ctx;

    private ClassPathXmlApplicationContext appContext;

    private static final IdentityResolver IDENTITY_RESOLVER = new GeneralIdentityResolver();

    private static Map<String, CougarClientWrapper> wrappers = new HashMap<String, CougarClientWrapper>();

    public static synchronized CougarClientWrapper getInstance(TransportType tt) throws Exception {
        return getInstance(tt, true);
    }

    public static synchronized CougarClientWrapper getInstance(TransportType tt, boolean clientEnumHandlingHardFail) throws Exception {
        String key = tt.name()+clientEnumHandlingHardFail;
        CougarClientWrapper ret = wrappers.get(key);
        if (ret == null) {
            ret = new CougarClientWrapper();
            ret.setUpClient(tt.getClientName(), clientEnumHandlingHardFail);
            wrappers.put(key, ret);
        }
        return ret;
    }

    private CougarClientWrapper(){

	}

    /**
     * Start a cougar client instance using the given transport (if one isn't already running)
     * Change the transport the running client is using if necessary
     *
     * @param transportType
     * @throws Exception
     */
	private void setUpClient(String transportType, boolean clientEnumHandlingHardFail) throws Exception {
        System.setProperty("cougar.client.transport", transportType);
        System.setProperty("cougar.app.name", "TestClient"); // Set the client app name (will cause client logs to be renamed)
        System.setProperty("cougar.client.http.async.worker.maxPoolSize", "10");
        System.setProperty("cougar.client.http.enums.hardFailure", String.valueOf(clientEnumHandlingHardFail));
        System.setProperty("cougar.client.http.async.enums.hardFailure", String.valueOf(clientEnumHandlingHardFail));
        System.setProperty("cougar.client.socket.enums.hardFailure", String.valueOf(clientEnumHandlingHardFail));
        System.setProperty("cougar.client.socket.rpc.timeout","30000");
        // skip the defaults - we want to use our own
        System.setProperty("cougar.client.socket.ssl.supportsTls","false");
        System.setProperty("cougar.client.socket.ssl.requiresTls","false");

        CougarLoggingUtils.setTraceLogger(null);
        CougarSpringCtxFactoryImpl cougarCtx = new CougarSpringCtxFactoryImpl();
        appContext = cougarCtx.create(null);
        client = (BaselineSyncClient) appContext.getBean("syncClient");
        asyncClient = (BaselineClient) appContext.getBean("asyncClient");
        ctx = new CougarClientExecutionContext();
	}

    /**
     * Populates the Exec context's identity member with the passed Identity tokens
     * @param idtokens
     * @return IdentityChain
     */
    public IdentityChain setCtxIdentity(Map<String,String> idtokens){
    	final List<IdentityToken> tokens = new ArrayList<IdentityToken>();
    	for(Map.Entry<String, String> entry: idtokens.entrySet()){
    		tokens.add(new IdentityToken(entry.getKey(), entry.getValue()));
    	}
    	try {
    		IdentityChain idChain = new IdentityChainImpl();
            IDENTITY_RESOLVER.resolve(idChain, new DehydratedExecutionContext() {
                @Override
                public List<IdentityToken> getIdentityTokens() {
                    return tokens;
                }

                @Override
                public void setIdentityChain(IdentityChain chain) {
                }

                @Override
                public GeoLocationDetails getLocation() {
                    return null;
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
            });
			((ExecutionContextImpl)ctx).setIdentity(idChain);
			return idChain;
		} catch (InvalidCredentialsException e) {
			e.printStackTrace();
			return null;
		}
    }

    public BaselineSyncClient getClient() {
		return client;
	}

    public BaselineClient getAsyncClient() {
		return asyncClient;
	}


	public ExecutionContext getCtx() {
		return ctx;
	}

	/**
	 * Wrapper method for the interceptorCheckedException baseline operation. Will call the operation requesting either a pre or post exception, catch the response exception and return the exception message
	 * @param preOrPost
     * @return String
	 *
	 */
	public String callInterceptorExceptionOperation(PreOrPostInterceptorException preOrPost){

		try {
			getClient().interceptorCheckedExceptionOperation(getCtx(), preOrPost);
		}
		catch (SimpleException se) {
			return se.getReason();
		}

		return "No exception was thrown";
	}


	public static final class CougarClientExecutionContext extends ExecutionContextImpl{

		private GeoLocationDetails geoLocationDetails = null;

	    @Override
	    public GeoLocationDetails getLocation() {
	        if (geoLocationDetails == null) {
	            try {
	                final List<String> thisAddress = Collections.singletonList(InetAddress.getLocalHost().getHostAddress());
	                geoLocationDetails = new GeoLocationDetails() {

	                    @Override
	                    public String getCountry() {
	                        return "UK";
	                    }

	                    @Override
	                    public String getLocation() {
	                        return null;
	                    }

	                    @Override
	                    public String getRemoteAddr() {
	                        return thisAddress.get(0);
	                    }

	                    @Override
	                    public List<String> getResolvedAddresses() {
	                        return thisAddress;
	                    }

						@Override
						public boolean isLowConfidenceGeoLocation() {
							return false;
						}

                        @Override
                        public String getInferredCountry() {
                            return null;
                        }
                    };
	            } catch (UnknownHostException ignored) {} //unecessary checked exception.  wicked
	        }
	        return geoLocationDetails;
	    }

	}
}

