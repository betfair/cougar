package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.transport.api.protocol.http.GeoLocationDeserializer;
import com.betfair.cougar.util.HeaderUtils;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * TODO: keep internal
 */
public class BetfairGeoLocationDeserializer implements GeoLocationDeserializer {
    private final String ipHeader;
    private final String ipsHeader;

    public BetfairGeoLocationDeserializer(String ipHeader, String ipsHeader) {
        this.ipHeader = ipHeader;
        this.ipsHeader = ipsHeader;
    }

    @Override
    public List<String> deserialize(HttpServletRequest request, String remoteAddress) {

        final String xIP = HeaderUtils.cleanHeaderValue(request.getHeader(ipHeader));
        final String xIPs = HeaderUtils.cleanHeaderValue(request.getHeader(ipsHeader));
        List<String> resolvedAddresses = RemoteAddressUtils.parse(xIP, xIPs);
        if (resolvedAddresses.isEmpty() && remoteAddress != null) {
            resolvedAddresses = Collections.singletonList(remoteAddress);
        }
        return resolvedAddresses;
    }
}
