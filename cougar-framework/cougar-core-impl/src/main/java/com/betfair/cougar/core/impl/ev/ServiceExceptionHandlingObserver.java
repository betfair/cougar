package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;

/**
 *
 */
public class ServiceExceptionHandlingObserver implements ExecutionObserver {

    private ExecutionObserver wrapped;

    public ServiceExceptionHandlingObserver(ExecutionObserver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void onResult(ExecutionResult executionResult) {
        if (!executionResult.isFault()) {
            wrapped.onResult(executionResult);
            return;
        }

        CougarException ce = executionResult.getFault();
        if (ce instanceof CougarClientException) {
            wrapped.onResult(new ExecutionResult(new CougarServiceException(ServerFaultCode.ServiceRuntimeException,"Unhandled client exception",ce)));
        }
        else {
            wrapped.onResult(executionResult);
        }
    }
}
