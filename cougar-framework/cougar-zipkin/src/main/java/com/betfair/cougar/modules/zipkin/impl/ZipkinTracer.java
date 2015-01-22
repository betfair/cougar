package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.impl.tracing.AbstractTracer;
import com.betfair.cougar.modules.zipkin.api.ZipkinData;
import com.betfair.cougar.modules.zipkin.api.ZipkinRequestUUID;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Zipkin tracer implementation
 */
public class ZipkinTracer extends AbstractTracer {

    private ZipkinEmitter zipkinEmitter;

    @Override
    public void start(RequestUUID uuid, OperationKey operationKey) {
        ZipkinData zipkinData = buildZipkinDataIfEnabled(uuid, operationKey);
        if (zipkinData != null) {
            zipkinEmitter.emitServerReceive(zipkinData);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1, arg2);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object arg1, Object arg2, Object arg3) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, arg1, arg2, arg3);
        }
    }

    @Override
    public void trace(RequestUUID uuid, String msg, Object... args) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            emitAnnotation(zipkinData, msg, args);
        }
    }

    @Override
    public void end(RequestUUID uuid) {
        ZipkinData zipkinData = getZipkinDataIfReady(uuid);
        if (zipkinData != null) {
            zipkinEmitter.emitServerSend(zipkinData);
        }
    }

    @Override
    public void startCall(RequestUUID uuid, RequestUUID subUuid, OperationKey operationKey) {
        ZipkinData zipkinData = buildZipkinDataIfEnabled(subUuid, operationKey);
        if (zipkinData != null) {
            zipkinEmitter.emitClientSend(zipkinData);
        }
    }

    @Override
    public void endCall(RequestUUID uuid, RequestUUID subUuid, OperationKey operationKey) {
        ZipkinData zipkinData = getZipkinDataIfReady(subUuid);
        if (zipkinData != null) {
            zipkinEmitter.emitClientReceive(zipkinData);
        }
    }

    private static ZipkinData getZipkinDataIfReady(@Nonnull RequestUUID uuid) {
        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinRequestUUID = (ZipkinRequestUUID) uuid;

            if (zipkinRequestUUID.isZipkinTracingReady()) {
                return zipkinRequestUUID.getZipkinData();
            } else {
                // Zipkin is disabled or wasn't initialized properly yet (the latter should never happen)
                return null;
            }
        } else {
            return null;
        }
    }

    private static ZipkinData buildZipkinDataIfEnabled(@Nonnull RequestUUID uuid, @Nonnull OperationKey operationKey) {
        Objects.requireNonNull(operationKey);

        if (uuid instanceof ZipkinRequestUUID) {
            ZipkinRequestUUID zipkinSubRequestUUID = (ZipkinRequestUUID) uuid;

            if (zipkinSubRequestUUID.isZipkinTracingEnabled()) {

                zipkinSubRequestUUID.buildZipkinData(operationKey.toString());

                return zipkinSubRequestUUID.getZipkinData();
            } else {
                return null;
            }
        } else {
            return null;
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
