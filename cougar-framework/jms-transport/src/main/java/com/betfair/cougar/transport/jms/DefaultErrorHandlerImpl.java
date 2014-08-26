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

package com.betfair.cougar.transport.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.protocol.events.EventErrorHandler;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.logging.Level;

/**
 * this default implementation of the @see com.betfair.cougar.transport.api.protocol.events.EventErrorHandler
 * interface logs and acks a bad message.  If you require additional functionality implement your own
 * EventErrorHandler and wire that in to the EventHandler implementation
 */
public class DefaultErrorHandlerImpl implements EventErrorHandler<Message> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultErrorHandlerImpl.class);

    @Override
    public void handleEventProcessingError(Message errorEvent, Throwable exception) {
        LOGGER.error("An error occurred processing event: [" + errorEvent + "]", exception);
        try {
            errorEvent.acknowledge();
        } catch (JMSException ex) {
            LOGGER.error("An error occured acknowledging bad JMS message [" + errorEvent  + "]", ex);
        }
    }
}
