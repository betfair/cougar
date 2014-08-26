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
