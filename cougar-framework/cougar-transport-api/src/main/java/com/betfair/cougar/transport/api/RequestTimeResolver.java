package com.betfair.cougar.transport.api;

import java.io.IOException;
import java.util.Date;

public interface RequestTimeResolver<I, O> {

    public Date resolveRequestTime(I input);

    public void writeRequestTime(O output);
}

