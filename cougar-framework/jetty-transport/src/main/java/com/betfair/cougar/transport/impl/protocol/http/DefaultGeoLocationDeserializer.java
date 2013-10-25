package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.util.HeaderUtils;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 */
public class DefaultGeoLocationDeserializer implements GeoLocationDeserializer {

    @Override
    public List<String> deserialize(HttpServletRequest request, String remoteAddress) {
        final String xIPs = HeaderUtils.cleanHeaderValue(request.getHeader("X-Forwarded-For"));
        List<String> resolvedAddresses = RemoteAddressUtils.parse(null, xIPs);
        if (resolvedAddresses.isEmpty() && remoteAddress != null) {
            resolvedAddresses = Collections.singletonList(remoteAddress);
        }
        return resolvedAddresses;
    }
}
