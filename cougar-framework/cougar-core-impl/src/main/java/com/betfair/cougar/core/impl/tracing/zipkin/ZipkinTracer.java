package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.zipkin.ZipkinData;
import com.betfair.cougar.core.impl.tracing.AbstractTracer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

/**
 * Zipkin tracer implementation
 */
public class ZipkinTracer extends AbstractTracer {

    @Resource(name = ZipkinEmitter.BEAN_NAME)
    private ZipkinEmitter zipkinEmitter;

    @Override
    public void start(RequestUUID uuid) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            zipkinEmitter.emitServerStartSpan(zipkinData);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        ZipkinData zipkinData = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinData != null) {
            zipkinEmitter.emitServerStopSpan(zipkinData);
        }
    }

    @Nullable
    private static ZipkinData getZipkinData(@Nonnull RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUIDImpl) {
            ZipkinRequestUUIDImpl zipkinRequestUUID = (ZipkinRequestUUIDImpl) uuid;

            return zipkinRequestUUID.getZipkinData();
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private void emitAnnotation(@Nonnull ZipkinData zipkinData, String msg, Object... args) {
        String s = String.format(msg, args);
        zipkinEmitter.emitAnnotation(zipkinData, s);
    }
}
