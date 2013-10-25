package com.betfair.cougar.core.api.ev;

/**
 *
 */
public interface ExecutionTimingRecorder {

    /**
     * Record a successful call to the resource being managed.
     * @param timeTakenMs the time in milliseconds that the call took to execute
     */
    public void recordCall(double timeTakenMs);

    /**
     * Record a call to the resource being managed that has resulted
     * in an Exception or other failure.
     * @param timeTakenMs the time in milliseconds that the failed call took to execute
     */
    public void recordFailure(double timeTakenMs);
}
