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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.*;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.api.transports.AbstractRegisterableTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.RequestLogger;
import com.betfair.cougar.transport.api.TransportCommandProcessorFactory;
import com.betfair.cougar.transport.api.protocol.ProtocolBinding;
import com.betfair.cougar.transport.api.protocol.ProtocolBindingRegistry;
import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.transport.api.protocol.http.HttpCommandProcessor;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.jsonrpc.JsonRpcOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.jetty.jmx.JettyEndpoints;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import com.betfair.cougar.util.jmx.JMXControl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.servlet.ServletException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@ManagedResource
public class JettyHttpTransport extends AbstractRegisterableTransport implements GateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyHttpTransport.class);

    private JMXControl jmxControl;

    private JettyServerWrapper server = new JettyServerWrapper();

    private StaticContentServiceHandler wsdlStaticHandler;
    private StaticContentServiceHandler htmlStaticHandler;
    private ContextHandlerCollection handlerCollection = new ContextHandlerCollection();

    private TransportCommandProcessorFactory<HttpCommandProcessor> commandProcessorFactory;
    private HttpCommandProcessor defaultCommandProcessor;
    private ProtocolBindingRegistry protocolBindingRegistry;

    private Map<String, JettyHandlerSpecification> handlerSpecificationMap = new HashMap<String, JettyHandlerSpecification>();

    private Map<String, String> pathAliases = new HashMap<>();

    // created in setApplicationContext
    // set to empty initially so tests that don't set up channels and service binding descriptors will work OK
    private Set<HttpServiceBindingDescriptor> serviceBindingDescriptors = new HashSet<HttpServiceBindingDescriptor>();

    // The set of JSON-RPC context roots already bound.
    private Set<String> rpcProtocolBindings = new HashSet<String>();

    // populated by setter methods

    private String wsdlContextPath;
    private String wsdlRegex;
    private String wsdlMediaType;

    private String htmlContextPath;
    private String htmlRegex;
    private String htmlMediaType;

    private GeoLocationDeserializer deserializer;
    private String uuidHeader;
    private String uuidParentsHeader;

    private int timeoutInSeconds;

    private GeoIPLocator geoIPLocator;

    private RequestLogger requestLogger;

    private JettyEndpoints jettyEndPoints;

    private boolean gzipEnabled;
    private int gzipMinSize;
    private int gzipBufferSize;
    private String gzipExcludedAgents;

    // CORS
    /**
     * Is CORS Handler enabled?
     */
    private boolean corsEnabled;
    /**
     * Comma separated list of allowed cors origins
     * @link <a href="http://www.w3.org/TR/cors/#access-control-allow-origin-response-header">CORS Allow Origins</a>
     */
    private String corsAllowedOrigins;
    /**
     * Comma separated list of allowed cors methods
     * @link <a href="http://www.w3.org/TR/cors/#access-control-allow-methods-response-header">CORS Allow Methods</a>
     */
    private String corsAllowedMethods;
    /**
     * Comma separated list of allowed cors headers
     * @see <a href="http://www.w3.org/TR/cors/#access-control-allow-headers-response-header">CORS Allow Headers</a>
     */
    private String corsAllowedHeaders;
    /**
     * String representation of the pre-flight request ttl
     * @link <a href="http://www.w3.org/TR/cors/#access-control-max-age-response-header">CORS Max Age</a>
     */
    private String corsPreflightMaxAge;
    /**
     * String representation of a boolean indicating if the request can include user credentials
     * @link <a href="http://www.w3.org/TR/cors/#access-control-allow-credentials-response-header">CORS Allow Credentials</a>
     */
    private String corsAllowCredentials;
    /**
     * Comma separated list of allowed cors exposed headers
     * @link <a href="http://www.w3.org/TR/cors/#http-access-control-expose-headers">CORS Expose Headers</a>
     */
    private String corsExposedHeaders;
    // End CORS

    private int unknownCipherKeyLength;
    private boolean suppressCommasInAccessLogForStaticHtml;
    private boolean suppressCommasInAccessLogForCalls;
    private String corsMaxAge;

    public JettyHttpTransport() {
    }

    @Override
    public String getName() {
        return "JettyHttpTransport";
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void onCougarStart() {
        createJettyHandlers();

        logOperationEndpoints(serviceBindingDescriptors);
        try {
            server.start();
        } catch (Exception ex) {
            LOGGER.error("Failed to startup jetty", ex);
            throw new PanicInTheCougar(ex);
        }
    }

    /**
     * By setting the starting gate property this IntroductionService will register
     * itself with the CougarStartingGate
     *
     * @param startingGate the starting gate for the application
     */
    public void setStartingGate(CougarStartingGate startingGate) {
        startingGate.registerStartingListener(this);
    }

    @Override
    public void notify(BindingDescriptor bindingDescriptor) {
        if (bindingDescriptor.getServiceProtocol().underlyingTransportIsHttp()) {
            serviceBindingDescriptors.add((HttpServiceBindingDescriptor) bindingDescriptor);
            registerHandler((HttpServiceBindingDescriptor) bindingDescriptor);
        }
    }

    public void start() throws Exception {
        register();
        initialiseStaticJettyConfig();
        server.startThreadPool();

        // Create a shutdown hook to close the jetty port cleanly
        Runtime.getRuntime().addShutdownHook(new Thread("Jetty Shutdown Hook") {

            @Override
            public void run() {
                LOGGER.info("Gracefully shutting down Jetty Server");
                try {
                    JettyHttpTransport.this.stop();
                } catch (Exception e) {
                    LOGGER.warn("Failed to shutdown jetty", e);
                }
            }
        });
    }

    public void stop() throws Exception {
        unregister();
        server.stop();
    }

    public void initialiseStaticJettyConfig() throws Exception {
        server.initialiseConnectors();

        ErrorHandler errorHandler = new CougarErrorHandler();
        wsdlStaticHandler = new StaticContentServiceHandler(
                wsdlContextPath,
                wsdlRegex,
                wsdlMediaType,
                uuidHeader,
                uuidParentsHeader,
                deserializer,
                geoIPLocator,
                requestLogger,
                true);
        wsdlStaticHandler.setUnknownCipherKeyLength(unknownCipherKeyLength);

        htmlStaticHandler = new StaticContentServiceHandler(
                htmlContextPath,
                htmlRegex,
                htmlMediaType,
                uuidHeader,
                uuidParentsHeader,
                deserializer,
                geoIPLocator,
                requestLogger,
                suppressCommasInAccessLogForStaticHtml);
        htmlStaticHandler.setUnknownCipherKeyLength(unknownCipherKeyLength);

        StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setServer(server.getJettyServer());

        handlerCollection.setServer(server.getJettyServer());

        JettyHandler defaultJettyServiceHandler = new AliasHandler(defaultCommandProcessor, suppressCommasInAccessLogForCalls, pathAliases);
        ContextHandler context = new ContextHandler();
        context.setContextPath("");
        context.setResourceBase(".");
        context.setHandler(defaultJettyServiceHandler);
        handlerCollection.addHandler(context);

        handlerCollection.addHandler(wsdlStaticHandler);
        handlerCollection.addHandler(htmlStaticHandler);
//        handlerCollection.addHandler(aliasHandler);
        statisticsHandler.setHandler(handlerCollection);

        // Register the errorhandler with the server itself
        server.addBean(errorHandler);
        server.setHandler(statisticsHandler);
    }


    /**
     * This method adds the service binding descriptor, and all the appropriate protocol binding combos
     * into the handlerSpecMap, before binding the serviceBindingDescriptor to the appropriate
     * command processor
     */
    public void registerHandler(HttpServiceBindingDescriptor serviceBindingDescriptor) {
        for (ProtocolBinding protocolBinding : protocolBindingRegistry.getProtocolBindings()) {
            if (protocolBinding.getProtocol() == serviceBindingDescriptor.getServiceProtocol()) {
                String contextPath = protocolBinding.getContextRoot() + serviceBindingDescriptor.getServiceContextPath();

                JettyHandlerSpecification handlerSpec = handlerSpecificationMap.get(contextPath);
                if (handlerSpec == null) {
                    handlerSpec = new JettyHandlerSpecification(protocolBinding.getContextRoot(),
                            protocolBinding.getProtocol(), serviceBindingDescriptor.getServiceContextPath());
                    handlerSpecificationMap.put(contextPath, handlerSpec);
                }
                if (protocolBinding.getIdentityTokenResolver() != null) {
                    handlerSpec.addServiceVersionToTokenResolverEntry(serviceBindingDescriptor.getServiceVersion(), protocolBinding.getIdentityTokenResolver());
                }
            }
        }
        commandProcessorFactory.getCommandProcessor(serviceBindingDescriptor.getServiceProtocol()).bind(serviceBindingDescriptor);
    }

    private void createJettyHandlers() {
        for (Map.Entry<String, JettyHandlerSpecification> spec : handlerSpecificationMap.entrySet()) {
            HttpCommandProcessor commandProcessor = commandProcessorFactory.getCommandProcessor(spec.getValue().getProtocol());
            bindServiceContextRoot(spec.getValue(), commandProcessor);
        }
    }

    private void bindServiceContextRoot(JettyHandlerSpecification spec, HttpCommandProcessor commandProcessor) {
        Protocol serviceProtocol = spec.getProtocol();
        String jettyContextRoot = spec.getJettyContextRoot();
        if (jettyContextRoot.endsWith("/")) {
            jettyContextRoot = jettyContextRoot.substring(0, jettyContextRoot.length()-1);
        }

        if (serviceProtocol == Protocol.JSON_RPC && jsonRpcContextAlreadyBound(jettyContextRoot)) {
            LOGGER.info("Not binding Jetty Handler for protocol: JSON-RPC on context root [" +
                    jettyContextRoot + "] - context already bound");
        } else {
            StringBuilder sb = new StringBuilder("Adding a new Jetty Handler on url [").append(jettyContextRoot).append("] ");
            if (serviceProtocol == Protocol.JSON_RPC) {
                sb.append("For all service/version combinations ");
            } else {
                sb.append("for versions [");
                for (ServiceVersion version : spec.getVersionToIdentityTokenResolverMap().keySet()) {
                    sb.append(version.toString() + " ");
                }
                sb.append("] ");
            }
            sb.append("on protocol " + serviceProtocol);
            LOGGER.info(sb.toString());

            JettyHandler.IdentityTokenResolverLookup identityTokenResolverLookup = null;

            if (!spec.getVersionToIdentityTokenResolverMap().isEmpty()) {
                if (spec.getVersionToIdentityTokenResolverMap().size() == 1) {
                    identityTokenResolverLookup =
                            new JettyHandler.SingletonIdentityTokenResolverLookup(
                                    spec.getVersionToIdentityTokenResolverMap().values().iterator().next());
                } else {
                    identityTokenResolverLookup = new JettyHandler.GeneralHttpIdentityTokenResolverLookup(jettyContextRoot, spec);
                }
            }
            JettyHandler jettyServiceHandler = new JettyHandler(commandProcessor, spec, identityTokenResolverLookup, suppressCommasInAccessLogForCalls);
            jettyServiceHandler.setTimeoutInSeconds(timeoutInSeconds);
            // Jetty stuff
            ContextHandler context = new ContextHandler();
            context.setServer(server.getJettyServer());
            context.setContextPath(jettyContextRoot);

            // CORS stuff
            if (corsEnabled) {
                final ContextHandler corsContext = new ContextHandler();
                corsContext.setServer(server.getJettyServer());
                corsContext.setContextPath(jettyContextRoot);
                try {
                    corsContext.setHandler(new CrossOriginHandler(corsAllowedOrigins, corsAllowedMethods, corsAllowedHeaders, corsPreflightMaxAge, corsAllowCredentials, corsExposedHeaders));
                    handlerCollection.addHandler(corsContext);
                    if (serviceProtocol == Protocol.SOAP || serviceProtocol == Protocol.JSON_RPC) {
                        corsContext.setAllowNullPathInfo(true);
                    }
                    else {
                        corsContext.setAllowNullPathInfo(false);
                    }
                    corsContext.setResourceBase(".");
                }
                catch (ServletException e) {
                    throw new CougarFrameworkException("Failed to create CORS handler: [" + jettyContextRoot + "]", e);
                }
            }
            //-- End CORS stuff

            // Rescript is not allowed null paths as the operation must be appended to the end of the URI
            if (serviceProtocol == Protocol.SOAP || serviceProtocol == Protocol.JSON_RPC) {
                context.setAllowNullPathInfo(true);
            } else {
                context.setAllowNullPathInfo(false);
            }
            context.setResourceBase(".");

            if (gzipEnabled) {
                try {
                    context.setHandler(new GzipHandler(gzipBufferSize, gzipMinSize, gzipExcludedAgents, jettyServiceHandler));
                }
                catch (ServletException e) {
                    throw new CougarFrameworkException("Failed to create GZIP handler: [" + jettyContextRoot + "]", e);
                }
            }
            else {
                context.setHandler(jettyServiceHandler);
            }
            handlerCollection.addHandler(context);
            try {
                //If the server is already running, start this context up!
                //If it isn't running, then it will be started when Jetty starts
                if (server.isRunning()) {
                    context.start();
                }
            } catch (Exception ex) {
                throw new CougarFrameworkException("Failed to register serviceBindingDescriptor: [" + jettyContextRoot + "]", ex);
            }
        }
    }

    private boolean jsonRpcContextAlreadyBound(String contextRoot) {
        if (rpcProtocolBindings.contains(contextRoot)) {
            return true;
        }
        rpcProtocolBindings.add(contextRoot);
        return false;
    }

    private void logOperationEndpoints(
            Collection<HttpServiceBindingDescriptor> services
    ) {
        List<String> entries = new ArrayList<String>();
        for (ProtocolBinding protocolBinding : protocolBindingRegistry.getProtocolBindings()) {
            for (HttpServiceBindingDescriptor service : services) {
                if (service.getServiceProtocol() == protocolBinding.getProtocol()) {
                    OperationBindingDescriptor[] operations = service.getOperationBindings();
                    for (OperationBindingDescriptor operation : operations) {
                        StringBuffer entry = new StringBuffer();
                        entry.append(String.format("%9s : ", service.getServiceProtocol()));
                        entry.append(String.valueOf(operation.getOperationKey()));
                        entry.append(" => ");
                        entry.append(endpointFor(protocolBinding.getContextRoot(), service, operation));
                        entries.add(entry.toString());
                    }
                }
            }
        }
        Collections.sort(entries);
        jettyEndPoints.setEndPoints(entries);
    }

    private String endpointFor(String contextRoot, HttpServiceBindingDescriptor serviceDescriptor,
                               OperationBindingDescriptor operationDescriptor
    ) {
        StringBuffer buf = new StringBuffer();
        buf.append(getURISchemeAndAuthority());
        buf.append(contextRoot);
        buf.append(serviceDescriptor.getServiceContextPath());
        if (serviceDescriptor.getServiceProtocol() != Protocol.JSON_RPC) {
            buf.append("v").append(serviceDescriptor.getServiceVersion().getMajor());
        }
        if (operationDescriptor instanceof RescriptOperationBindingDescriptor) {
            String path = ((RescriptOperationBindingDescriptor) operationDescriptor).getURI();
            buf.append(path);
        } else if (operationDescriptor instanceof JsonRpcOperationBindingDescriptor){
            buf.append("/");
        }
        return buf.toString();
    }

    private String getURISchemeAndAuthority() {
        try {
            boolean http = server.isHttpEnabled();
            return String.format(
                    "%s://%s:%s",
                    http ? "http" : "https",
                    InetAddress.getLocalHost().getHostAddress(),
                    http ? server.getHttpPort() : server.getHttpsPort()
            );
        } catch (UnknownHostException e) {
            throw new RuntimeException("There was a problem determining this host's address", e);
        }
    }

    public void setServerWrapper(JettyServerWrapper server) {
        this.server = server;
    }

    public JettyServerWrapper getServerWrapper() {
        return server;
    }

    public void setDefaultCommandProcessor(HttpCommandProcessor defaultCommandProcessor) {
        this.defaultCommandProcessor = defaultCommandProcessor;
    }

    public void setProtocolBindingRegistry(ProtocolBindingRegistry protocolBindingRegistry) {
        this.protocolBindingRegistry = protocolBindingRegistry;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public Server getJettyServer() {
        return server.getJettyServer();
    }

    public StaticContentServiceHandler getWsdlStaticHandler() {
        return wsdlStaticHandler;
    }

    public StaticContentServiceHandler getHtmlStaticHandler() {
        return htmlStaticHandler;
    }

    public Map<String, JettyHandlerSpecification> getHandlerSpecificationMap() {
        return handlerSpecificationMap;
    }

    public void setCommandProcessorFactory(TransportCommandProcessorFactory<HttpCommandProcessor> commandProcessorFactory) {
        this.commandProcessorFactory = commandProcessorFactory;
    }


    public void setWsdlContextPath(String wsdlContextPath) {
        this.wsdlContextPath = wsdlContextPath;
    }

    public void setWsdlRegex(String wsdlRegex) {
        this.wsdlRegex = wsdlRegex;
    }

    public void setWsdlMediaType(String wsdlMediaType) {
        this.wsdlMediaType = wsdlMediaType;
    }

    public void setHtmlContextPath(String htmlContextPath) {
        this.htmlContextPath = htmlContextPath;
    }

    public void setHtmlRegex(String htmlRegex) {
        this.htmlRegex = htmlRegex;
    }

    public void setHtmlMediaType(String htmlMediaType) {
        this.htmlMediaType = htmlMediaType;
    }

    public void setGeoLocationDeserializer(GeoLocationDeserializer deserializer) {
        this.deserializer = deserializer;
    }

    public void setRequestLogger(RequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }

    public void setUuidHeader(String uuidHeader) {
        this.uuidHeader = uuidHeader;
    }

    public void setUuidParentsHeader(String uuidParentsHeader) {
        this.uuidParentsHeader = uuidParentsHeader;
    }

    public void setGeoIPLocator(GeoIPLocator geoIPLocator) {
        this.geoIPLocator = geoIPLocator;
    }

    public void setJettyEndPoints(JettyEndpoints jettyEndPoints) {
        this.jettyEndPoints = jettyEndPoints;
    }

    public void setJmxControl(JMXControl jmxControl) {
        this.jmxControl = jmxControl;
    }

    public void setPathAliases(Map<String, String> pathAliases) {
        this.pathAliases = pathAliases;
    }

    @ManagedAttribute
    public Map<String, String> getPathAliases() {
        return pathAliases;
    }

    @ManagedAttribute
    public boolean isHttpEnabled() {
        return server.isHttpEnabled();
    }

    @ManagedAttribute
    public boolean isHttpsEnabled() {
        return server.isHttpsEnabled();
    }

    @ManagedAttribute
    public int getRequestHeaderSize() {
        return server.getRequestHeaderSize();
    }

    @ManagedAttribute
    public int getResponseBufferSize() {
        return server.getResponseBufferSize();
    }

    @ManagedAttribute
    public int getResponseHeaderSize() {
        return server.getResponseHeaderSize();
    }

    @ManagedAttribute
    public int getHttpAcceptors() {
        return server.getHttpAcceptors();
    }

    @ManagedAttribute
    public int getHttpSelectors() {
        return server.getHttpSelectors();
    }

    @ManagedAttribute
    public int getHttpsAcceptors() {
        return server.getHttpsAcceptors();
    }

    @ManagedAttribute
    public int getHttpsSelectors() {
        return server.getHttpsSelectors();
    }

    @ManagedAttribute
    public boolean isHttpForwarded() {
        return server.isHttpForwarded();
    }

    @ManagedAttribute
    public boolean isHttpsForwarded() {
        return server.isHttpsForwarded();
    }

    @ManagedAttribute
    public boolean isGzipEnabled() {
    	return gzipEnabled;
    }

    public void setGzipEnabled(boolean gzipEnabled) {
    	this.gzipEnabled = gzipEnabled;
    }

    @ManagedAttribute
    public String getExcludedAgents() {
    	return gzipExcludedAgents;
    }

    public void setGzipExcludedAgents(String excludedAgents) {
    	if (excludedAgents != null && !excludedAgents.isEmpty()) {
    		this.gzipExcludedAgents = excludedAgents;
    	}
    }

    @ManagedAttribute
    public int getGzipBufferSize() {
    	return gzipBufferSize;
    }

    public void setGzipBufferSize(int bufferSize) {
    	this.gzipBufferSize = bufferSize;
    }

    @ManagedAttribute
    public int getGzipMinSize() {
    	return gzipMinSize;
    }

    public void setGzipMinSize(int minSize) {
    	this.gzipMinSize = minSize;
    }

    @ManagedAttribute
    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    @ManagedAttribute
    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    @ManagedAttribute
    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }

    @ManagedAttribute
    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    @ManagedAttribute
    public String getCorsMaxAge() {
        return corsMaxAge;
    }

    public void setCorsMaxAge(String corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }

    @ManagedAttribute
    public String getCorsPreflightMaxAge() {
        return corsPreflightMaxAge;
    }

    public void setCorsPreflightMaxAge(String corsPreflightMaxAge) {
        this.corsPreflightMaxAge = corsPreflightMaxAge;
    }

    @ManagedAttribute
    public String getCorsAllowCredentials() {
        return corsAllowCredentials;
    }

    public void setCorsAllowCredentials(String corsAllowCredentials) {
        this.corsAllowCredentials = corsAllowCredentials;
    }

    @ManagedAttribute
    public String getCorsExposedHeaders() {
        return corsExposedHeaders;
    }

    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }

    public int getMaxFormContentSize() {
        return getServerWrapper().getMaxFormContentSize();
    }

    @ManagedAttribute
    public int getHttpAcceptQueueSize() {
        return getServerWrapper().getHttpAcceptQueueSize();
    }

    @ManagedAttribute
    public int getHttpsAcceptQueueSize() {
        return getServerWrapper().getHttpsAcceptQueueSize();
    }

    @ManagedAttribute
    public long getLowResourcesMaxMemory() {
        return getServerWrapper().getLowResourcesMaxMemory();
    }

    @ManagedAttribute
    public int getLowResourcesMaxConnections() {
        return getServerWrapper().getLowResourcesMaxConnections();
    }

    @ManagedAttribute
    public int getLowResourcesPeriod() {
        return getServerWrapper().getLowResourcesPeriod();
    }

    @ManagedAttribute
    public int getLowResourcesMaxTime() {
        return getServerWrapper().getLowResourcesMaxTime();
    }

    @ManagedAttribute
    public int getLowResourcesIdleTime() {
        return getServerWrapper().getLowResourcesIdleTime();
    }

    @ManagedAttribute
    public boolean isLowResourcesMonitorThreads() {
        return getServerWrapper().isLowResourcesMonitorThreads();
    }

    @ManagedAttribute
    public boolean isSuppressCommasInAccessLogForStaticHtml() {
        return suppressCommasInAccessLogForStaticHtml;
    }

    public void setSuppressCommasInAccessLogForStaticHtml(boolean suppressCommasInAccessLogForStaticHtml) {
        this.suppressCommasInAccessLogForStaticHtml = suppressCommasInAccessLogForStaticHtml;
    }

    @ManagedAttribute
    public boolean isSuppressCommasInAccessLogForCalls() {
        return suppressCommasInAccessLogForCalls;
    }

    public void setSuppressCommasInAccessLogForCalls(boolean suppressCommasInAccessLogForCalls) {
        this.suppressCommasInAccessLogForCalls = suppressCommasInAccessLogForCalls;
    }

    public ContextHandlerCollection getHandlerCollection() {
        return handlerCollection;
    }
}
