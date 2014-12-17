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

package com.betfair.cougar.client.factory;

import com.betfair.cougar.api.Service;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.client.HttpClientExecutable;
import com.betfair.cougar.client.SyncHttpTransportFactory;
import com.betfair.cougar.client.api.ContextEmitter;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.RegisterableClientExecutableResolver;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.client.ExceptionFactory;
import com.betfair.cougar.core.impl.CougarIntroductionService;
import com.betfair.cougar.core.impl.ev.ClientServiceRegistration;
import com.betfair.cougar.transport.api.protocol.http.HttpServiceBindingDescriptor;
import org.springframework.core.io.Resource;

import java.util.concurrent.Executor;

/**
 * Base cougar client service factory that registers client services
 * with the specified parameters.  Currently works with only synchronous
 * http transport.
 * - Creates a new instance of the transport
 * - Initialises the executable resolver with the transport and the operation keys
 * - Packages the whole thing into a service registration request and introduces the service to EV
 */
public abstract class AbstractCougarClientFactory {

    private SyncHttpTransportFactory syncHttpTransportFactory;
    private CougarIntroductionService cougarIntroductionService;
    protected ExecutionVenue executionVenue;
    protected Executor executor;

    protected void registerClient(Service service, String endpointUrl, String namespace,
                                  HttpServiceBindingDescriptor serviceBindingDescriptor, ExceptionFactory exceptionFactory,
                                  ServiceDefinition serviceDefinition, RegisterableClientExecutableResolver executableResolver,
                                  IdentityResolver identityResolver, IdentityTokenResolver identityTokenResolver) {
        registerClient(service, endpointUrl, namespace, serviceBindingDescriptor, exceptionFactory, serviceDefinition,
                executableResolver, identityResolver, identityTokenResolver, false, null, null, null, null, false);
    }

    protected void registerClient(Service service, String endpointUrl, String namespace,
                                  HttpServiceBindingDescriptor serviceBindingDescriptor, ExceptionFactory exceptionFactory,
                                  ServiceDefinition serviceDefinition, RegisterableClientExecutableResolver executableResolver,
                                  IdentityResolver identityResolver, IdentityTokenResolver identityTokenResolver,
                                  boolean sslEnabled, Resource keyStore, String keyPassword, Resource trustStore,
                                  String trustPassword, boolean hostnameVerificationDisabled) {

        try {
            // Initialise transport
            HttpClientExecutable transport = syncHttpTransportFactory.getHttpTransport(endpointUrl, serviceBindingDescriptor,
                    exceptionFactory, sslEnabled, keyStore, keyPassword,
                    trustStore, trustPassword, hostnameVerificationDisabled);
            transport.setIdentityTokenResolver(identityTokenResolver);
            transport.setIdentityResolver(identityResolver);
            transport.init();

            // Initialise executable resolver
            executableResolver.setDefaultOperationTransport(transport);

            // TODO : Wire in the eventTransport
            // executableResolver.setEventTransport(eventTransport);
            executableResolver.init();

            // Package up the registration request
            ClientServiceRegistration clientServiceRegistration = new ClientServiceRegistration();
            clientServiceRegistration.setResolver(executableResolver);
            clientServiceRegistration.setServiceDefinition(serviceDefinition);
            clientServiceRegistration.setNamespace(namespace);

            // register with ev
            cougarIntroductionService.registerService(clientServiceRegistration);
        } catch (Exception ex) {
            throw new CougarFrameworkException("Error while registering client with ev", ex);
        }
    }

    public void setCougarIntroductionService(CougarIntroductionService cougarIntroductionService) {
        this.cougarIntroductionService = cougarIntroductionService;
    }

    public void setSyncHttpTransportFactory(SyncHttpTransportFactory syncHttpTransportFactory) {
        this.syncHttpTransportFactory = syncHttpTransportFactory;
    }

    public void setExecutionVenue(ExecutionVenue executionVenue) {
        this.executionVenue = executionVenue;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
