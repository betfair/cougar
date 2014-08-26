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
import org.apache.mina.common.IoSession;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class RequestResponseManagerImplTest {

    @Test
    public void timeout() throws IOException, InterruptedException {
        IoSession session = mock(IoSession.class);
        NioLogger logger = new NioLogger("ALL");
        RequestResponseManagerImpl impl = new RequestResponseManagerImpl(session, logger, 1);
        assertEquals(0, impl.getOutstandingRequestCount());

        WaitableResponseHandler responseHandler = new WaitableResponseHandler();
        impl.sendRequest(new byte[0], responseHandler);

        Thread.sleep(2); // 2ms > 1ms

        impl.checkForExpiredRequests();

        responseHandler.await(10, TimeUnit.SECONDS);
        assertEquals(WaitableResponseHandler.ResponseType.Timeout, responseHandler.getResponseType());
        assertNull(responseHandler.getResponseMessage());
    }

    @Test
    public void responseReceivedAfterTimeout() throws IOException, InterruptedException {
        IoSession session = mock(IoSession.class);
        NioLogger logger = new NioLogger("ALL");
        RequestResponseManagerImpl impl = new RequestResponseManagerImpl(session, logger, 1);
        assertEquals(0, impl.getOutstandingRequestCount());

        WaitableResponseHandler responseHandler = new WaitableResponseHandler();
        long correlationId =  impl.sendRequest(new byte[0], responseHandler);

        Thread.sleep(2); // 2ms > 1ms

        impl.checkForExpiredRequests();

        ResponseMessage message = new ResponseMessage(correlationId, new byte[0]);
        impl.messageReceived(session, message);
        // just want no exceptions
    }

    private static class WaitableResponseHandler implements RequestResponseManager.ResponseHandler {
        private CountDownLatch latch = new CountDownLatch(1);

        public static enum ResponseType { Timeout, Response, SessionClosed }

        private ResponseType responseType;
        private ResponseMessage responseMessage;

        public void timedOut() {
            result(ResponseType.Timeout, null);
        }

        @Override
        public void responseReceived(ResponseMessage message) {
            result(ResponseType.Response, message);
        }

        @Override
        public void sessionClosed() {
            result(ResponseType.SessionClosed, null);
        }

        private void result(ResponseType type, ResponseMessage message) {
            this.responseType = type;
            this.responseMessage = message;
            latch.countDown();
        }

        public ResponseType getResponseType() {
            return responseType;
        }

        public ResponseMessage getResponseMessage() {
            return responseMessage;
        }

        public void await(long time, TimeUnit unit) throws InterruptedException {
            latch.await(time, unit);
        }
    }
}
