package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.core.api.ev.ExecutionTimingRecorder;
import com.betfair.tornjak.kpi.KPIMonitor;

/**
 *
 */
public class SimpleExecutionTimingRecorder implements ExecutionTimingRecorder {

    protected final KPIMonitor stats;
    protected final String statsName;

    public SimpleExecutionTimingRecorder(KPIMonitor stats, String statsName) {
        this.stats = stats;
        this.statsName = statsName;
    }

    @Override
    public void recordCall(double timeTakenMs) {
        stats.addEvent(statsName, timeTakenMs, true);
    }

    @Override
    public void recordFailure(double timeTakenMs) {
        stats.addEvent(statsName, timeTakenMs, false);
    }
}
