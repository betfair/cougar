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
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case for @See JMSEventUnMarshaller
 */
public class JMSEventUnMarshallerTest {
    private JMSEventUnMarshaller unMarshaller;

    @Mock
    private UnMarshaller unMarshallerImpl;

    @Mock
    private DataBindingFactory dbf;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        unMarshaller = new JMSEventUnMarshaller();
        unMarshaller.setDataBindingFactory(dbf);

        Mockito.when(dbf.getUnMarshaller()).thenReturn(unMarshallerImpl);
    }

    @Test
    public void testUnMarshalling() throws JMSException {
        String expectedMessageId="21231123xxx";
        String expectedRoutingString="betfair.com@19/1/2011 15:07";
        String expectedEventName=ExpectedEvent.class.getSimpleName();

        TextMessage message = Mockito.mock(TextMessage.class);

        Mockito.when(message.getStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ID_FIELD_NAME))).thenReturn(expectedMessageId);
        Mockito.when(message.getStringProperty(Matchers.eq(JMSPropertyConstants.MESSAGE_ROUTING_FIELD_NAME))).thenReturn(expectedRoutingString);
        Mockito.when(message.getStringProperty(Matchers.eq(JMSPropertyConstants.EVENT_NAME_FIELD_NAME))).thenReturn(expectedEventName);
        Mockito.when(message.getText()).thenReturn("");

        ExpectedEvent e = Mockito.mock(ExpectedEvent.class);
        Mockito.when(unMarshallerImpl.unmarshall(Matchers.any(InputStream.class), Matchers.eq(ExpectedEvent.class), Matchers.anyString(), Matchers.eq(true))).thenReturn(e);

        List<Class<? extends Event>> eventBodyClasses = new ArrayList<Class<? extends Event>>();
        eventBodyClasses.add(ExpectedEvent.class);
//        eventBodyClasses.add(PingEvent.class); todo: move ping events..

        unMarshaller.unmarshallEvent(eventBodyClasses, ExpectedEvent.class, message);

        Mockito.verify(e).setCougarMessageRouteString(expectedRoutingString);
        Mockito.verify(e).setMessageId(expectedMessageId);
    }



}
