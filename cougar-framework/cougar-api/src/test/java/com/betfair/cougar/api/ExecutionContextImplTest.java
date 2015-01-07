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

package com.betfair.cougar.api;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for the @See ExecutionContextImpl
 */
public class ExecutionContextImplTest {
    public static final String GEO = "GeoLocationDetailsTest";
    public static final String ID = "IdentityChainTest";
    public static final String UUID = "UUIDTest";


    @Test
    public void testToString() {
        ExecutionContextImpl ec = new ExecutionContextImpl();

        //just check that it works with null everything
        ec.toString();

        ec.setGeoLocationDetails(new GeoLocationDetails() {
            @Override
            public String getRemoteAddr() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<String> getResolvedAddresses() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getCountry() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isLowConfidenceGeoLocation() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getLocation() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getInferredCountry() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String toString() {
                return GEO;
            }
        });
        ec.setIdentity(new IdentityChain() {
            @Override
            public void addIdentity(Identity identity) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Identity> getIdentities() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String toString() {
                return ID;
            }
        });
        ec.setReceivedTime(new Date());
        ec.setRequestUUID(new RequestUUID() {
            @Override
            public String getUUID() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String toCougarLogString() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String toString() {
                return UUID;
            }

            @Override
            public String getRootUUIDComponent() {
                return null;
            }

            @Override
            public String getParentUUIDComponent() {
                return null;
            }

            @Override
            public String getLocalUUIDComponent() {
                return UUID;
            }

            @Override
            public RequestUUID getNewSubUUID() {
                return null;
            }
        });
        ec.setTraceLoggingEnabled(false);


        String result = ec.toString();
        assertTrue(result.contains(GEO));
        assertTrue(result.contains(ID));
        assertTrue(result.contains(UUID));
    }


}
