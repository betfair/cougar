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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.netutil.nio.message.ResponseMessage;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

import java.io.IOException;

/**
 *
 */
public interface RequestResponseManager {

    String SESSION_KEY = "Session.RequestResponseManager";

    int getOutstandingRequestCount();

    void sessionClosed(IoSession currentSession);

    void messageReceived(IoSession session, Object message);

    void checkForExpiredRequests();

    public static interface ResponseHandler {
        void responseReceived(ResponseMessage message);
        void sessionClosed();
        void timedOut();
    }

    /**
     * Returns the correlation id..
     */
    long sendRequest(byte[] message, ResponseHandler handler) throws IOException;
}
