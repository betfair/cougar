package com.betfair.cougar.client.api;

import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import org.apache.http.Header;

import java.util.List;

/**
 *
 */
public interface GeoLocationSerializer {

    void serialize(GeoLocationDetails gld, List<Header> result);
}
