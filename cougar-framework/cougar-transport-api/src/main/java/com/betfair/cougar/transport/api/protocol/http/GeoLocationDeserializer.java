package com.betfair.cougar.transport.api.protocol.http;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface GeoLocationDeserializer {
    List<String> deserialize(HttpServletRequest request, String remoteAddress);
}
