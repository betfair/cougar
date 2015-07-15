/*
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

package com.betfair.cougar.test.socket.tester.server;

import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.NullExecutionTimingRecorder;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.core.api.transports.TransportRegistry;
import com.betfair.cougar.core.impl.logging.EventLoggerImpl;
import com.betfair.cougar.core.impl.security.CommonNameCertInfoExtractor;
import com.betfair.cougar.core.impl.tracing.CompoundTracer;
import com.betfair.cougar.core.impl.transports.TransportRegistryImpl;
import com.betfair.cougar.logging.EventLoggingRegistry;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.netutil.nio.marshalling.DefaultExecutionContextResolverFactory;
import com.betfair.cougar.netutil.nio.marshalling.DefaultSocketTimeResolver;
import com.betfair.cougar.netutil.nio.marshalling.SocketRMIMarshaller;
import com.betfair.cougar.test.socket.tester.common.ClientAuthRequirement;
import com.betfair.cougar.test.socket.tester.common.Common;
import com.betfair.cougar.test.socket.tester.common.SslRequirement;
import com.betfair.cougar.transport.api.protocol.CougarObjectIOFactory;
import com.betfair.cougar.transport.impl.DehydratedExecutionContextResolutionImpl;
import com.betfair.cougar.transport.nio.ExecutionVenueNioServer;
import com.betfair.cougar.transport.nio.ExecutionVenueServerHandler;
import com.betfair.cougar.transport.nio.IoSessionManager;
import com.betfair.cougar.transport.socket.PooledServerConnectedObjectManager;
import com.betfair.cougar.transport.socket.SocketTransportCommandProcessor;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.NullGeoIPLocator;
import org.springframework.core.io.ClassPathResource;

import javax.management.MBeanServerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
*
*/
class ServerInstance {

    private final String name;
    private final ExecutionVenueNioServer server;
    private int port;

    ServerInstance(String name) throws IOException {
        this(name, SslRequirement.None);
    }

    ServerInstance(String name, SslRequirement sslRequirement) throws IOException {
        this(name, sslRequirement, ClientAuthRequirement.None);
    }

    public ServerInstance(String name, SslRequirement sslRequirement, ClientAuthRequirement clientAuthRequirement) throws IOException {
        this.name = name;
        port = findPort();

        server = new ExecutionVenueNioServer();
        TlsNioConfig nioConfig = new TlsNioConfig();
        NioLogger sessionLogger = new NioLogger("ALL");
        nioConfig.setListenAddress("127.0.0.1");
        nioConfig.setListenPort(port);
        nioConfig.setNioLogger(sessionLogger);
        nioConfig.setMaxWriteQueueSize(0);
        nioConfig.setRecvBufferSize(524288);
        nioConfig.setSendBufferSize(524288);
        nioConfig.setKeepAlive(true);
        nioConfig.setKeepAliveInterval(1000);
        nioConfig.setKeepAliveTimeout(5000);
        nioConfig.setReuseAddress(true);
        nioConfig.setTcpNoDelay(true);
        nioConfig.setWorkerTimeout(0);
        nioConfig.setUseDirectBuffersInMina(false);
        nioConfig.setMbeanServer(MBeanServerFactory.createMBeanServer());
        if (sslRequirement != SslRequirement.None) {
            nioConfig.setSupportsTls(sslRequirement == SslRequirement.Supports || sslRequirement == SslRequirement.Requires);
            nioConfig.setRequiresTls(sslRequirement == SslRequirement.Requires);
            nioConfig.setKeystore(new ClassPathResource("cougar_server_cert.jks"));
            nioConfig.setKeystoreType("JKS");
            nioConfig.setKeystorePassword("password");
            if (clientAuthRequirement != ClientAuthRequirement.None) {
                nioConfig.setTruststore(new ClassPathResource("cougar_client_ca.jks"));
                nioConfig.setTruststoreType("JKS");
                nioConfig.setTruststorePassword("password");
                nioConfig.setWantClientAuth(true);
                if (clientAuthRequirement == ClientAuthRequirement.Needs) {
                    nioConfig.setNeedClientAuth(true);
                }
            }
        }
        ExecutorService serverExecutor = new JMXReportingThreadPoolExecutor(2,3,5000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
        IdentityResolverFactory identityResolverFactory = new IdentityResolverFactory();
        DehydratedExecutionContextResolutionImpl contextResolution = new DehydratedExecutionContextResolutionImpl();
        contextResolution.registerFactory(new DefaultExecutionContextResolverFactory(new NullGeoIPLocator(), new DefaultSocketTimeResolver(true))); // todo: set false if run on multiple machines
        contextResolution.onCougarStart();
        SocketRMIMarshaller marshaller = new SocketRMIMarshaller(new CommonNameCertInfoExtractor(), contextResolution);
        EventLoggingRegistry registry = new EventLoggingRegistry();
        TestServerExecutionVenue executionVenue = new TestServerExecutionVenue();
        Executor executor = new JMXReportingThreadPoolExecutor(2,3,5000,TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());

        CougarObjectIOFactory objectIoFactory = new HessianObjectIOFactory(false);
        IoSessionManager sessionManager = new IoSessionManager();
        TransportRegistry transportRegistry = new TransportRegistryImpl();

        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());


        PooledServerConnectedObjectManager connectedObjectManager = new PooledServerConnectedObjectManager();
        connectedObjectManager.setNioLogger(sessionLogger);
        connectedObjectManager.setNumProcessingThreads(1);
        connectedObjectManager.setObjectIOFactory(objectIoFactory);
        connectedObjectManager.setMaxUpdateActionsPerMessage(100);
        EventLoggerImpl eventLogger = new EventLoggerImpl();
        eventLogger.setEnabled(false);
        connectedObjectManager.setEventLogger(eventLogger);
        connectedObjectManager.start();

        SocketTransportCommandProcessor processor = new SocketTransportCommandProcessor();


        processor.setConnectedObjectManager(connectedObjectManager);
        processor.setIdentityResolverFactory(identityResolverFactory);
        processor.setMarshaller(marshaller);
        processor.setNioLogger(sessionLogger);
        processor.setRegistry(registry);
        processor.setUnknownCipherKeyLength(0);
        processor.setExecutionVenue(executionVenue);

        processor.setExecutor(executor);
        processor.setTracer(new CompoundTracer());


        ExecutionVenueServerHandler serverHandler = new ExecutionVenueServerHandler(sessionLogger, processor, objectIoFactory);
        serverHandler.addListener(connectedObjectManager);

        server.setNioConfig(nioConfig);
        server.setServerExecutor(serverExecutor);
        server.setServerHandler(serverHandler);
        server.setSessionManager(sessionManager);
        server.setSocketAcceptorProcessors(1);
        server.setTransportRegistry(transportRegistry);

        EchoOperation op = new EchoOperation(clientAuthRequirement == ClientAuthRequirement.Needs);
        registerOp(Common.echoOperationDefinition, op, executionVenue, processor);
        registerOp(Common.echoFailureOperationDefinition, op, executionVenue, processor);

        HeapOperation op2 = new HeapOperation(clientAuthRequirement == ClientAuthRequirement.Needs);
        registerOp(Common.heapSubscribeOperationDefinition, op2, executionVenue, processor);
        registerOp(Common.heapSetOperationDefinition, op2, executionVenue, processor);
        registerOp(Common.heapCloseOperationDefinition, op2, executionVenue, processor);

        server.start();

        server.setHealthState(true);

    }

    private void registerOp(OperationDefinition def, Executable exec, ExecutionVenue ev, SocketTransportCommandProcessor processor) {
        ev.registerOperation(null, def, exec, new NullExecutionTimingRecorder(), 0L);
        processor.bindOperation(def);
    }

    private int findPort() {
        int port = 0;
        while (true) {
            try {
                ServerSocket socket = new ServerSocket(0);
                port = socket.getLocalPort();
                socket.close();
                return port;
            } catch (IOException ioe) {
                System.err.println("Couldn't bind to port "+port+" trying again");
            }
        }
    }

    public String getName() {
        return name;
    }

    public void shutdown() {

        server.stop();
    }

    public int getPort() {
        return port;
    }
}
