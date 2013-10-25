package com.betfair.cougar.client;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.client.api.GeoLocationSerializer;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.List;

/**
 *
 */
public class DefaultGeoLocationSerializer implements GeoLocationSerializer {

    @Override
    public void serialize(GeoLocationDetails gld, List<Header> result) {
        if (gld != null) {
            String ipAddressList = RemoteAddressUtils.externaliseWithLocalAddresses(gld.getResolvedAddresses());
            if (ipAddressList != null && !ipAddressList.isEmpty()) {
                result.add(new BasicHeader("X-Forwarded-For", ipAddressList));
            }
        }
    }
}
