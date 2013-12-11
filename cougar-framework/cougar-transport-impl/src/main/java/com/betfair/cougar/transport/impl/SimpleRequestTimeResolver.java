package com.betfair.cougar.transport.impl;

import com.betfair.cougar.transport.api.RequestTimeResolver;

import java.util.Date;

/**
 *
 */
public abstract class SimpleRequestTimeResolver<I> implements RequestTimeResolver<I> {
    private boolean clientTimeSynchronizedWithServer;

    public SimpleRequestTimeResolver(boolean clientTimeSynchronizedWithServer) {
        this.clientTimeSynchronizedWithServer = clientTimeSynchronizedWithServer;
    }

    protected abstract Date readRequestTime(I input);

    @Override
    public Date resolveRequestTime(I input) {
        if (clientTimeSynchronizedWithServer) {
            Date d = readRequestTime(input);
            if (d != null) {
                return d;
            }
        }
        return new Date();
    }
}
