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

import com.betfair.cougar.netutil.nio.message.*;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage.ProtocolMessageType;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.atomic.AtomicLong;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.ALL;

@ManagedResource
public class CougarProtocolDecoder extends CumulativeProtocolDecoder implements Exportable {
    private static final Logger LOG = LoggerFactory.getLogger(CougarProtocolDecoder.class);
    private final NioLogger nioLogger;

    private final AtomicLong badMessagesReceived = new AtomicLong();
    private final AtomicLong acceptsReceived = new AtomicLong();
    private final AtomicLong connectsReceived = new AtomicLong();
    private final AtomicLong rejectsReceived = new AtomicLong();
    private final AtomicLong messageRequestsReceived = new AtomicLong();
    private final AtomicLong messageResponsesReceived = new AtomicLong();
    private final AtomicLong eventsReceived = new AtomicLong();
    private final AtomicLong keepAlivesReceived = new AtomicLong();
    private final AtomicLong incompleteMessagesReceived = new AtomicLong();
    private final AtomicLong disconnectsReceived = new AtomicLong();
    private final AtomicLong suspendsReceived = new AtomicLong();
    private final AtomicLong tlsRequestsReceived = new AtomicLong();
    private final AtomicLong tlsResponsesReceived = new AtomicLong();

    public CougarProtocolDecoder(NioLogger nioLogger) {
        this.nioLogger = nioLogger;

        export(nioLogger.getJmxControl());
    }

    @Override
    protected boolean doDecode(IoSession session, ByteBuffer buffer, ProtocolDecoderOutput out) throws Exception {
        if (buffer.prefixedDataAvailable(4)) {
            int msgLen = buffer.getInt() - 1; // the message type is not included in the payload.
            ProtocolMessageType pm = ProtocolMessageType.getMessageByMessageType(buffer.get());
            byte[] messageBody;
            // we need to know if we're acting as a client or a server and treat appropriately
            if (pm == ProtocolMessageType.MESSAGE) {
                Boolean b = (Boolean) session.getAttribute(CougarProtocol.IS_SERVER_ATTR_NAME);
                if (b != null) {
                    pm = b ? ProtocolMessageType.MESSAGE_REQUEST : ProtocolMessageType.MESSAGE_RESPONSE;
                } else {
                    throw new IllegalStateException("Received MESSAGE, but yet don't know whether I'm a client or a server");
                }
            }
            switch (pm) {
                case MESSAGE_REQUEST:
                    messageRequestsReceived.incrementAndGet();
                    messageBody = new byte[msgLen - 8];
                    nioLogger.log(ALL, session, "CougarProtocolDecoder: MESSAGE_REQUEST: Message of length %s received", msgLen);
                    long reqCorrelationId = buffer.getLong();
                    buffer.get(messageBody);
                    RequestMessage req = new RequestMessage(reqCorrelationId, messageBody);
                    out.write(req);
                    break;
                case MESSAGE_RESPONSE:
                    messageResponsesReceived.incrementAndGet();
                    messageBody = new byte[msgLen - 8];
                    nioLogger.log(ALL, session, "CougarProtocolDecoder: MESSAGE_RESPONSE: Message of length %s received", msgLen);
                    long respCorrelationId = buffer.getLong();
                    buffer.get(messageBody);
                    ResponseMessage res = new ResponseMessage(respCorrelationId, messageBody);
                    out.write(res);
                    break;
                case EVENT:
                    eventsReceived.incrementAndGet();
                    messageBody = new byte[msgLen];
                    nioLogger.log(ALL, session, "CougarProtocolDecoder: EVENT: Message of length %s received", msgLen);
                    buffer.get(messageBody);
                    EventMessage em = new EventMessage(messageBody);
                    out.write(em);
                    break;
                case CONNECT:
                    connectsReceived.incrementAndGet();
                    byte[] versionsRequested = NioUtils.getVersionSet(buffer);
                    ConnectMessage cm = new ConnectMessage(versionsRequested);
                    out.write(cm);
                    break;
                case ACCEPT:
                    //Client Side - server has accepted our connection request
                    acceptsReceived.incrementAndGet();
                    byte versionAccepted = buffer.get();
                    AcceptMessage acceptMessage = new AcceptMessage(versionAccepted);
                    out.write(acceptMessage);
                    break;
                case REJECT:
                    //Client Side - server has said foxtrot oscar
                    rejectsReceived.incrementAndGet();
                    byte rejectReasonCode = buffer.get();
                    RejectMessageReason reason = RejectMessageReason.getByReasonCode(rejectReasonCode);
                    byte[] versionsAccepted = NioUtils.getVersionSet(buffer);
                    RejectMessage rejectMessage = new RejectMessage(reason, versionsAccepted);
                    out.write(rejectMessage);
                    break;
                case KEEP_ALIVE:
                    keepAlivesReceived.incrementAndGet();
                    KeepAliveMessage keepAlive = new KeepAliveMessage();
                    out.write(keepAlive);
                    break;
                case DISCONNECT:
                    disconnectsReceived.incrementAndGet();
                    DisconnectMessage disconnect = new DisconnectMessage();
                    out.write(disconnect);
                    break;
                case SUSPEND:
                    suspendsReceived.incrementAndGet();
                    SuspendMessage suspend = new SuspendMessage();
                    out.write(suspend);
                    break;
                case START_TLS_REQUEST:
                    //Client Side - server has said foxtrot oscar
                    tlsRequestsReceived.incrementAndGet();
                    byte requestValue = buffer.get();
                    TLSRequirement requirement = TLSRequirement.getByValue(requestValue);
                    StartTLSRequestMessage requestMessage = new StartTLSRequestMessage(requirement);
                    out.write(requestMessage);
                    break;
                case START_TLS_RESPONSE:
                    //Client Side - server has said foxtrot oscar
                    tlsResponsesReceived.incrementAndGet();
                    byte responseValue = buffer.get();
                    TLSResult result = TLSResult.getByValue(responseValue);
                    StartTLSResponseMessage responseMessage = new StartTLSResponseMessage(result);
                    out.write(responseMessage);
                    break;
                default:
                    badMessagesReceived.incrementAndGet();
                    LOG.error("Unknown message type " + pm + " - Ignoring");

            }
            return true;

        } else {
            incompleteMessagesReceived.incrementAndGet();
            nioLogger.log(ALL, session, "CougarProtocolDecoder: Returning FALSE, remaining was %s", buffer.remaining());
            return false;
        }
    }


    private String getAsString(byte[] versions) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (byte b : versions) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(b);
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Exports this service as an MBean, if the JMXControl is available
     */
    @Override
    public void export(JMXControl jmxControl) {
        if (jmxControl != null) {
            jmxControl.registerMBean("CoUGAR.socket.transport:name=decoder", this);
        }
    }

    @ManagedAttribute
    public long getBadMessagesReceived() {
        return badMessagesReceived.get();
    }

    @ManagedAttribute
    public long getAcceptsReceived() {
        return acceptsReceived.get();
    }

    @ManagedAttribute
    public long getConnectsReceived() {
        return connectsReceived.get();
    }

    @ManagedAttribute
    public long getRejectsReceived() {
        return rejectsReceived.get();
    }

    @ManagedAttribute
    public long getMessageRequestsReceived() {
        return messageRequestsReceived.get();
    }

    @ManagedAttribute
    public long getMessageResponsesReceived() {
        return messageResponsesReceived.get();
    }

    @ManagedAttribute
    public long getEventsReceived() {
        return eventsReceived.get();
    }

    @ManagedAttribute
    public long getKeepAlivesReceived() {
        return keepAlivesReceived.get();
    }

    @ManagedAttribute
    public long getSuspendsReceived() {
        return suspendsReceived.get();
    }

    @ManagedAttribute
    public long getIncompleteMessagesReceived() {
        return incompleteMessagesReceived.get();
    }
}
