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

package com.betfair.platform;

import com.betfair.cougar.api.ExecutionContext;

import java.util.Date;

/**
 * Helper class to aid in the creation of a client side execution context.
 *
 * Pass in the known params data and this class will generate an {@link com.betfair.cougar.api.ExecutionContext}
 * that will pass the required data across.
 */
public class ExecutionContextHelper {
    public static ExecutionContext createContext(String applicationKey, String ipAddress) {
        return createContext(applicationKey, ipAddress, null, new Date(), null, false);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken) {
        return createContext(applicationKey, ipAddress, sessionToken, null, new Date(), null, null, false);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken, String inferredCountry) {
        return createContext(applicationKey, ipAddress, sessionToken, null, new Date(), null, inferredCountry, false);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken,
                                                    Date receivedTime, String geoLocatedCountry, boolean traceEnabled) {
        return createContext(applicationKey, ipAddress, sessionToken, null, receivedTime, geoLocatedCountry, traceEnabled);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken, String adminSessionToken,
                                                    Date receivedTime, String geoLocatedCountry, boolean traceEnabled) {
        return createContext(applicationKey, ipAddress, sessionToken, adminSessionToken, receivedTime, geoLocatedCountry, null, traceEnabled);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken, String adminSessionToken,
                                                    Date receivedTime, String geoLocatedCountry, String inferredCountry, boolean traceEnabled) {
        return createContext(applicationKey, ipAddress, sessionToken, adminSessionToken, receivedTime, new Date(), geoLocatedCountry, inferredCountry, traceEnabled);
    }

    public static ExecutionContext createContext(String applicationKey, String ipAddress, String sessionToken, String adminSessionToken,
                                                    Date receivedTime, Date requestTime, String geoLocatedCountry, String inferredCountry, boolean traceEnabled) {
        if (applicationKey == null) throw new IllegalArgumentException("Application key must be defined");
        return new ClientExecutionContext(applicationKey, ipAddress, sessionToken, adminSessionToken,
                                            receivedTime, requestTime, geoLocatedCountry, inferredCountry, traceEnabled);
    }
}
