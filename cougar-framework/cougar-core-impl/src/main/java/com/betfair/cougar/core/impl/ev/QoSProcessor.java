package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.Status;

/**
 * Quality of Service ExecutionPreProcessor. Always executed.
 * If the trigger's status is worse than the triggering status then the wrapped processor will be invoked.
 */
public class QoSProcessor implements ExecutionPreProcessor {

    private Monitor trigger;
    private Status triggeringStatus;
    private ExecutionPreProcessor processor;

    public QoSProcessor(Monitor trigger, Status triggeringStatus, ExecutionPreProcessor processor) {
        this.trigger = trigger;
        this.triggeringStatus = triggeringStatus;
        this.processor = processor;
    }

    @Override
    public ExecutionRequirement getExecutionRequirement() {
        return ExecutionRequirement.EVERY_OPPORTUNITY;
    }

    @Override
    public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
        if (trigger.getStatus().ordinal() >= triggeringStatus.ordinal()) {
            return processor.invoke(ctx, key, args);
        }
        return new InterceptorResult(InterceptorState.CONTINUE);
    }

    @Override
    public String getName() {
        return "QoSProcessor("+processor.getName()+")";
    }
}
