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

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.betfair.cougar.transport.api.protocol.events.AbstractEvent;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.StartsWith;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

/**
 * Unit test for @see JMSEventMarshaller
 */
public class JMSEventMarshallerTest {
    @Mock
    private DataBindingFactory dbf;

    @Mock
    private Session session;

    @Mock
    private Message message;

    private static final String EXPECTED_RESULT = "{\"name\":\"bob\", \"address\":\"100 chancellors\"}";

    private Marshaller marshaller = new Marshaller() {

        @Override
        public void marshall(OutputStream outputStream, Object result, String encoding, boolean client) {
            try {
                outputStream.write(EXPECTED_RESULT.getBytes(encoding));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getFormat() {
            return "json";
        }
    };


    private JMSEventMarshaller cut;

    @BeforeClass
    public static void setupStatic() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

    @Before
    public void init() throws JMSException {
        MockitoAnnotations.initMocks(this);

        cut = new JMSEventMarshaller();
        cut.setDataBindingFactory(dbf);

        Mockito.when(dbf.getMarshaller()).thenReturn(marshaller);
    }

    @Test
    public void test() throws JMSException {
        TextMessage msg = Mockito.mock(TextMessage.class);
        Mockito.when(msg.getText()).thenReturn(EXPECTED_RESULT);
        Mockito.when(session.createTextMessage(Matchers.anyString())).thenReturn(msg);

        EventServiceBindingDescriptor eventServiceBindingDescriptor = Mockito.mock(EventServiceBindingDescriptor.class);
        Mockito.when(eventServiceBindingDescriptor.getServiceVersion()).thenReturn(new ServiceVersion(1, 0));

        TextMessage result = cut.marshallEvent(eventServiceBindingDescriptor, new ExpectedEvent("bob", "100 chancellors"), session);
        Mockito.verify(session).createTextMessage(EXPECTED_RESULT);

        assertEquals(EXPECTED_RESULT, result.getText());
    }

    @Test
    public void headerTest() throws JMSException, UnknownHostException {
        TextMessage msg = Mockito.mock(TextMessage.class);

        Mockito.when(session.createTextMessage(Matchers.anyString())).thenReturn(msg);

        EventServiceBindingDescriptor eventServiceBindingDescriptor = Mockito.mock(EventServiceBindingDescriptor.class);
        Mockito.when(eventServiceBindingDescriptor.getServiceVersion()).thenReturn(new ServiceVersion(1, 0));
        cut.marshallEvent(eventServiceBindingDescriptor, new ExpectedEvent("bob", "100 chancellors"), session);

        String thisHost = InetAddress.getLocalHost().getCanonicalHostName();

        Mockito.verify(msg).setStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ID_FIELD_NAME), Matchers.anyString());
        Mockito.verify(msg).setStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ROUTING_FIELD_NAME), Matchers.argThat(new StartsWith(thisHost)));
    }

    @Test
    public void chainedHeaderTest() throws JMSException, UnknownHostException {
        TextMessage msg = Mockito.mock(TextMessage.class);

        String messageId = "342432";

        Mockito.when(session.createTextMessage(Matchers.anyString())).thenReturn(msg);

        EventServiceBindingDescriptor eventServiceBindingDescriptor = Mockito.mock(EventServiceBindingDescriptor.class);
        Mockito.when(eventServiceBindingDescriptor.getServiceVersion()).thenReturn(new ServiceVersion(1, 0));

        AbstractEvent e = new ExpectedEvent("bob", "100 chancellors");

        String firstHost = "a.host";

        e.setCougarMessageRouteString(firstHost + JMSPropertyConstants.TIMESTAMP_SEPARATOR + "19/01/2011 14:21");
        e.setMessageId(messageId);

        cut.marshallEvent(eventServiceBindingDescriptor, e, session);

        String secondHost = InetAddress.getLocalHost().getCanonicalHostName();

        String[] hostList = new String[] { firstHost, secondHost };

        Mockito.verify(msg).setStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ID_FIELD_NAME), Matchers.eq(messageId));
        Mockito.verify(msg).setStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ROUTING_FIELD_NAME), Matchers.argThat(includesBothHostsInOrder(hostList)));

    }

    private ArgumentMatcher<String> includesBothHostsInOrder(final Object hosts) {
        return new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                assertTrue(argument instanceof String);
                String routingString = (String)argument;

                String[] hostArray = (String[])hosts;
                int hostPos1 = routingString.indexOf(hostArray[0]);
                int hostPos2 = routingString.indexOf(hostArray[1]);

                assertTrue("Host " + hostArray[0] + " was not found in message routing string", hostPos1 != -1);
                assertTrue("Host " + hostArray[1] + " was not found in message routing string", hostPos2 != -1);
                assertTrue("Host strings are out of order", hostPos1 > hostPos2);

                return true;
            }
        };
    }


}
