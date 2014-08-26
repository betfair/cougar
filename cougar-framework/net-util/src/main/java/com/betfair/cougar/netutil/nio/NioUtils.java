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

import com.betfair.cougar.netutil.nio.message.EventMessage;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.netutil.nio.message.TLSResult;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import java.awt.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import static com.betfair.cougar.netutil.nio.message.ProtocolMessage.ProtocolMessageType;

/**
 * Utilities for managing the session and binary protocol.
 */
public class NioUtils {
    private static AtomicLong sessionIdent = new AtomicLong(0);

    public static void writeVersionSet(ByteBuffer buffer, byte[] applicationVersions) {
        buffer.put((byte) applicationVersions.length);
        buffer.put(applicationVersions);
    }

    public static byte[] getVersionSet(ByteBuffer buffer) {
        int numElements = buffer.get();
        byte[] versions = new byte[numElements];
        buffer.get(versions);
        return versions;
    }

    public static String getSessionId(IoSession session) {
        String sessionId = (String) session.getAttribute("COUGAR_SESSION_ID");
        if (sessionId == null) {
            sessionId = String.format("%07d", sessionIdent.incrementAndGet());
            session.setAttribute("COUGAR_SESSION_ID", sessionId);
        }
        return sessionId;
    }

    public static boolean isSecure(IoSession session) {
        TLSResult result = (TLSResult) session.getAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME);
        return result != null && result == TLSResult.SSL;
    }

    public static ByteBuffer createMessageHeader(int numBytes, ProtocolMessage message) {
        return createMessageHeader(numBytes, message.getProtocolMessageType());
    }

    public static ByteBuffer createMessageHeader(int numBytes, ProtocolMessageType type) {
        ByteBuffer bb = ByteBuffer.allocate(5 + numBytes);
        bb.putInt(numBytes + 1);
        bb.put(type.getMessageType());
        return bb;
    }

    public static void writeEventMessageToSession(IoSession session, Object obj, CougarObjectIOFactory objectIOFactory) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CougarObjectOutput out = objectIOFactory.newCougarObjectOutput(baos, CougarProtocol.getProtocolVersion(session));
        out.writeObject(obj);
        out.flush();

        session.write(new EventMessage(baos.toByteArray()));
    }

    public static String getRemoteAddressUrl(IoSession session) {
        StringBuilder sb = new StringBuilder("tcp");
        if (isSecure(session)) {
            sb.append("+ssl");
        }
        sb.append("://");
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress iAddress = (InetSocketAddress) remoteAddress;
            if (iAddress.isUnresolved()) {
                sb.append(iAddress.getAddress());
            }
            else {
                sb.append(iAddress.getHostName());
            }
            sb.append(":").append(iAddress.getPort());
        }
        else {
            sb.append(remoteAddress);
        }
        return sb.toString();
    }
}
