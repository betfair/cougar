/*
 * Copyright 2013, The Sporting Exchange Limited
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.client.socket.resolver.DNSBasedAddressResolver;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionTimingRecorder;
import com.betfair.cougar.core.impl.security.CommonNameCertInfoExtractor;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TlsNioConfig;
import com.betfair.cougar.netutil.nio.marshalling.SocketRMIMarshaller;
import com.betfair.cougar.transport.api.protocol.socket.SocketBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.socket.SocketOperationBindingDescriptor;
import com.betfair.cougar.transport.nio.IoSessionManager;
import com.betfair.cougar.transport.socket.SocketTransportCommandProcessor;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import org.mockito.Mockito;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.core.api.ev.Executable;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionPostProcessor;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.netutil.nio.NioConfig;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.transport.nio.ExecutionVenueNioServer;
import com.betfair.cougar.transport.nio.ExecutionVenueServerHandler;

/**
 * This class is used as a stub to facilitate NIO unit testing
 */
public class ServerClientFactory {
    private static CougarLogger logger = CougarLoggingUtils.getLogger(ServerClientFactory.class);

    public static final int COMMAND_STOP_SERVER = 1;
    public static final int COMMAND_SLEEP_60S = 2;
    public static final int COMMAND_ECHO_ARG2 = 100;
    public static final int COMMAND_FRAMEWORK_ERROR = 999;

	public static ExecutionVenueNioServer createServer(byte serverVersion, TlsNioConfig cfg) {
        CougarProtocol.setMinServerProtocolVersion(serverVersion);
        CougarProtocol.setMaxServerProtocolVersion(serverVersion);
		final ExecutionVenueNioServer server = new ExecutionVenueNioServer();
		server.setNioConfig(cfg);

		
	    SocketTransportCommandProcessor cmdProcessor = new SocketTransportCommandProcessor();
        cmdProcessor.setIdentityResolverFactory(new IdentityResolverFactory());
		
		Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                Thread t = new Thread(command);
                t.start();
            }
        };

        
        GeoIPLocator geo = Mockito.mock(GeoIPLocator.class);
        SocketRMIMarshaller marshaller = new SocketRMIMarshaller(geo, new CommonNameCertInfoExtractor());
        IdentityResolverFactory identityResolverFactory = new IdentityResolverFactory();
        identityResolverFactory.setIdentityResolver(Mockito.mock(IdentityResolver.class));



        ExecutionVenue ev = new ExecutionVenue() {
            @Override
            public void registerOperation(String namespace, OperationDefinition def, Executable executable, ExecutionTimingRecorder recorder) {
            }

            @Override
            public OperationDefinition getOperationDefinition(OperationKey key) {
                return AbstractClientTest.OPERATION_DEFINITION;
            }

            @Override
            public Set<OperationKey> getOperationKeys() {
                return null;
            }

            @Override
            public void execute(ExecutionContext ctx, OperationKey key, Object[] args, ExecutionObserver observer) {
                switch (Integer.parseInt(args[1].toString())) {
            	case COMMAND_STOP_SERVER:
                    logger.log(Level.INFO, "Stopping server");
            		server.stop();
            		break;
                case COMMAND_SLEEP_60S:
                    logger.log(Level.INFO, "Sleeping for 60s");
                    try { Thread.sleep(60000L); } catch (Exception e) {}
                case COMMAND_ECHO_ARG2:
                	observer.onResult(new ExecutionResult(args[2]));
                    break;
                case COMMAND_FRAMEWORK_ERROR:
                	observer.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.FrameworkError, AbstractClientTest.BANG)));
                    break;
            }
            	
            }

            @Override
            public void setPreProcessors(List<ExecutionPreProcessor> preProcessorList) {
            }

            @Override
            public void setPostProcessors(List<ExecutionPostProcessor> preProcessorList) {
            }
        };

        cmdProcessor.setExecutor(executor);
        cmdProcessor.setMarshaller(marshaller);
        cmdProcessor.setExecutionVenue(ev);
        ServiceBindingDescriptor desc = new SocketBindingDescriptor() {
            @Override
            public OperationBindingDescriptor[] getOperationBindings() {
                return new OperationBindingDescriptor[] { new SocketOperationBindingDescriptor(AbstractClientTest.OPERATION_DEFINITION.getOperationKey()) };
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return AbstractClientTest.OPERATION_DEFINITION.getOperationKey().getVersion();
            }

            @Override
            public String getServiceName() {
                return AbstractClientTest.OPERATION_DEFINITION.getOperationKey().getServiceName();
            }

            @Override
            public Protocol getServiceProtocol() {
                return Protocol.SOCKET;
            }
        };
        cmdProcessor.bind(desc);
        cmdProcessor.onCougarStart();


        final NioLogger nioLogger = new NioLogger("ALL");
        ExecutionVenueServerHandler handler = new ExecutionVenueServerHandler(nioLogger, cmdProcessor, new HessianObjectIOFactory());
        server.setServerHandler(handler);

        IoSessionManager sessionManager = new IoSessionManager();
        sessionManager.setNioLogger(nioLogger);
        sessionManager.setMaxTimeToWaitForRequestCompletion(5000);
        server.setSessionManager(sessionManager);
		
		return server;		
	}
	
	public static TlsNioConfig getDefaultConfig() {
        TlsNioConfig cfg = new TlsNioConfig();
        cfg.setNioLogger(new NioLogger("ALL"));
		
		cfg.setReuseAddress(true);
		cfg.setTcpNoDelay(true);
		
		return cfg;		
	}
	
	public static ExecutionVenueNioServer createServer(String host, int port, byte serverVersion) {
		return createServer(host, port, serverVersion, getDefaultConfig());
	}
	
	public static ExecutionVenueNioServer createServer(String host, int port, byte serverVersion, TlsNioConfig cfg) {
		cfg.setListenAddress(host);
		cfg.setListenPort(port);
		
		return createServer(serverVersion, cfg);
	}
	
	
	public static ExecutionVenueNioClient createClient (String connectionString, NioConfig cfg) {
        GeoIPLocator geo = Mockito.mock(GeoIPLocator.class);
        SocketRMIMarshaller marshaller = new SocketRMIMarshaller(geo, new CommonNameCertInfoExtractor());
        IdentityResolverFactory factory = new IdentityResolverFactory();
        factory.setIdentityResolver(Mockito.mock(IdentityResolver.class));

        NioLogger logger = new NioLogger("ALL");
		ExecutionVenueNioClient client = new ExecutionVenueNioClient(logger,  cfg, new HessianObjectIOFactory(), new ClientConnectedObjectManager(), null, connectionString,
                new JMXReportingThreadPoolExecutor(30, 60, 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()), new JMXReportingThreadPoolExecutor(30, 60, 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()),
                new DNSBasedAddressResolver());
        client.setMarshaller(marshaller);
        
        return client;		
	}
		
	
	public static ExecutionVenueNioClient createClient (String connectionString) {        
        return createClient(connectionString, getDefaultConfig());
		
	}
	
}
