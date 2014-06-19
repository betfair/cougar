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

package com.betfair.platform.application;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.to.*;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Very basic class to test remote connectivity for client cougar to cougar calculation
 */
public class CougarToCougarCommsTester implements ApplicationListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(CougarToCougarCommsTester.class);

    private BaselineSyncClient client;

    private RequestContext ctx = new RequestContext() {
        private GeoLocationDetails geoDetails;

        @Override
        public GeoLocationDetails getLocation() {
            if (geoDetails == null) {
                try {
                    final List<String> thisAddress = Collections.singletonList(InetAddress.getLocalHost().getHostAddress());
                    geoDetails = new GeoLocationDetails() {

                        @Override
                        public String getCountry() {
                            return "UK";
                        }

                        @Override
                        public String getLocation() {
                            return null;
                        }

                        @Override
                        public String getInferredCountry() {
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

                    };
                } catch (UnknownHostException ignored) {} //unecessary checked exception.  wicked
            }
            return geoDetails;
        }



        @Override
        public void trace(String msg, Object... args) {
        }

        @Override
        public void addEventLogRecord(LoggableEvent record) {
        }

        @Override
        public void setRequestLogExtension(LogExtension extension) {
        }

        @Override
        public void setConnectedObjectLogExtension(LogExtension extension) {
        }

        @Override
        public LogExtension getConnectedObjectLogExtension() {
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
    };

    private ExecutionObserver obs = new ExecutionObserver() {

        @Override
        public void onResult(ExecutionResult result) {
            LOGGER.info("Result received: [" + result.getResult().toString() + "]");
        }

    };


    private ExecutionObserver byteObs = new ExecutionObserver() {

        @Override
        public void onResult(ExecutionResult executionResult) {
            ByteOperationResponseObject obj = ((ByteOperationResponseObject)executionResult.getResult());
            String result = null;
            try {
                result = new String(obj.getBodyParameter(), "utf-8");
            } catch (UnsupportedEncodingException e) {

            }
            System.out.println(result);
        }
    };

    private ExecutionObserver voidObs = new ExecutionObserver() {

        @Override
        public void onResult(ExecutionResult result) {
            LOGGER.info("Void result received this valid here should be null [" + result.getResult() + "]");
        }
    };

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            System.out.println(new Date() + ": SIMPLE TEST:");
            simple();
            System.out.println(new Date() + ": SIMPLE DONE");
            System.out.println(new Date() + ": SIMPLE TEST:");
            timeout();
            System.out.println(new Date() + ": SIMPLE DONE");
            System.out.println(new Date() + ": ASYNC TEST:");
            async();
            System.out.println(new Date() + ": ASYNC DONE");
            System.out.println(new Date() + ": EXCEPTION TEST:");
            exception();
            System.out.println(new Date() + ": EXCEPTION DONE");

            System.out.println("SIMPLE TEST:");
            run();

            try {Thread.sleep(15000); } catch (InterruptedException e) {};
            System.out.println("MT TEST:");
            multithreadedInstanceTest();
        }
    }

    public void simple() {
        try {
            String mesasge = getClient().testSimpleGet(ctx, "client foo").getMessage();
            System.out.println("testSimpleGet() returned "+ mesasge);
        } catch (Exception ex) {
            System.err.println("EXCEPTION RECEIVED: " + ex.getMessage());
            ex.printStackTrace();
        }

    }


    public void timeout() {
        try {
            getClient().testSleep(ctx, 2000L, 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TimeoutException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SimpleException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public void async() {
        try {
          //  getClient().testSleep(ctx, 1000L);
            System.out.println("testGetTimeout() returned");
        } catch (Exception ex) {
            System.err.println("EXCEPTION RECEIVED: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

     public void exception() {
        try {
            getClient().testException(ctx, "Unauthorised", "SUSPENDED");
        } catch (CougarApplicationException cax) {
            System.out.println("Expected Application Exception: " + cax.getExceptionCode()+", responseCode: "+ cax.getResponseCode());
        } catch (Exception ex) {
            System.err.println("EXCEPTION RECEIVED: " + ex.getMessage());
            ex.printStackTrace();
        }

    }


    public void run() {
        try {
            System.out.println("testSimpleGet() returned "+getClient().testSimpleGet(ctx, "FORWARD:foo").getMessage());


            SomeComplexObject sco = new SomeComplexObject();
            sco.setDateTimeParameter(new Date());
            sco.setEnumParameter(SomeComplexObjectEnumParameterEnum.BAR);
            sco.setListParameter(new ArrayList<String>() {{
                add("bob");
            }});
            sco.setStringParameter("Foo");

            Map<String,SomeComplexObject> m = new HashMap<String, SomeComplexObject>();
            m.put("wibble", sco);
            BodyParamComplexMapObject obj = new BodyParamComplexMapObject();
            obj.setComplexMap(m);
            ComplexMapOperationResponseObject response = getClient().complexMapOperation(ctx, obj);

            System.out.println(response);
        } catch (Exception ex) {
            System.err.println("EXCEPTION RECEIVED: ");
            ex.printStackTrace();
        }

    }

    public void multithreadedInstanceTest() {
        for (int i=0; i<10; i++) {
            final String parrot = String.valueOf(i);

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    final String expected = parrot;
                    try {
                        while (true) {
                            long time = System.currentTimeMillis();
                            SimpleResponse response = getClient().testSimpleGet(ctx, parrot);
                            System.out.println("MT response: "+ response);
                            if (!response.getMessage().equals(expected)) {
                                System.out.println("WTF!");
//                            } else {
//                                System.out.println(expected+ ": Response returned in: " + (System.currentTimeMillis() - time) + "ms");
                            }
//                            Thread.sleep(50);
                        }
                    } catch (SimpleException e) {
                        LOGGER.error("An exception occurred", e);
//                    } catch (InterruptedException e) {
//                        LOGGER.error("An exception occurred", e);
                    }
                }
            }, "ARSE-"+i);
            t.start();
        }

    }

    public BaselineSyncClient getClient() {
        return client;
    }

    public void setClient(BaselineSyncClient client) {
        this.client = client;
    }
}
