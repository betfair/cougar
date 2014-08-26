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
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.mina.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.PROTOCOL;
import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.SESSION;

@ManagedResource
public class CougarProtocol1 extends IoFilterAdapter implements Exportable, ICougarProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(CougarProtocol1.class);

    private static final KeepAliveMessage KEEP_ALIVE = new KeepAliveMessage();


    private final NioLogger nioLogger;
    private final byte applicationVersion;
    private boolean isServer;

    private volatile boolean isEnabled = false;

    private final int interval ;
    private final int timeout ;

    private final AtomicLong heartbeatsMissed = new AtomicLong();
    private final AtomicLong heartbeatsSent = new AtomicLong();
    private final AtomicLong sessionsCreated = new AtomicLong();

    private String lastSessionFrom = null;



    public CougarProtocol1(boolean server, NioLogger nioLogger, byte applicationVersion, int keepAliveInterval, int keepAliveTimeout) {
        this.isServer = server;
        this.nioLogger = nioLogger;
        this.applicationVersion = applicationVersion;
        this.interval = keepAliveInterval;
        this.timeout = keepAliveTimeout;
        export(nioLogger.getJmxControl());

    }

    public void closeSession(IoSession ioSession) {
        WriteFuture future = ioSession.write(new DisconnectMessage());
        future.addListener(new IoFutureListener() {

            @Override
            public void operationComplete(IoFuture future) {
                future.getSession().close();

            }
        });
    }


    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
        if (!isServer) {
            ClientHandshake clientHandshake = new ClientHandshake();
            session.setAttribute(ClientHandshake.HANDSHAKE, clientHandshake);
            session.write(new ConnectMessage(new byte[] {applicationVersion}));
        }
        super.sessionOpened(nextFilter,session);
    }


    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        try {
            if (status == IdleStatus.WRITER_IDLE) {
                nioLogger.log(PROTOCOL, session, "CougarProtocolCodecFilter: sending KEEP_ALIVE");
                session.write(KEEP_ALIVE);
                heartbeatsSent.incrementAndGet();
            } else {
                nioLogger.log(PROTOCOL, session, "CougarProtocolCodecFilter: KEEP_ALIVE timeout closing session");
                session.close();
                heartbeatsMissed.incrementAndGet();
            }
        } finally {
            nextFilter.sessionIdle(session, status);
        }
    }

    @Override
    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
        session.setIdleTime(IdleStatus.READER_IDLE, timeout);
        session.setIdleTime(IdleStatus.WRITER_IDLE, interval);
        nextFilter.sessionCreated(session);

        nioLogger.log(SESSION, session, "CougarProtocolCodecFilter: Created session at %s from %s", session.getCreationTime(), session.getRemoteAddress());
        sessionsCreated.incrementAndGet();
        lastSessionFrom = session.getRemoteAddress().toString();
    }


    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (message instanceof byte[]) {
            super.messageReceived(nextFilter,session,message);
        }
        else if (message instanceof ProtocolMessage) {
            ProtocolMessage protocolMessage = (ProtocolMessage) message;
            switch (protocolMessage.getProtocolMessageType()) {
                case CONNECT:
                    if (isEnabled()) {
                        ConnectMessage connectMessage = (ConnectMessage) protocolMessage;
                        //As a server, ensure that we support the version the client is expecting us to communicate with
                        if (Arrays.binarySearch(connectMessage.getApplicationVersions(), applicationVersion) >= 0) {
                            nioLogger.log(PROTOCOL, session, "CougarProtocolDecoder: ACCEPTing connection request with version %s", applicationVersion);
                            session.write(new AcceptMessage(applicationVersion));
                        } else {
                            //we don't speak your language. goodbye
                            nioLogger.log(PROTOCOL, session, "CougarProtocolDecoder: REJECTing connection request with versions %s", getAsString(connectMessage.getApplicationVersions()));
                            LOG.info("REJECTing connection request from session "+session.getRemoteAddress()+" with versions "+ getAsString(connectMessage.getApplicationVersions()));
                            session.write(new RejectMessage(RejectMessageReason.INCOMPATIBLE_VERSION,new byte[] {applicationVersion}));
                        }
                    }
                    else {
                        nioLogger.log(PROTOCOL, session, "REJECTing connection request from session %s as service unavailable", session.getReadMessages());
                        LOG.info("REJECTing connection request from session " + session.getReadMessages() + " as service unavailable");
                        session.write(new RejectMessage(RejectMessageReason.SERVER_UNAVAILABLE, new byte[] {applicationVersion}));
                    }
                    break;
                case ACCEPT:
                    //Client Side - server has accepted our connection request
                    AcceptMessage acceptMessage = (AcceptMessage) protocolMessage;
                    if (acceptMessage.getAcceptedVersion() != applicationVersion) {
                        session.close();
                        throw new IllegalStateException("Protocol version mismatch - client version is "+applicationVersion+", server has accepted "+acceptMessage.getAcceptedVersion());
                    }
                    nioLogger.log(PROTOCOL, session, "CougarProtocolDecoder: ACCEPT received for with version %s", acceptMessage.getAcceptedVersion());
                    ((ClientHandshake)session.getAttribute(ClientHandshake.HANDSHAKE)).accept();
                    break;
                case REJECT:
                    //Client Side - server has said foxtrot oscar
                    RejectMessage rejectMessage = (RejectMessage) protocolMessage;
                    nioLogger.log(PROTOCOL, session, "CougarProtocolDecoder: REJECT received: versions accepted are %s", getAsString(rejectMessage.getAcceptableVersions()));
                    ClientHandshake handshake = (ClientHandshake)session.getAttribute(ClientHandshake.HANDSHAKE);
                    if (handshake != null) {
                        handshake.reject();
                    }
                    break;
                case KEEP_ALIVE:
                    //Both sides keep alive received, which is ignored
                    nioLogger.log(PROTOCOL, session, "CougarProtocolDecoder: KEEP_ALIVE received");
                    break;
                case DISCONNECT:
                    //Client Side - server doesn't love us anymore
                    session.setAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name());
                    session.close();
                    break;
                default:
                    LOG.error("Unknown message type "+protocolMessage.getProtocolMessageType()+" - Ignoring");

            }
        }
    }

    private String getAsString(byte[] versions) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (byte b: versions) {
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

    @ManagedAttribute
    public void setEnabled(boolean healthy) {
        this.isEnabled = healthy;
    }

    @ManagedAttribute
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Exports this service as an MBean, if the JMXControl is available
     */
    @Override
    public void export(JMXControl jmxControl) {
        if (jmxControl != null) {
            jmxControl.registerMBean("CoUGAR.socket.transport:name=wireProtocol", this);
        }
    }

    @ManagedAttribute
    public int getInterval() {
        return interval;
    }

    @ManagedAttribute
    public int getTimeout() {
        return timeout;
    }

    @ManagedAttribute
    public long getHeartbeatsMissed() {
        return heartbeatsMissed.get();
    }

    @ManagedAttribute
    public long getHeartbeatsSent() {
        return heartbeatsSent.get();
    }

    @ManagedAttribute
    public long getSessionsCreated() {
        return sessionsCreated.get();
    }

    @ManagedAttribute
    public String getLastSessionFrom() {
        return lastSessionFrom;
    }

}
