package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ServiceLogManager;
import com.betfair.cougar.core.api.ev.ServiceLogManagerFactory;

/**
 * This class provides a no frills service log manager that will supply
 * a default logger based on the supplied logNamePrefix
 */
public class NamedLoggerServiceLogManagerFactory implements ServiceLogManagerFactory {

    private final String logNamePrefix;

    public NamedLoggerServiceLogManagerFactory(String logNamePrefix) {
        this.logNamePrefix = logNamePrefix;
    }

    @Override
    public ServiceLogManager create(String namespace, String serviceName, ServiceVersion version) {
        String logName = logNamePrefix + serviceName;
        return new DefaultServiceLogManager(logName);
    }
}
