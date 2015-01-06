package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.impl.tracing.AbstractTracer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;
import com.google.common.base.Optional;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Zipkin tracer implementation
 */
public class ZipkinTracer extends AbstractTracer {

    private ZipkinEmitter zipkinEmitter;

    @Override
    public void start(RequestUUID uuid, OperationKey operation) {
        Objects.requireNonNull(operation);

        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) uuid;

            zipkinRequestUUID.setZipkinSpanName(operation.toString());

            Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
            // Check if Zipkin is enabled
            if (zipkinDataOptional.isPresent()) {
                zipkinEmitter.emitServerReceiveSpan(zipkinDataOptional.get());
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinData(uuid);
        // Check if Zipkin is enabled
        if (zipkinDataOptional.isPresent()) {
            zipkinEmitter.emitServerSendSpan(zipkinDataOptional.get());
        }
    }

    @Nonnull
    private static Optional<ZipkinData> getZipkinData(@Nonnull RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) uuid;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {
                return Optional.of(zipkinRequestUUID.getZipkinData());
            } else {
                return Optional.absent();
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private void emitAnnotation(@Nonnull ZipkinData zipkinData, String msg, Object... args) {
        String s = String.format(msg, args);
        zipkinEmitter.emitAnnotation(zipkinData, s);
    }

    public void setZipkinEmitter(@Nonnull ZipkinEmitter zipkinEmitter) {
        Objects.requireNonNull(zipkinEmitter);
        this.zipkinEmitter = zipkinEmitter;
    }
}
