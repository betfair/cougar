/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.client.socket;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.client.CallContextFactory;
import com.betfair.cougar.client.ClientCallContext;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.client.socket.jmx.ClientSocketTransportInfo;
import com.betfair.cougar.client.socket.resolver.NetworkAddressResolver;
import com.betfair.cougar.core.api.client.AbstractClientTransport;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.impl.tracing.TracingEndObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.api.socket.RemotableMethodInvocationMarshaller;
import com.betfair.cougar.netutil.nio.*;
import com.betfair.cougar.netutil.nio.message.EventMessage;
import com.betfair.cougar.netutil.nio.message.ResponseMessage;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.transport.api.protocol.socket.InvocationRequest;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import com.betfair.cougar.util.jmx.JMXControl;
import com.caucho.hessian.io.HessianProtocolException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.mina.common.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

import static com.betfair.cougar.netutil.nio.NioLogger.LoggingLevel.*;

public class ExecutionVenueNioClient extends AbstractClientTransport implements ApplicationContextAware, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionVenueNioClient.class);

    private static final int DEFAULT_HANDSHAKE_RESPONSE_TIMEOUT = 5000;
    private static final int DEFAULT_RECONNECT_INTERVAL = 5000;
    private static final int DEFAULT_SESSION_RECYCLE_INTERVAL = 86400000;


    private final IoSessionFactory sessionFactory;

    private final CougarObjectIOFactory objectIOFactory;
    private ClientConnectedObjectManager connectedObjectManager;
    private List<HandlerListener> handlerListeners = new CopyOnWriteArrayList<HandlerListener>();
    private final NioLogger logger;
    private final String addressList;
    private ApplicationContext applicationContext;
    private boolean hardFailEnumDeserialisation;
    private RPCTimeoutChecker rpcTimeoutChecker;
    private Tracer tracer;

    public ExecutionVenueNioClient(NioLogger logger, NioConfig nioConfig, CougarObjectIOFactory objectIOFactory, ClientConnectedObjectManager connectedObjectManager, ClientSocketTransportInfo clientSocketTransportInfo, String addressList,
                                   JMXReportingThreadPoolExecutor ioExecutorService, JMXReportingThreadPoolExecutor reconnectExecutor, NetworkAddressResolver addressResolver, Tracer tracer) {
        this(logger, nioConfig, objectIOFactory, connectedObjectManager, clientSocketTransportInfo, addressList,
                ioExecutorService, reconnectExecutor, DEFAULT_RECONNECT_INTERVAL, DEFAULT_HANDSHAKE_RESPONSE_TIMEOUT, DEFAULT_SESSION_RECYCLE_INTERVAL, addressResolver, tracer);
    }

    public ExecutionVenueNioClient(NioLogger logger, NioConfig nioConfig, CougarObjectIOFactory objectIOFactory, ClientConnectedObjectManager connectedObjectManager, ClientSocketTransportInfo clientSocketTransportInfo, String addressList,
                                   JMXReportingThreadPoolExecutor ioExecutorService, JMXReportingThreadPoolExecutor reconnectExecutor,
                                   int reconnectInterval, int handshakeResponseTimeout, long sessionRecycleInterval,
                                   NetworkAddressResolver addressResolver, Tracer tracer) {
        this.logger = logger;
        this.tracer = tracer;
        this.sessionFactory = new IoSessionFactory(logger, addressList, ioExecutorService, reconnectExecutor,
                nioConfig, ioHandler, sessionCloseListener, reconnectInterval, handshakeResponseTimeout, sessionRecycleInterval, addressResolver);
        this.addressList = addressList;
        this.objectIOFactory = objectIOFactory;
        if (clientSocketTransportInfo != null) {
            addListener(clientSocketTransportInfo);
        }
        // make sure this parameter gets passed down to the RRM..
        if (nioConfig.getRpcTimeoutMillis() != 0) {
            rpcTimeoutChecker = new RPCTimeoutChecker(nioConfig.getRpcTimeoutGranularityMillis());
            addListener(rpcTimeoutChecker);
        }
        this.connectedObjectManager = connectedObjectManager;
    }

    public IoSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void addListener(HandlerListener listener) {
        handlerListeners.add(listener);
    }

    /**
     * Starts the client
     *
     * @return a Future<Boolean> that is true once the connection is established
     */
    public synchronized FutureTask<Boolean> start() {
        this.sessionFactory.start();
        if (rpcTimeoutChecker != null) {
            rpcTimeoutChecker.getThread().start();
        }
        final FutureTask<Boolean> futureTask = new FutureTask<Boolean>(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        while (!ExecutionVenueNioClient.this.sessionFactory.isConnected()) {
                            Thread.sleep(50);
                        }
                        return true;
                    }
                });
        final Thread thread = new Thread(futureTask);
        thread.setDaemon(true);
        thread.start();
        return futureTask;
    }


    /**
     * Stops the client.
     */
    public synchronized FutureTask<Boolean> stop() {
        this.sessionFactory.stop();
        if (rpcTimeoutChecker != null) {
            rpcTimeoutChecker.stop();
        }
        final FutureTask<Boolean> futureTask = new FutureTask<Boolean>(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        while (ExecutionVenueNioClient.this.sessionFactory.isConnected()) {
                            Thread.sleep(50);
                        }
                        return true;
                    }
                });
        final Thread thread = new Thread(futureTask);
        thread.setDaemon(true);
        thread.start();
        return futureTask;
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder("ExecutionVenueNioClient[connectedTo=")
                        .append(getSessionFactory().getConnectedStatus())
                        .append(']');
        return sb.toString();
    }

    // ####################################################
    private final IoHandler ioHandler = new IoHandlerAdapter() {

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            for (HandlerListener listener : handlerListeners) {
                listener.sessionOpened(session);
            }
        }

        @Override
        public void messageReceived(final IoSession session, final Object message) throws Exception {
            RequestResponseManager requestResponseManager = (RequestResponseManager) session.getAttribute(RequestResponseManager.SESSION_KEY);
            if (message instanceof ResponseMessage) {
                if (requestResponseManager != null) {
                    requestResponseManager.messageReceived(session, message);
                }
            } else if (message instanceof EventMessage) {
                EventMessage em = (EventMessage) message;
                CougarObjectInput input = objectIOFactory.newCougarObjectInput(new ByteArrayInputStream(em.getPayload()), CougarProtocol.getProtocolVersion(session));
                Object payload = input.readObject();
                if (payload instanceof HeapDelta) {
                    connectedObjectManager.applyDelta(session, (HeapDelta) payload);
                } else if (payload instanceof TerminateSubscription) {
                    connectedObjectManager.terminateSubscription(session, (TerminateSubscription) payload);
                }
            } else {
                logger.log(PROTOCOL, session, "Received unsupported message: %s", message);
                LOG.warn("Received unsupported message: " + message);
            }
        }

        @Override
        public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
            if (cause instanceof IOException && !logException(cause)) {
                // We arrive here when the output pipe is broken. Broken network connections are not
                // really exceptional and should not be reported by dumping the stack trace.
                // Instead a summary debug level log message with some relevant info
                 logger.log(ALL, session, "ExecutionVenueNioClient: IOException received on session - closing",cause);
            } else {
                logger.log(SESSION, session, "ExecutionVenueNioClient: Unexpected exception from session - see main log for details");
                LOG.warn("Unexpected exception from session " + NioUtils.getSessionId(session), cause);
            }
            sessionFactory.close(session);
        }

        private boolean logException(Throwable t) {
            if (causeContainsHessianProtocolException(t)) {
                return true;
            }
            else if (sslException(t)) {
                return true;
            }
            return false;
        }

        private boolean sslException(Throwable t) {
            return t instanceof SSLException;
        }

        private boolean causeContainsHessianProtocolException(Throwable t) {
            Throwable cause = t;
            while (cause != null) {
                if (cause instanceof HessianProtocolException) {
                    return true;
                }
                cause = cause.getCause();
            }
            return false;
        }
    };

    // #####################################################
    private IoFutureListener sessionCloseListener = new IoFutureListener() {
        public void operationComplete(IoFuture future) {
            final IoSession session = future.getSession();
            if (session != null) {
                LOG.info("session is closing " + session.getCreationTime());
                notifyConnectionLost(session);
            }
        }
    };

    public int getOutstandingRequestCount(IoSession session) {
        RequestResponseManager manager = (RequestResponseManager) session.getAttribute(RequestResponseManager.SESSION_KEY);
        return manager != null ? manager.getOutstandingRequestCount() : 0;
    }

    // ================================ Executable ==========

    private RemotableMethodInvocationMarshaller marshaller;
    private ContextEmitter<Map<String, String>, ?> contextEmitter;

    @Override
    public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer,
                        ExecutionVenue executionVenue, TimeConstraints timeConstraints) {

        execute(ctx, executionVenue.getOperationDefinition(key), args, observer, timeConstraints);
    }

    public void execute(final ExecutionContext ctx, final OperationDefinition def, final Object[] args,
                        final ExecutionObserver origObserver, final TimeConstraints timeConstraints) {
        ClientCallContext callContext = CallContextFactory.createSubContext(ctx);
        tracer.startCall(ctx.getRequestUUID(), callContext.getRequestUUID(), def.getOperationKey());
        final ExecutionObserver observer = new TracingEndObserver(tracer, origObserver, ctx.getRequestUUID(), callContext.getRequestUUID(), def.getOperationKey());

        if (validateCTX(ctx, observer)) {
            final IoSession session = sessionFactory.getSession();
            if (session == null) {
                LOG.error("An attempt was made to execute a method when the client was not connected!");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Operation: " + def.getOperationKey() + " with parameters: " + Arrays.toString(args));
                }
                observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError,
                        "This Client is not connected to a server so this call cannot be completed!")));
            } else {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte protocolVersion = CougarProtocol.getProtocolVersion(session);
                    final CougarObjectOutput out = objectIOFactory.newCougarObjectOutput(baos, protocolVersion);

                    final Map<String,String> additionalData = new HashMap<>();
                    contextEmitter.emit(callContext,additionalData,null);

//                    addObserver(correlationId, def.getReturnType(), observer);
                    marshaller.writeInvocationRequest(new InvocationRequest() {

                        @Override
                        public Object[] getArgs() {
                            return args;
                        }

                        @Override
                        public ExecutionContext getExecutionContext() {
                            return ctx;
                        }

                        @Override
                        public OperationKey getOperationKey() {
                            return def.getOperationKey();
                        }

                        @Override
                        public Parameter[] getParameters() {
                            return def.getParameters();
                        }

                        @Override
                        public TimeConstraints getTimeConstraints() {
                            return timeConstraints;
                        }
                    }, out, getIdentityResolver(), additionalData, protocolVersion);
                    out.close();

                    ((RequestResponseManager) session.getAttribute(RequestResponseManager.SESSION_KEY)).sendRequest(baos.toByteArray(), new RequestResponseManager.ResponseHandler() {
                        @Override
                        public void responseReceived(ResponseMessage message) {
                            CougarObjectInput in = objectIOFactory.newCougarObjectInput(new ByteArrayInputStream(message.getPayload()), CougarProtocol.getProtocolVersion(session));

                            try {
                                EnumUtils.setHardFailureForThisThread(hardFailEnumDeserialisation);
                                InvocationResponse response = marshaller.readInvocationResponse(def.getReturnType(), in);
                                // connected object calls need some additional setup prior to responding to the observer
                                if (def.getOperationKey().getType() == OperationKey.Type.ConnectedObject && response.isSuccess()) {
                                    connectedObjectManager.handleSubscriptionResponse(session, response, observer);
                                }
                                else {
                                    response.recreate(observer, def.getReturnType(), message.getPayload().length);
                                }
                            } catch (Exception e) {
                                observer.onResult(new ExecutionResult(new CougarClientException(CougarMarshallingException.unmarshallingException("binary", "Unable to deserialise response, closing session", e, true))));
                                if (session.isConnected()) {
                                    logger.log(NioLogger.LoggingLevel.SESSION, session, "Error occurred whilst trying to deserialise response, closing session");
                                    // it is possible that we never get here
                                    session.close();
                                }
                            }
                        }

                        @Override
                        public void timedOut() {
                            observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.Timeout, "Exception occurred in Client: Read timed out: "+NioUtils.getRemoteAddressUrl(session))));
                        }

                        @Override
                        public void sessionClosed() {
                            observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.RemoteCougarCommunicationFailure, "Connectivity to remote server lost!")));
                        }
                    });
                } catch (Throwable e) {
                    observer.onResult(new ExecutionResult(new CougarClientException(ServerFaultCode.FrameworkError,
                            "An exception occurred with remote method call", e)));
                }
            }
        }
    }

    public void setMarshaller(RemotableMethodInvocationMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public RemotableMethodInvocationMarshaller getMarshaller() {
        return marshaller;
    }

    @Required
    public void setContextEmitter(ContextEmitter<Map<String, String>, ?> contextEmitter) {
        this.contextEmitter = contextEmitter;
    }

    /**
     * Notify all observers that comms are lost
     */
    private synchronized void notifyConnectionLost(final IoSession session) {
        new Thread("Connection-Closed-Notifier") {
            @Override
            public void run() {
                // todo: sml: should consider rationalising these so they're all HandlerListeners..
                if (connectedObjectManager != null) {
                    connectedObjectManager.sessionTerminated(session);
                }
                for (HandlerListener listener : handlerListeners) {
                    listener.sessionClosed(session);
                }
                RequestResponseManager requestResponseManager = (RequestResponseManager) session.getAttribute(RequestResponseManager.SESSION_KEY);
                if (requestResponseManager != null) {
                    requestResponseManager.sessionClosed(session);
                }
            }
        }.start();
    }

    private boolean validateCTX(ExecutionContext ctx, ExecutionObserver observer) {
        // Ensure that the context passed is valid
        CougarValidationException ex = null;
        if (ctx == null) {
            ex = new CougarValidationException(ServerFaultCode.MandatoryNotDefined, "Execution Context must not be null");
        } else if (ctx.getLocation() == null) {
            ex = new CougarValidationException(ServerFaultCode.MandatoryNotDefined, "Geolocation details must not be null");
        }
        if (ex != null) {
            observer.onResult(new ExecutionResult(ex));
            return false;
        }
        return true;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            JMXControl control = (JMXControl) applicationContext.getBean("clientJMXControl");
            if (control != null) {
                final String[] instances = addressList.replaceAll(":", "_").split(",");
                for (String instance : instances) {
                    control.registerMBean("CoUGAR.socket.transport.client:name=socketSessionFactory,instance=" + instance, sessionFactory);
                    control.registerMBean("CoUGAR.socket.transport.client:name=socketSessionRecycler,instance=" + instance, sessionFactory.getSessionRecycler());
                }
            }
        } catch (Exception ex) {
            LOG.warn("Error while registering socket session mbeans", ex);
        }
    }

    @ManagedAttribute
    public boolean isHardFailEnumDeserialisation() {
        return hardFailEnumDeserialisation;
    }

    public void setHardFailEnumDeserialisation(boolean hardFailEnumDeserialisation) {
        this.hardFailEnumDeserialisation = hardFailEnumDeserialisation;
    }

}
