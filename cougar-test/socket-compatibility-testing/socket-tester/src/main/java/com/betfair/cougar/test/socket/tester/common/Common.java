/*
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

package com.betfair.cougar.test.socket.tester.common;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.SimpleOperationDefinition;
import com.betfair.cougar.core.api.transcription.*;

/**
 *
 */
public class Common {

    private static final int minMajorCougarVersion = 2;
    private static final int[] maxMinorVersions = new int[] { -1, -1, 10, Integer.MAX_VALUE };
    private static ServiceVersion serviceVersionFromCougarVersion(int majorVersion, int minorVersion) {
        if (majorVersion >= maxMinorVersions.length) {
            throw new RuntimeException("Invalid major version: "+majorVersion);
        }
        if (minorVersion > maxMinorVersions[majorVersion]) {
            throw new RuntimeException("Invalid minor version: "+minorVersion+" > "+maxMinorVersions[majorVersion]);
        }
        int retMinor = 0;
        for (int m=minMajorCougarVersion; m<majorVersion; m++) {
            retMinor += 1 /* for zero version */ + maxMinorVersions[m];
        }
        retMinor++; /* for zero version on current major */
        retMinor += minorVersion;
        return new ServiceVersion(1,retMinor);
    }

    public static final ServiceVersion SERVICE_VERSION = serviceVersionFromCougarVersion(3,3);

    public static final OperationDefinition echoOperationDefinition = new SimpleOperationDefinition(
            new OperationKey(new ServiceVersion(1,0),"SocketTester","echoSuccess", OperationKey.Type.Request),
            new Parameter[] { new Parameter("message", ParameterType.create(String.class), true) },
            ParameterType.create(EchoResponse.class)
            );
    public static final OperationDefinition echoFailureOperationDefinition = new SimpleOperationDefinition(
            new OperationKey(new ServiceVersion(1,0),"SocketTester","echoFailure", OperationKey.Type.Request),
            new Parameter[] { new Parameter("message", ParameterType.create(String.class), true) },
            ParameterType.create(EchoResponse.class)
            );
    public static final OperationDefinition heapSubscribeOperationDefinition = new SimpleOperationDefinition(
            new OperationKey(new ServiceVersion(1,0),"SocketTester","heapSubscribe", OperationKey.Type.ConnectedObject),
            new Parameter[] { new Parameter("clientId", ParameterType.create(String.class), true) },
            ParameterType.create(ConnectedResponse.class)
            );
    public static final OperationDefinition heapSetOperationDefinition = new SimpleOperationDefinition(
            new OperationKey(new ServiceVersion(1,0),"SocketTester","heapSet", OperationKey.Type.Request),
            new Parameter[] { new Parameter("clientId", ParameterType.create(String.class), true), new Parameter("message", ParameterType.create(String.class), true) },
            ParameterType.create(void.class)
            );
    public static final OperationDefinition heapCloseOperationDefinition = new SimpleOperationDefinition(
            new OperationKey(new ServiceVersion(1,0),"SocketTester","heapClose", OperationKey.Type.Request),
            new Parameter[] { new Parameter("clientId", ParameterType.create(String.class), true) },
            ParameterType.create(void.class)
            );

}
