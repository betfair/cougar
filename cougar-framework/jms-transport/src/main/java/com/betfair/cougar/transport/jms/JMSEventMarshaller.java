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
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.transport.api.protocol.events.EventMarshaller;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.util.RequestUUIDImpl;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class will convert an Event object into a JSON formatted text message
 */
public class JMSEventMarshaller implements EventMarshaller<TextMessage> {
    public static final String  DEFAULT_CHARACTER_ENCODING = "utf-8";
    public static final int     DEFAULT_DELIVERY_MODE      = DeliveryMode.PERSISTENT;
    public static final long    DEFAULT_EXPIRATION_TIME    = 0;
    public static final int     DEFAULT_PRIORITY           = 4;

    public static final String DEFAULT_ROUTE_TIMESTAMP_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

    private String dateFormatPattern = DEFAULT_ROUTE_TIMESTAMP_DATE_FORMAT;
    private String characterEncoding = DEFAULT_CHARACTER_ENCODING;
    private int    deliveryMode      = DEFAULT_DELIVERY_MODE;
    private long   expirationTime    = DEFAULT_EXPIRATION_TIME;
    private int    priority          = DEFAULT_PRIORITY;

    private DataBindingFactory dataBindingFactory;


    public String marshallEventBody(Event event) throws CougarException {
        Marshaller marshaller = dataBindingFactory.getMarshaller();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshall(outputStream, event, characterEncoding, false);
        return outputStream.toString();
    }

    public String getHostString(Event event) throws UnknownHostException {
        StringBuilder sb = new StringBuilder();

        sb.append(InetAddress.getLocalHost().getCanonicalHostName());
        sb.append(JMSPropertyConstants.TIMESTAMP_SEPARATOR);
        sb.append(new SimpleDateFormat(dateFormatPattern).format(new Date()));

        if (event.getCougarMessageRouteString() != null) {
            sb.append(JMSPropertyConstants.COUGAR_ROUTING_SEPARATOR);
            sb.append(event.getCougarMessageRouteString());
        }
        return sb.toString();
    }

    @Override
    public TextMessage marshallEvent(EventServiceBindingDescriptor bindingDescriptor, Event event, Object session) throws CougarException {
        try {
            TextMessage message = ((Session)session).createTextMessage(marshallEventBody(event));

            //Sets the routing string
            message.setStringProperty(JMSPropertyConstants.MESSAGE_ROUTING_FIELD_NAME, getHostString(event));

            //Sets the message id guid.  If there isn't one, make one up
            String messageId = event.getMessageId();
            if (messageId == null) {
                messageId = new RequestUUIDImpl().toString();
            }
            message.setStringProperty(JMSPropertyConstants.MESSAGE_ID_FIELD_NAME, messageId);

            //Sets the version header
            message.setStringProperty(JMSPropertyConstants.EVENT_VERSION_FIELD_NAME, bindingDescriptor.getServiceVersion().toString());

            //Sets the event name header
            message.setStringProperty(JMSPropertyConstants.EVENT_NAME_FIELD_NAME, event.getClass().getSimpleName());

            message.setBooleanProperty("JMS_SonicMQ_preserveUndelivered", true);
            message.setBooleanProperty("JMS_SonicMQ_notifyUndelivered",   true);
            message.setJMSDeliveryMode(deliveryMode);
            message.setJMSExpiration(expirationTime);
            message.setJMSPriority(priority);

            return message;
        } catch (JMSException jmsex) {
            throw new CougarFrameworkException("Error marshalling Event", jmsex);
        } catch (UnknownHostException e) {
            throw new CougarFrameworkException("Error looking up local host name", e);
        }
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public DataBindingFactory getDataBindingFactory() {
        return dataBindingFactory;
    }

    public void setDataBindingFactory(DataBindingFactory dataBindingFactory) {
        this.dataBindingFactory = dataBindingFactory;
    }

    public void setDateFormatPattern(String dateFormatPattern) {
        if (dateFormatPattern!= null) {
            this.dateFormatPattern = dateFormatPattern;
        }
    }
}
