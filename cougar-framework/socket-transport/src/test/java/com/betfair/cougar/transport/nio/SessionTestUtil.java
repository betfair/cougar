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

package com.betfair.cougar.transport.nio;

import com.betfair.cougar.netutil.nio.CougarProtocol;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class to build mock sessions
 */
public class SessionTestUtil {

    public static IoSession newSession(byte version) {
        final IoSession mockSession = mock(IoSession.class);
        when(mockSession.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME)).thenReturn(version);
        final WriteFuture mockFuture = mock(WriteFuture.class);
        when(mockSession.write(any())).thenReturn(mockFuture);
        return mockSession;
    }

    public static IoSession newV1Session() {
        return newSession(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC);
    }

    public static IoSession newV2Session() {
        return newSession(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC);
    }

    public static IoSession newV3Session() {
        return newSession(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS);
    }
}
