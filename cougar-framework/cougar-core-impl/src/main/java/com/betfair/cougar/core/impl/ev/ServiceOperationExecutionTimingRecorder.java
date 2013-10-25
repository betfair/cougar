package com.betfair.cougar.core.impl.ev;

import com.betfair.tornjak.kpi.KPIMonitor;

/**
 *
 */
public class ServiceOperationExecutionTimingRecorder extends SimpleExecutionTimingRecorder {

    private final String operationName;

    public ServiceOperationExecutionTimingRecorder(KPIMonitor stats, String statsName, String operationName) {
        super(stats, statsName);
        this.operationName = operationName;
    }

    @Override
    public void recordCall(double timeTakenMs) {
        super.recordCall(timeTakenMs);
        stats.addEvent(statsName, operationName, timeTakenMs, true);
    }

    @Override
    public void recordFailure(double timeTakenMs) {
        super.recordFailure(timeTakenMs);
        stats.addEvent(statsName, operationName, timeTakenMs, false);
    }
}
