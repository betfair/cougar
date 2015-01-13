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
    public void start(RequestUUID uuid, OperationKey operationKey) {
        Objects.requireNonNull(operationKey);

        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) uuid;

            if (zipkinRequestUUID.isZipkinTracingEnabled()) {

                zipkinRequestUUID.setZipkinSpanName(operationKey.toString());

                ZipkinData zipkinData = zipkinRequestUUID.getZipkinData();

                zipkinEmitter.emitServerReceive(zipkinData);
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            emitAnnotation(zipkinDataOptional.get(), msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        Optional<ZipkinData> zipkinDataOptional = getZipkinDataIfReady(uuid);
        // Check if Zipkin is ready
        if (zipkinDataOptional.isPresent()) {
            zipkinEmitter.emitServerSend(zipkinDataOptional.get());
        }
    }

    @Override
    public void subCall(RequestUUID uuid, RequestUUID subUuid, OperationKey operationKey) {
        Objects.requireNonNull(operationKey);

        if (subUuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinSubRequestUUID = (ZipkinRequestUUID) subUuid;

            if (zipkinSubRequestUUID.isZipkinTracingEnabled()) {

                zipkinSubRequestUUID.setZipkinSpanName(operationKey.toString());

                ZipkinData zipkinData = zipkinSubRequestUUID.getZipkinData();

                zipkinEmitter.emitClientSend(zipkinData);
            }
        } else {
            throw new IllegalStateException("RequestUUID is not a ZipkinRequestUUIDImpl");
        }
    }

    private static Optional<ZipkinData> getZipkinDataIfReady(@Nonnull RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) uuid;

            if (zipkinRequestUUID.isZipkinTracingReady()) {
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
