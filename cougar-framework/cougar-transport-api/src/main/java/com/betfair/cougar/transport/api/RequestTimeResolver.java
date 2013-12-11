package com.betfair.cougar.transport.api;

import java.util.Date;

public interface RequestTimeResolver<I> {

    /**
     * Resolves the request time in the server's time that this request was made by the client. This method MUST NOT return null.
     */
    public Date resolveRequestTime(I input);
}

