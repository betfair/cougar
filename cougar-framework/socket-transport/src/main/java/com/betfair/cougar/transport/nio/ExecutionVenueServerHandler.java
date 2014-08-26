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

import com.betfair.cougar.netutil.nio.*;
import com.betfair.cougar.netutil.nio.message.EventMessage;
import com.betfair.cougar.netutil.nio.message.RequestMessage;
import com.betfair.cougar.netutil.nio.message.ResponseMessage;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.api.protocol.socket.SocketBindingDescriptor;
import com.betfair.cougar.transport.socket.SocketTransportCommand;
import com.betfair.cougar.transport.socket.SocketTransportCommandImpl;
import com.betfair.cougar.transport.socket.SocketTransportCommandProcessor;
import com.betfair.cougar.transport.socket.SocketTransportRPCCommandImpl;
import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.ALL;
import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.SESSION;

@ManagedResource
public class ExecutionVenueServerHandler extends IoHandlerAdapter implements Exportable {

    private final static Logger LOG = LoggerFactory.getLogger(ExecutionVenueServerHandler.class);

    private TransportCommandProcessor<SocketTransportCommand> processor;
    private final NioLogger sessionLogger;

    private final AtomicLong requestsReceived = new AtomicLong();
    private final AtomicLong eventsReceived = new AtomicLong();
    private final AtomicLong sessionsOpened = new AtomicLong();
    private final AtomicLong sessionsClosed = new AtomicLong();
    private final AtomicLong otherExceptions = new AtomicLong();
    private final AtomicLong ioExceptions = new AtomicLong();

    private final CougarObjectIOFactory objectIOFactory;
    private List<HandlerListener> listeners = new CopyOnWriteArrayList<HandlerListener>();
    private final ConcurrentHashMap<IoSession, String> sessions = new ConcurrentHashMap<IoSession, String>();

    public void addListener(HandlerListener listener) {
        listeners.add(listener);
    }

    protected ExecutionVenueServerHandler() {
        sessionLogger = null;
        objectIOFactory = null;
    }

    public ExecutionVenueServerHandler(NioLogger sessionLogger, TransportCommandProcessor<SocketTransportCommand> processor,
                                       CougarObjectIOFactory objectIOFactory) {
        this.sessionLogger = sessionLogger;
        this.processor = processor;
        this.objectIOFactory = objectIOFactory;

        export(sessionLogger.getJmxControl());
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof ResponseMessage) {
            ((IoHandler) session.getAttribute(RequestResponseManager.SESSION_KEY)).messageReceived(session, message);
        }
        else if (message instanceof RequestMessage) {
            RequestMessage req = (RequestMessage) message;

            final CougarObjectOutput out = objectIOFactory.newCougarObjectOutput(new ByteArrayOutputStreamWithIoSession(session, req.getCorrelationId()), CougarProtocol.getProtocolVersion(session));
            final CougarObjectInput in = objectIOFactory.newCougarObjectInput(new ByteArrayInputStream(req.getPayload()), CougarProtocol.getProtocolVersion(session));
            final String remoteAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
            final SocketTransportCommandImpl command = new SocketTransportRPCCommandImpl(in, out, remoteAddress, session);

            requestsReceived.incrementAndGet();
            processor.process(command);

            sessionLogger.log(ALL, session, "ExecutionVenueServerHandler - Message %s processed", req.getCorrelationId());
        }
        else if (message instanceof EventMessage) {
            EventMessage em = (EventMessage) message;

            final CougarObjectInput in = objectIOFactory.newCougarObjectInput(new ByteArrayInputStream(em.getPayload()), CougarProtocol.getProtocolVersion(session));
            final String remoteAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
            final SocketTransportCommandImpl command = new SocketTransportCommandImpl(in, remoteAddress, session);

            eventsReceived.incrementAndGet();
            processor.process(command);
        } else {
            LOG.warn("ExecutionVenueServerHandler - Received unexpected message type: " + message + " - closing session");
            sessionLogger.log(NioLogger.LoggingLevel.SESSION, session, "ExecutionVenueServerHandler - Received unexpected message type: %s - closing session", message);
            session.close();
        }

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);

        sessionsClosed.incrementAndGet();
        sessions.remove(session);
        sessionLogger.log(SESSION, session, "ExecutionVenueServerHandler: Session closed");

        for (HandlerListener listener : listeners) {
            listener.sessionClosed(session);
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);

        sessionsOpened.incrementAndGet();
        sessions.put(session, "");
        sessionLogger.log(SESSION, session, "ExecutionVenueServerHandler: Session opened");

        for (HandlerListener listener : listeners) {
            listener.sessionOpened(session);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            ioExceptions.incrementAndGet();

            // We arrive here when the output pipe is broken. Broken network connections are not
            // really exceptional and should not be reported by dumping the stack trace.
            // Instead a summary debug level log message with some relevant info
            sessionLogger.log(ALL, session, "ExecutionVenueServerHandler: IOException received on session - closing");
        } else {
            otherExceptions.incrementAndGet();
            sessionLogger.log(SESSION, session, "ExecutionVenueServerHandler: Unexpected exception from session - see main log for details");
            LOG.warn("Unexpected exception from session " + NioUtils.getSessionId(session), cause);
        }
        session.close();
    }

    public void notify(SocketBindingDescriptor bindingDescriptor) {
        processor.bind(bindingDescriptor);
    }

    /**
     * Exports this service as an MBean, if the JMXControl is available
     */
    @Override
    public void export(JMXControl jmxControl) {
        if (jmxControl != null) {
            jmxControl.registerMBean("CoUGAR.socket.transport:name=handler", this);
        }
    }

    @ManagedOperation
    public String getSessionsDetails(boolean html) {
        StringBuilder buffer = new StringBuilder();
        if (html) buffer.append("<pre>");
        for (IoSession session : sessions.keySet()) {
            buffer.append("SessionId=").append(NioUtils.getSessionId(session)).append(",")
                    .append("remoteHost=").append(session.getRemoteAddress()).append(",")
                    .append("connected=").append(session.isConnected()).append(",")
                    .append("closing=").append(session.isClosing())
                    .append('\n');
        }
        if (html) buffer.append("</pre>");
        return buffer.toString();
    }

    public long getOutstandingRequests() {
        return ((SocketTransportCommandProcessor)processor).getOutstandingRequests();
    }

    @ManagedAttribute
    public long getRequestsReceived() {
        return requestsReceived.get();
    }

    @ManagedAttribute
    public long getEventsReceived() {
        return eventsReceived.get();
    }

    @ManagedAttribute
    public long getSessionsOpened() {
        return sessionsOpened.get();
    }

    @ManagedAttribute
    public long getSessionsClosed() {
        return sessionsClosed.get();
    }

    @ManagedAttribute
    public long getIoExceptionsClosingSession() {
        return ioExceptions.get();
    }

    @ManagedAttribute
    public long getOtherExceptionsClosingSession() {
        return otherExceptions.get();
    }
}
