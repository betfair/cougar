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
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class QoSProcessorTest {

    @Mock
    private StatusSource trigger;
    @Mock
    private ExecutionPreProcessor wrappedProcessor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void equalStatusTriggers() {
        QoSProcessor processor = new QoSProcessor(trigger, Status.OK, wrappedProcessor);
        when(trigger.getStatus()).thenReturn(Status.OK);

        processor.invoke(null, null, null);

        verify(wrappedProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

    @Test
    public void worseStatusTriggers() {
        QoSProcessor processor = new QoSProcessor(trigger, Status.WARN, wrappedProcessor);
        when(trigger.getStatus()).thenReturn(Status.FAIL);

        processor.invoke(null, null, null);

        verify(wrappedProcessor, times(1)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }

    @Test
    public void betterStatusDoesntTrigger() {
        QoSProcessor processor = new QoSProcessor(trigger, Status.WARN, wrappedProcessor);
        when(trigger.getStatus()).thenReturn(Status.OK);

        processor.invoke(null, null, null);

        verify(wrappedProcessor, times(0)).invoke(any(ExecutionContext.class), any(OperationKey.class), any(Object[].class));
    }
}
