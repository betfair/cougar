package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.core.api.ev.ServiceLogManagerFactory;
import com.betfair.cougar.logging.EventLoggingRegistry;

/**
 *
 */
public class DefaultServiceLogManagerFactory implements ServiceLogManagerFactory {

    private final EventLoggingRegistry eventLoggingRegistry;

    public DefaultServiceLogManagerFactory(EventLoggingRegistry eventLoggingRegistry) {
        this.eventLoggingRegistry = eventLoggingRegistry;
    }

    @Override
    public ServiceLogManager create(String namespace, String serviceName, ServiceVersion version) {
        String loggerName = eventLoggingRegistry.registerConcreteLogger(namespace, serviceName);
        return new DefaultServiceLogManager(loggerName);
    }
}
