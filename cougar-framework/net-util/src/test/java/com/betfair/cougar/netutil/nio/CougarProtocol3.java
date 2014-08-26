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

import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.netutil.nio.message.AcceptMessage;
import com.betfair.cougar.netutil.nio.message.ConnectMessage;
import com.betfair.cougar.netutil.nio.message.DisconnectMessage;
import com.betfair.cougar.netutil.nio.message.KeepAliveMessage;
import com.betfair.cougar.netutil.nio.message.ProtocolMessage;
import com.betfair.cougar.netutil.nio.message.RejectMessage;
import com.betfair.cougar.netutil.nio.message.RejectMessageReason;
import com.betfair.cougar.netutil.nio.message.StartTLSRequestMessage;
import com.betfair.cougar.netutil.nio.message.StartTLSResponseMessage;
import com.betfair.cougar.netutil.nio.message.SuspendMessage;
import com.betfair.cougar.netutil.nio.message.TLSRequirement;
import com.betfair.cougar.netutil.nio.message.TLSResult;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.SSLFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.PROTOCOL;
import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.SESSION;

@ManagedResource
public class CougarProtocol3 extends IoFilterAdapter implements Exportable, ICougarProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(CougarProtocol.class);

    private static final KeepAliveMessage KEEP_ALIVE = new KeepAliveMessage();

    public static final String PROTOCOL_VERSION_ATTR_NAME = "CougarProtocol.sessionProtocolVersion";
    public static final String IS_SERVER_ATTR_NAME = "CougarProtocol.isServer";
    public static final String NEGOTIATED_TLS_LEVEL_ATTR_NAME = "CougarProtocol.negotiatedTlsLevel";
    public static final String CLIENT_CERTS_ATTR_NAME = "CougarProtocol.clientCertificateChain";
    public static final String TSSF_ATTR_NAME = "CougarProtocol.transportSecurityStrengthFactor";

    public static final byte APPLICATION_PROTOCOL_VERSION_CLIENT_ONLY_RPC = 1;
    public static final byte APPLICATION_PROTOCOL_VERSION_BIDIRECTION_RPC = 2;
    public static final byte APPLICATION_PROTOCOL_VERSION_START_TLS = 3;
    public static final byte APPLICATION_PROTOCOL_VERSION_MIN_SUPPORTED = APPLICATION_PROTOCOL_VERSION_CLIENT_ONLY_RPC;
    public static final byte APPLICATION_PROTOCOL_VERSION_MAX_SUPPORTED = APPLICATION_PROTOCOL_VERSION_START_TLS;
    public static final byte APPLICATION_PROTOCOL_VERSION_UNSUPPORTED = APPLICATION_PROTOCOL_VERSION_MIN_SUPPORTED - 1;

    // these allow tests to force us to particular versions of the protocol, even invalid ones
    private static byte maxServerProtocolVersion = APPLICATION_PROTOCOL_VERSION_MAX_SUPPORTED;
    private static byte maxClientProtocolVersion = APPLICATION_PROTOCOL_VERSION_MAX_SUPPORTED;
    private static byte minServerProtocolVersion = APPLICATION_PROTOCOL_VERSION_MIN_SUPPORTED;
    private static byte minClientProtocolVersion = APPLICATION_PROTOCOL_VERSION_MIN_SUPPORTED;

    public static void setMaxServerProtocolVersion(byte maxServerProtocolVersion) {
        CougarProtocol3.maxServerProtocolVersion = maxServerProtocolVersion;
    }

    public static void setMaxClientProtocolVersion(byte maxClientProtocolVersion) {
        CougarProtocol3.maxClientProtocolVersion = maxClientProtocolVersion;
    }

    public static void setMinServerProtocolVersion(byte minServerProtocolVersion) {
        CougarProtocol3.minServerProtocolVersion = minServerProtocolVersion;
    }

    public static void setMinClientProtocolVersion(byte minClientProtocolVersion) {
        CougarProtocol3.minClientProtocolVersion = minClientProtocolVersion;
    }

    private byte[] getServerAcceptableVersions() {
        byte[] ret = new byte[(maxServerProtocolVersion - minServerProtocolVersion) + 1];
        int ind = 0;
        for (byte i = maxServerProtocolVersion; i >= minServerProtocolVersion; i--) {
            ret[ind++] = i;
        }
        return ret;
    }

    private byte[] getClientAcceptableVersions() {
        byte[] ret = new byte[(maxClientProtocolVersion - minClientProtocolVersion) + 1];
        int ind = 0;
        for (byte i = minClientProtocolVersion; i <= maxClientProtocolVersion; i++) {
            ret[ind++] = i;
        }
        return ret;
    }

    private static Set<TranscribableParams>[] transcribableParamsByProtocolVersion;
    static {
        Set<TranscribableParams>[] map = new Set[APPLICATION_PROTOCOL_VERSION_MAX_SUPPORTED+1];
        map[APPLICATION_PROTOCOL_VERSION_CLIENT_ONLY_RPC] =  Collections.unmodifiableSet(EnumSet.noneOf(TranscribableParams.class));
        map[APPLICATION_PROTOCOL_VERSION_BIDIRECTION_RPC] =  Collections.unmodifiableSet(EnumSet.noneOf(TranscribableParams.class));
        map[APPLICATION_PROTOCOL_VERSION_START_TLS] =  Collections.unmodifiableSet(EnumSet.of(TranscribableParams.EnumsWrittenAsStrings, TranscribableParams.MajorOnlyPackageNaming));
        transcribableParamsByProtocolVersion = map;
    }

    public static Set<TranscribableParams> getTranscribableParamSet(IoSession session) {
        return getTranscribableParamSet(getProtocolVersion(session));
    }

    public static Set<TranscribableParams> getTranscribableParamSet(byte protocolVersion) {
        return transcribableParamsByProtocolVersion[protocolVersion];
    }

    private final NioLogger nioLogger;
    private boolean isServer;

    private volatile boolean isEnabled = false;

    private final int interval;
    private final int timeout;

    private final AtomicLong heartbeatsMissed = new AtomicLong();
    private final AtomicLong heartbeatsSent = new AtomicLong();
    private final AtomicLong sessionsCreated = new AtomicLong();

    private String lastSessionFrom = null;

    private final SSLFilter sslFilter;
    private final boolean supportsTls;
    private final boolean requiresTls;

    private final long rpcTimeoutMillis;

    public static CougarProtocol3 getClientInstance(NioLogger nioLogger, int keepAliveInterval, int keepAliveTimeout, SSLFilter sslFilter, boolean supportsTls, boolean requiresTls, long rpcTimeoutMillis) {
        return new CougarProtocol3(false, nioLogger, keepAliveInterval, keepAliveTimeout, sslFilter, supportsTls, requiresTls, rpcTimeoutMillis);
    }

    public static CougarProtocol3 getServerInstance(NioLogger nioLogger, int keepAliveInterval, int keepAliveTimeout, SSLFilter sslFilter, boolean supportsTls, boolean requiresTls) {
        return new CougarProtocol3(true, nioLogger, keepAliveInterval, keepAliveTimeout, sslFilter, supportsTls, requiresTls, 0);
    }

    protected CougarProtocol3(boolean server, NioLogger nioLogger, int keepAliveInterval, int keepAliveTimeout, SSLFilter sslFilter, boolean supportsTls, boolean requiresTls, long rpcTimeoutMillis) {
        this.isServer = server;
        this.nioLogger = nioLogger;
        this.interval = keepAliveInterval;
        this.timeout = keepAliveTimeout;
        this.sslFilter = sslFilter;
        this.supportsTls = supportsTls;
        this.requiresTls = requiresTls;
        this.rpcTimeoutMillis = rpcTimeoutMillis;
        export(nioLogger.getJmxControl());
    }

    public void closeSession(final IoSession ioSession) {
        closeSession(ioSession, false);
    }

    public void closeSession(final IoSession ioSession, boolean blockUntilComplete) {
        WriteFuture future = ioSession.write(new DisconnectMessage());
        final AtomicReference<CloseFuture> closeFuture = new AtomicReference<CloseFuture>();
        final CountDownLatch latch = new CountDownLatch(1);
        future.addListener(new IoFutureListener() {

            @Override
            public void operationComplete(IoFuture future) {
                nioLogger.log(NioLogger.LoggingLevel.SESSION, ioSession, "CougarProtocol - Closing session after disconnection");
                closeFuture.set(future.getSession().close());
                latch.countDown();

            }
        });
        if (blockUntilComplete) {
            try {
                future.join();
                latch.await();
                closeFuture.get().join();
            }
            catch (InterruptedException ie) {
                // ignore, this shouldn't happen, and tends only to be used for tests
            }
        }
    }

    public void suspendSession(final IoSession ioSession) {
        final Byte protocolVersion = (Byte) ioSession.getAttribute(PROTOCOL_VERSION_ATTR_NAME);
        if (protocolVersion == null || protocolVersion.equals(APPLICATION_PROTOCOL_VERSION_CLIENT_ONLY_RPC)) {
            return; // We don't need to do this for clients using older version, as they don't understand this message
        }

        WriteFuture future = ioSession.write(new SuspendMessage());
        future.addListener(new IoFutureListener() {

            @Override
            public void operationComplete(IoFuture future) {
                nioLogger.log(NioLogger.LoggingLevel.SESSION, ioSession, "CougarProtocol - Suspended session");
            }
        });

    }


    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
        if (!isServer) {
            ClientHandshake clientHandshake = new ClientHandshake();
            session.setAttribute(ClientHandshake.HANDSHAKE, clientHandshake);
            session.write(new ConnectMessage(getClientAcceptableVersions()));
        }
        super.sessionOpened(nextFilter, session);
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


    public static byte getProtocolVersion(IoSession session) {
        Byte b = (Byte) session.getAttribute(PROTOCOL_VERSION_ATTR_NAME);
        if (b == null) {
            throw new IllegalStateException("Protocol version requested for session before determined");
        }
        return b;
    }

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (message instanceof ProtocolMessage) {
            ProtocolMessage protocolMessage = (ProtocolMessage) message;
            switch (protocolMessage.getProtocolMessageType()) {
                case CONNECT:
                    // server side - request to connect from client
                    if (isEnabled()) {
                        ConnectMessage connectMessage = (ConnectMessage) protocolMessage;
                        //As a server, ensure that we support a version the client also supports
                        byte protocolVersionToUse = APPLICATION_PROTOCOL_VERSION_UNSUPPORTED;
                        for (byte testVersion = maxServerProtocolVersion; testVersion >= minServerProtocolVersion; testVersion--) {
                            if (Arrays.binarySearch(connectMessage.getApplicationVersions(), testVersion) >= 0) {
                                protocolVersionToUse = testVersion;
                                break;
                            }
                        }
                        if (protocolVersionToUse >= minServerProtocolVersion) {
                            // older versions of the protocol don't support TLS, so if we require it, then we have to stop here
                            if (protocolVersionToUse < APPLICATION_PROTOCOL_VERSION_START_TLS && requiresTls) {
                                nioLogger.log(PROTOCOL, session, "CougarProtocol: REJECTing connection request with version %s since we require TLS, which is not supported on this version", protocolVersionToUse);
                                session.write(new RejectMessage(RejectMessageReason.INCOMPATIBLE_VERSION, getServerAcceptableVersions()));
                                session.close();
                            }
                            else {
                                nioLogger.log(PROTOCOL, session, "CougarProtocol: ACCEPTing connection request with version %s", protocolVersionToUse);
                                session.setAttribute(PROTOCOL_VERSION_ATTR_NAME, protocolVersionToUse);
                                session.setAttribute(IS_SERVER_ATTR_NAME, true);
                                // this is used for all writes to the session after the initial handshaking
                                session.setAttribute(RequestResponseManager.SESSION_KEY, new RequestResponseManagerImpl(session, nioLogger, rpcTimeoutMillis));
                                session.write(new AcceptMessage(protocolVersionToUse));
                            }
                        } else {
                            //we don't speak your language. goodbye
                            nioLogger.log(PROTOCOL, session, "CougarProtocol: REJECTing connection request with versions %s", getAsString(connectMessage.getApplicationVersions()));
                            LOG.info("REJECTing connection request from session " + session.getRemoteAddress() + " with versions " + getAsString(connectMessage.getApplicationVersions()));
                            session.write(new RejectMessage(RejectMessageReason.INCOMPATIBLE_VERSION, getServerAcceptableVersions()));
                            session.close();
                        }
                    } else {
                        nioLogger.log(PROTOCOL, session, "REJECTing connection request from session %s as service unavailable", session.getReadMessages());
                        LOG.info("REJECTing connection request from session " + session.getReadMessages() + " as service unavailable");
                        session.write(new RejectMessage(RejectMessageReason.SERVER_UNAVAILABLE, getServerAcceptableVersions()));
                        session.close();
                    }
                    break;
                case ACCEPT:
                    //Client Side - server has accepted our connection request
                    AcceptMessage acceptMessage = (AcceptMessage) protocolMessage;
                    if (acceptMessage.getAcceptedVersion() < minClientProtocolVersion || acceptMessage.getAcceptedVersion() > maxClientProtocolVersion) {
                        nioLogger.log(PROTOCOL, session, "Protocol version mismatch - client version is %s, server has accepted %s", maxClientProtocolVersion, acceptMessage.getAcceptedVersion());
                        session.close();
                        throw new IllegalStateException("Protocol version mismatch - client version is " + maxClientProtocolVersion + ", server has accepted " + acceptMessage.getAcceptedVersion());
                    }
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: ACCEPT received for with version %s", acceptMessage.getAcceptedVersion());

                    session.setAttribute(IS_SERVER_ATTR_NAME, false);
                    session.setAttribute(PROTOCOL_VERSION_ATTR_NAME, acceptMessage.getAcceptedVersion());
                    session.setAttribute(RequestResponseManager.SESSION_KEY, new RequestResponseManagerImpl(session, nioLogger, rpcTimeoutMillis));

                    // if we're running version 3 or later then send our TLS request, otherwise we're done handshaking
                    if (acceptMessage.getAcceptedVersion() >= APPLICATION_PROTOCOL_VERSION_START_TLS) {
                        TLSRequirement requirement;
                        if (requiresTls) {
                            requirement = TLSRequirement.REQUIRED;
                        }
                        else if (supportsTls) {
                            requirement = TLSRequirement.SUPPORTED;
                        }
                        else {
                            requirement = TLSRequirement.NONE;
                        }
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: Server version supports TLS, sending requirement of %s", requirement);
                        session.write(new StartTLSRequestMessage(requirement));
                    }
                    // if we had to have tls, but the server is running an old version then we need to disconnect
                    else if (requiresTls) {
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: Server version doesn't support TLS, sending DISCONNECT");
                        session.write(new DisconnectMessage());
                        ClientHandshake handshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
                        if (handshake != null) {
                            handshake.reject();
                        }
                        session.close();
                    }
                    else {
                        ClientHandshake handshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
                        if (handshake != null) {
                            handshake.accept();
                        }
                    }
                    break;
                case REJECT:
                    //Client Side - server has said foxtrot oscar
                    RejectMessage rejectMessage = (RejectMessage) protocolMessage;
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: REJECT received: versions accepted are %s", getAsString(rejectMessage.getAcceptableVersions()));
                    ClientHandshake handshake2 = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
                    if (handshake2 != null) {
                        handshake2.reject();
                    }
                    session.close();
                    break;
                case START_TLS_REQUEST:
                    // server side - client has sent it's tls requirements
                    StartTLSRequestMessage tlsRequestMessage = (StartTLSRequestMessage) protocolMessage;
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: START_TLS_REQUEST - requirement is %s", tlsRequestMessage.getRequirement());
                    TLSResult result;
                    switch (tlsRequestMessage.getRequirement()) {
                        case NONE:
                            if (requiresTls) {
                                result = TLSResult.FAILED_NEGOTIATION;
                            }
                            else {
                                result = TLSResult.PLAINTEXT;
                            }
                            break;
                        case SUPPORTED:
                            if (supportsTls) {
                                result = TLSResult.SSL;
                            }
                            else {
                                result = TLSResult.PLAINTEXT;
                            }
                            break;
                        case REQUIRED:
                            if (supportsTls) {
                                result = TLSResult.SSL;
                            }
                            else {
                                result = TLSResult.FAILED_NEGOTIATION;
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unsupported TLS requirement received " + tlsRequestMessage.getRequirement());
                    }
                    StartTLSResponseMessage tlsResponseMessage = new StartTLSResponseMessage(result);
                    if (result != TLSResult.FAILED_NEGOTIATION) {
                        session.setAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME, result);
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: START_TLS_REQUEST - successfully negotiated %s comms", result);
                        if (result == TLSResult.SSL) {
                            // Insert SSLFilter to get ready for handshaking
                            session.getFilterChain().addFirst("ssl", sslFilter);

                            // Disable encryption temporarilly.
                            // This attribute will be removed by SSLFilter
                            // inside the Session.write() call below.
                            session.setAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);
                        }
                    }
                    session.write(tlsResponseMessage);
                    if (result == TLSResult.SSL) {
                        // Now DISABLE_ENCRYPTION_ONCE attribute is cleared.
                        assert session.getAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE) == null;
                    }
                    else if (result == TLSResult.FAILED_NEGOTIATION) {
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: START_TLS_REQUEST - negotiation failed, closing session");
                        session.close();
                    }

                    break;
                case START_TLS_RESPONSE:
                    // client side - server has determined our TLS settings for this connection
                    StartTLSResponseMessage responseMessage = (StartTLSResponseMessage) protocolMessage;
                    if (responseMessage.getResult() == TLSResult.FAILED_NEGOTIATION) {
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: START_TLS_RESPONSE - FAILED_NEGOTIATION received");
                        ClientHandshake handshake3 = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
                        if (handshake3 != null) {
                            handshake3.reject();
                        }
                        session.close();
                    }
                    else {
                        session.setAttribute(CougarProtocol.NEGOTIATED_TLS_LEVEL_ATTR_NAME, responseMessage.getResult());
                        nioLogger.log(PROTOCOL, session, "CougarProtocol: Starting %s comms following successful TLS negotiation", responseMessage.getResult());

                        if (responseMessage.getResult() == TLSResult.SSL) {
                            // Insert SSLFilter to get ready for handshaking
                            session.getFilterChain().addFirst("ssl", sslFilter);
                        }

                        // finish handshaking
                        ClientHandshake handshake = (ClientHandshake) session.getAttribute(ClientHandshake.HANDSHAKE);
                        if (handshake != null) {
                            handshake.accept();
                        }
                    }
                    break;

                case KEEP_ALIVE:
                    //Both sides keep alive received, which is ignored
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: KEEP_ALIVE received");
                    break;
                case DISCONNECT:
                    //Client Side - server doesn't love us anymore
                    session.setAttribute(ProtocolMessage.ProtocolMessageType.DISCONNECT.name());
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: DISCONNECT received");
                    session.close();
                    break;
                case SUSPEND:
                    //Client Side - this session is about to be closed
                    session.setAttribute(ProtocolMessage.ProtocolMessageType.SUSPEND.name());
                    nioLogger.log(PROTOCOL, session, "CougarProtocol: SUSPEND received");
                    break;
                case MESSAGE_REQUEST:
                case MESSAGE_RESPONSE:
                case EVENT:
                    super.messageReceived(nextFilter, session, message);
                    break;
                default:
                    LOG.error("Unknown message type " + protocolMessage.getProtocolMessageType() + " - Ignoring");

            }
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

    @ManagedAttribute
    public boolean isSupportsTls() {
        return supportsTls;
    }

    @ManagedAttribute
    public boolean isRequiresTls() {
        return requiresTls;
    }

    // for testing
    SSLFilter getSslFilter() {
        return sslFilter;
    }
}
