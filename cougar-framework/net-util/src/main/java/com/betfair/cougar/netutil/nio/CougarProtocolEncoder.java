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
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.atomic.AtomicLong;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.ALL;
import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.PROTOCOL;

@ManagedResource
public class CougarProtocolEncoder extends ProtocolEncoderAdapter implements Exportable {
    private final NioLogger nioLogger;

    private final AtomicLong badMessagesRequested = new AtomicLong();
    private final AtomicLong acceptsSent = new AtomicLong();
    private final AtomicLong connectsSent = new AtomicLong();
    private final AtomicLong rejectsSent = new AtomicLong();
    private final AtomicLong keepAlivesSent = new AtomicLong();
    private final AtomicLong messageRequestsSent = new AtomicLong();
    private final AtomicLong messageResponsesSent = new AtomicLong();
    private final AtomicLong disconnectsSent = new AtomicLong();
    private final AtomicLong eventsSent = new AtomicLong();
    private final AtomicLong suspendsSent = new AtomicLong();
    private final AtomicLong tlsRequestsSent = new AtomicLong();
    private final AtomicLong tlsResponsesSent = new AtomicLong();

    public CougarProtocolEncoder(NioLogger nioLogger) {
        this.nioLogger = nioLogger;

        export(nioLogger.getJmxControl());
    }

    public static ByteBuffer encode(ProtocolMessage pm, byte protocolVersion) {
        final ByteBuffer buffer;
        switch (pm.getProtocolMessageType()) {
            case ACCEPT:
                buffer = NioUtils.createMessageHeader(1, pm);
                buffer.put(((AcceptMessage) pm).getAcceptedVersion());
                break;

            case CONNECT:
                ConnectMessage cm = (ConnectMessage) pm;
                buffer = NioUtils.createMessageHeader(1 + cm.getApplicationVersions().length, pm);
                NioUtils.writeVersionSet(buffer, cm.getApplicationVersions());
                break;

            case REJECT:
                RejectMessage rm = (RejectMessage) pm;
                buffer = NioUtils.createMessageHeader(2 + rm.getAcceptableVersions().length, pm);
                buffer.put(rm.getRejectReason().getReasonCode());
                NioUtils.writeVersionSet(buffer, rm.getAcceptableVersions());
                break;

            case KEEP_ALIVE:
                buffer = NioUtils.createMessageHeader(0, pm);
                break;
            case DISCONNECT:
                buffer = NioUtils.createMessageHeader(0, pm);
                break;

            case MESSAGE_REQUEST:
                RequestMessage req = (RequestMessage) pm;
                ProtocolMessageType reqMsgType = protocolVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC ? ProtocolMessageType.MESSAGE : ProtocolMessageType.MESSAGE_REQUEST;
                buffer = NioUtils.createMessageHeader(req.getPayload().length + 8, reqMsgType);
                buffer.putLong(req.getCorrelationId());
                buffer.put(req.getPayload());
                break;
            case MESSAGE_RESPONSE:
                ResponseMessage resp = (ResponseMessage) pm;
                // backwards compatibility with version 1 protocol
                ProtocolMessageType responseType = protocolVersion == CougarProtocol.TRANSPORT_PROTOCOL_VERSION_CLIENT_ONLY_RPC ? ProtocolMessageType.MESSAGE : ProtocolMessageType.MESSAGE_RESPONSE;
                buffer = NioUtils.createMessageHeader(resp.getPayload().length + 8, responseType);
                buffer.putLong(resp.getCorrelationId());
                buffer.put(resp.getPayload());
                break;

            case EVENT:
                EventMessage em = (EventMessage) pm;
                if (protocolVersion < CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC) {
                    return null;
                }
                buffer = NioUtils.createMessageHeader(em.getPayload().length, em);
                buffer.put(em.getPayload());
                break;

            case SUSPEND:
                if (protocolVersion < CougarProtocol.TRANSPORT_PROTOCOL_VERSION_BIDIRECTION_RPC) {
                    return null;
                }
                buffer = NioUtils.createMessageHeader(0, pm);
                break;

            case START_TLS_REQUEST:
                if (protocolVersion < CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
                    return null;
                }
                buffer = NioUtils.createMessageHeader(1, pm);
                buffer.put(((StartTLSRequestMessage) pm).getRequirement().getValue());
                break;
            case START_TLS_RESPONSE:
                if (protocolVersion < CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
                    return null;
                }
                buffer = NioUtils.createMessageHeader(1, pm);
                buffer.put(((StartTLSResponseMessage) pm).getResult().getValue());
                break;

            default:
                throw new IllegalArgumentException("Unknown ProtocolMessage [" + pm.getProtocolMessageType() + "] received");

        }

        return buffer;
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        final ByteBuffer buffer;
        if (message instanceof ProtocolMessage) {
            ProtocolMessage pm = (ProtocolMessage) message;
            nioLogger.log(PROTOCOL, session, "CougarProtocolEncoder: Writing protocol message %s", pm.getProtocolMessageType());

            Byte version = (Byte) session.getAttribute(CougarProtocol.PROTOCOL_VERSION_ATTR_NAME);
            // go for lowest likely common denominator, since this will likely only occur for RejectMessages
            if (version == null) {
                version = CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED;
            }
            buffer = pm.getSerialisedForm(version);
            if (buffer == null) {
                badMessagesRequested.incrementAndGet();
                throw new IllegalArgumentException("Couldn't serialise ProtocolMessage [" + ((ProtocolMessage) message).getProtocolMessageType() + "]");
            }

            switch (pm.getProtocolMessageType()) {
                case ACCEPT:
                    acceptsSent.incrementAndGet();
                    break;

                case CONNECT:
                    connectsSent.incrementAndGet();
                    break;

                case REJECT:
                    rejectsSent.incrementAndGet();
                    break;

                case KEEP_ALIVE:
                    keepAlivesSent.incrementAndGet();
                    break;
                case DISCONNECT:
                    disconnectsSent.incrementAndGet();
                    break;

                case MESSAGE_REQUEST:
                    messageRequestsSent.incrementAndGet();
                    nioLogger.log(ALL, session, "CougarProtocolEncoder: Writing message of length %s", (((RequestMessage) pm).getPayload().length + 8));
                    break;
                case MESSAGE_RESPONSE:
                    messageRequestsSent.incrementAndGet();
                    nioLogger.log(ALL, session, "CougarProtocolEncoder: Writing message of length %s", ((ResponseMessage) pm).getPayload().length);
                    break;

                case EVENT:
                    eventsSent.incrementAndGet();
                    nioLogger.log(ALL, session, "CougarProtocolEncoder: Writing event of length %s", ((EventMessage) pm).getPayload().length);
                    break;
                case SUSPEND:
                    suspendsSent.incrementAndGet();
                    break;

                case START_TLS_REQUEST:
                    tlsRequestsSent.incrementAndGet();
                    break;
                case START_TLS_RESPONSE:
                    tlsResponsesSent.incrementAndGet();
                    break;

                default:
                    badMessagesRequested.incrementAndGet();
                    throw new IllegalArgumentException("Unknown ProtocolMessage [" + ((ProtocolMessage) message).getProtocolMessageType() + "] received");

            }
        } else {
            throw new IllegalArgumentException("Unknown message type " + message);
        }
        buffer.flip();
        out.write(buffer);
        out.flush();
    }

    /**
     * Exports this service as an MBean, if the JMXControl is available
     */
    @Override
    public void export(JMXControl jmxControl) {
        if (jmxControl != null) {
            jmxControl.registerMBean("CoUGAR.socket.transport:name=encoder", this);
        }
    }

    @ManagedAttribute
    public long getMessageRequestsSent() {
        return messageRequestsSent.get();
    }

    @ManagedAttribute
    public long getMessageResponsesSent() {
        return messageResponsesSent.get();
    }

    @ManagedAttribute
    public long getEventsSent() {
        return eventsSent.get();
    }

    @ManagedAttribute
    public long getKeepAlivesSent() {
        return keepAlivesSent.get();
    }

    @ManagedAttribute
    public long getRejectsSent() {
        return rejectsSent.get();
    }

    @ManagedAttribute
    public long getConnectsSent() {
        return connectsSent.get();
    }

    @ManagedAttribute
    public long getAcceptsSent() {
        return acceptsSent.get();
    }

    @ManagedAttribute
    public long getBadMessagesRequested() {
        return badMessagesRequested.get();
    }

    @ManagedAttribute
    public long getSuspendsSent() {
        return suspendsSent.get();
    }

    @ManagedAttribute
    public long getTlsRequestsSent() {
        return tlsRequestsSent.get();
    }

    @ManagedAttribute
    public long getTlsResponsesSent() {
        return tlsResponsesSent.get();
    }
}
