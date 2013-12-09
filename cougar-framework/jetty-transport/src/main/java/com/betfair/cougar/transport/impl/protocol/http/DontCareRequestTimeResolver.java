package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.transport.api.RequestTimeResolver;

import java.util.Date;

/**
 *
 */
public class DontCareRequestTimeResolver implements RequestTimeResolver {
    @Override
    public Date resolveRequestTime(Object input) {
        return new Date();
    }

    @Override
    public void writeRequestTime(Object output) {
        throw new UnsupportedOperationException();
    }
}
