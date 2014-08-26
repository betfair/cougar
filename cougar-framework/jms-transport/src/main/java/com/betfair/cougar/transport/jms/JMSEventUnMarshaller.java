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

import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.transport.api.protocol.events.AbstractEvent;
import com.betfair.cougar.transport.api.protocol.events.EventUnMarshaller;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;

/**
 * This JMS specific implementation of the event resolver interface will take a
 * JSON based TextMessage, and marshall it to the IDL defined event object.
 */
public class JMSEventUnMarshaller implements EventUnMarshaller<TextMessage> {
    private final static Logger LOGGER = LoggerFactory.getLogger(JMSEventUnMarshaller.class);

    public static final String DEFAULT_ENCODING_TYPE = "utf-8";

    private DataBindingFactory dataBindingFactory;

    //String encoding type, defaulted to UTF-8, but you can override this in spring config
    private String encodingType = DEFAULT_ENCODING_TYPE;

    @Override
    public Event unmarshallEvent(List<Class<? extends Event>> eventBodyClasses, Class<? extends Event> defaultBodyClass, TextMessage transportEvent) throws CougarException {
        InputStream is = null;
        try {
            String messageText = ((TextMessage)transportEvent).getText();
            LOGGER.debug("Received message with body [" + messageText + "]");
            is = new ByteArrayInputStream(messageText.getBytes(encodingType));

            String eventNameFromMessage = transportEvent.getStringProperty(JMSPropertyConstants.EVENT_NAME_FIELD_NAME);
            Class<? extends Event> correctClass = defaultBodyClass;
            for (Class c : eventBodyClasses) {
                if (c.getSimpleName().equals(eventNameFromMessage)) {
                    correctClass = c;
                    break;
                }
            }
            if (correctClass == null) {
                throw new CougarFrameworkException("Can't find event class for event named '"+eventNameFromMessage+"'");
            }

            AbstractEvent event = (AbstractEvent) dataBindingFactory.getUnMarshaller().unmarshall(is, correctClass, encodingType, true);

            event.setMessageId(transportEvent.getStringProperty(JMSPropertyConstants.MESSAGE_ID_FIELD_NAME));
            event.setCougarMessageRouteString(transportEvent.getStringProperty(JMSPropertyConstants.MESSAGE_ROUTING_FIELD_NAME));

            //When other types of JMS field types (eg, not stored in the message body) become necessary
            //This is where they'll be added

            return event;
        } catch (UnsupportedEncodingException ex) {
            //This is never going to happen in a month of Sundays
            throw new CougarFrameworkException("Unsupported encoding exception for JMS Event", ex);
        } catch (JMSException jmsex) {
            throw new CougarFrameworkException("Unsupported encoding exception for JMS Event", jmsex);
        }
    }


    public DataBindingFactory getDataBindingFactory() {
        return dataBindingFactory;
    }

    @Required
    public void setDataBindingFactory(DataBindingFactory dataBindingFactory) {
        this.dataBindingFactory = dataBindingFactory;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }


}
