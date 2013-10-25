package com.betfair.cougar.core.api.ev;

/**
 *
 */
public class NullExecutionTimingRecorder implements ExecutionTimingRecorder {
    @Override
    public void recordCall(double timeTakenMs) {
    }

    @Override
    public void recordFailure(double timeTakenMs) {
    }
}
