package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.transport.impl.SimpleRequestTimeResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 *
 */
public class SimpleHttpRequestTimeResolver extends SimpleRequestTimeResolver<HttpServletRequest,Object> {

    private String requestTimeHeader;

    public SimpleHttpRequestTimeResolver(String requestTimeHeader, boolean clientTimeSynchronizedWithServer) {
        super(clientTimeSynchronizedWithServer);
        this.requestTimeHeader = requestTimeHeader;
    }

    @Override
    protected Date readRequestTime(HttpServletRequest input) {
        Long time = null;
        if (requestTimeHeader != null && input.getHeader(requestTimeHeader) != null) {
            try {
                time = Long.parseLong(input.getHeader(requestTimeHeader));
            }
            catch (NumberFormatException nfe) {
                // defaults to null
            }
        }
        if (time != null) {
            return new Date(time);
        }
        return null;
    }

    @Override
    public void writeRequestTime(Object output) {
        throw new UnsupportedOperationException();
    }
}
