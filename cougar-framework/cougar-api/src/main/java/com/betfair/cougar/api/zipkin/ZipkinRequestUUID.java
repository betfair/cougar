package com.betfair.cougar.api.zipkin;

import com.betfair.cougar.api.RequestUUID;

import javax.annotation.Nullable;

public interface ZipkinRequestUUID extends RequestUUID {

    /**
     * Represents Zipkin's data.
     * @return object will all Zipkin data, or null if it wasn't set already.
     */
    @Nullable
    public ZipkinData getZipkinData();

}
