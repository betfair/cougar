package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.ServiceVersion;

/**
 *
 */
public interface ServiceLogManagerFactory {

    ServiceLogManager create(String namespace, String serviceName, ServiceVersion version);
}
