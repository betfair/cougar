package com.betfair.cougar.netutil.nio.marshalling;

import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.impl.SimpleRequestTimeResolver;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class DefaultSocketTimeResolver extends SimpleRequestTimeResolver<Long> {

    public DefaultSocketTimeResolver() {
        super(false);
    }

    public DefaultSocketTimeResolver(boolean clientTimeSynchronizedWithServer) {
        super(clientTimeSynchronizedWithServer);
    }

    @Override
    protected Date readRequestTime(Long input) {
        return new Date(input);
    }
}
