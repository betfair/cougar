package com.betfair.cougar.core.impl.tracing.zipkin;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.impl.tracing.AbstractTracer;

import javax.annotation.Resource;

/**
 * Zipkin tracer implementation
 */
public class ZipkinTracer extends AbstractTracer {

    @Resource(name = ZipkinEmitter.BEAN_NAME)
    private ZipkinEmitter zipkinEmitter;

    @Override
    public void start(RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUIDImpl) {
            ZipkinRequestUUIDImpl zipkinRequestUUID = (ZipkinRequestUUIDImpl) uuid;

            ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

            // Zipkin not enabled
            if (zipkinData != null) {
                zipkinEmitter.emitServerStartSpan(zipkinData);
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        emitAnnotation(uuid, msg);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        emitAnnotation(uuid, msg, arg1);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        emitAnnotation(uuid, msg, arg1, arg2);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        emitAnnotation(uuid, msg, arg1, arg2, arg3);
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        emitAnnotation(uuid, msg, args);
    }

    @Override
    public void end(RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUIDImpl) {
            ZipkinRequestUUIDImpl zipkinRequestUUID = (ZipkinRequestUUIDImpl) uuid;

            ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

            // Zipkin not enabled
            if (zipkinData != null) {
                zipkinEmitter.emitServerStopSpan(zipkinData);
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private void emitAnnotation(RequestUUID uuid, String msg, Object... args) {
        if (uuid instanceof ZipkinRequestUUIDImpl) {
            ZipkinRequestUUIDImpl zipkinRequestUUID = (ZipkinRequestUUIDImpl) uuid;

            ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

            // Check if Zipkin is enabled
            if (zipkinData != null) {
                String s = String.format(msg, args);

                zipkinEmitter.emitAnnotation(zipkinData, s);
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }
}
