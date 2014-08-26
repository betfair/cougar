/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.core.impl.ev;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusSource;

/**
 * Quality of Service ExecutionPreProcessor. Always executed.
 * If the trigger's status is worse than the triggering status then the wrapped processor will be invoked.
 */
public class QoSProcessor implements ExecutionPreProcessor {

    private StatusSource trigger;
    private Status triggeringStatus;
    private ExecutionPreProcessor processor;

    public QoSProcessor(StatusSource trigger, Status triggeringStatus, ExecutionPreProcessor processor) {
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
