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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Credential;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.Subscription;
import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.events.EventTransportIdentity;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.transports.EventTransportMode;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.protocol.events.EventBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.EventErrorHandler;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSDestinationResolver;
import com.betfair.cougar.transport.jms.monitoring.ConnectionMonitor;
import com.betfair.cougar.transport.jms.monitoring.TopicSubscriberPingMonitor;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import junit.framework.Assert;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Test case for SonicEventTransportImpl
 *
 */
public class JmsEventTransportImplTest {
    private JmsEventTransportImpl cut;
    private EventServiceBindingDescriptor evsd;

    private Object observedResult;
    private Subscription observedSubscription;
    private Session mockedSession;
    private MessageListener messageListener;
    private MessageConsumer mockedConsumer;
    private MonitorRegistry monitorRegistry;
    private Connection mockedConnection;

    private ExecutionObserver inbredExecutionObserver = new ExecutionObserver() {

        @Override
        public void onResult(ExecutionResult result) {
            switch (result.getResultType()) {
                case Success:
                    observedResult = result.getResult();
                    break;
                case Fault:
                    observedResult = result.getFault();
                    break;
                case Subscription:
                    observedResult = result.getSubscription();
                    break;
            }
        }
    };

    private static final String EVENT_NAME = "myDummyEvent";

    @Before
    public void setup() throws Exception {
        mockedConsumer = Mockito.mock(TopicSubscriber.class);
        mockedSession = Mockito.mock(Session.class);
        when(mockedSession.createConsumer((Destination)anyObject())).thenReturn(mockedConsumer);

        MessageProducer mockedProducer = Mockito.mock(MessageProducer.class);
        when(mockedSession.createProducer((Destination)anyObject())).thenReturn(mockedProducer);

        TopicSubscriber mockedTopicSubscriber = Mockito.mock(TopicSubscriber.class);
        when(mockedSession.createDurableSubscriber((Topic)anyObject(), anyString())).thenReturn(mockedTopicSubscriber);

        mockedConnection = Mockito.mock(Connection.class);

        ConnectionFactory mockedConnectionFactory = Mockito.mock(ConnectionFactory.class);
        when(mockedConnectionFactory.createConnection(anyString(), anyString())).thenReturn(mockedConnection);

        cut = new JmsEventTransportImpl(mockedConnectionFactory);
        cut.setDestinationResolver(new EventNameDestinationResolver("base"));
        cut.setConnectionMonitor(new ConnectionMonitor(false));
        monitorRegistry = mock(MonitorRegistry.class);
        cut.setMonitorRegistry(monitorRegistry);

        TopicSubscriberPingMonitor pingMonitor = mock(TopicSubscriberPingMonitor.class);

        messageListener = cut.new SubscriptionMessageListener(inbredExecutionObserver, DummyEventImpl.class, pingMonitor);
        when(mockedConsumer.getMessageListener()).thenReturn(messageListener);

        JMSEventUnMarshaller unMarshaller = Mockito.mock(JMSEventUnMarshaller.class);
        when(unMarshaller.unmarshallEvent(argThat(contains(DummyEventImpl.class)), eq(DummyEventImpl.class), (TextMessage) anyObject())).thenReturn(DummyEventImpl.BOBS_ADDRESS);

        cut.setEventUnMarshaller(unMarshaller);


        TextMessage mockedMessage = Mockito.mock(TextMessage.class);
        JMSEventMarshaller marshaller = Mockito.mock(JMSEventMarshaller.class);
        when(marshaller.marshallEvent((EventServiceBindingDescriptor) Matchers.anyObject(), (Event)anyObject(), (Session)anyObject())).thenReturn(mockedMessage);

        cut.setEventMarshaller(marshaller);

        evsd = new EventServiceBindingDescriptor() {

            @Override
            public EventBindingDescriptor[] getEventBindings() {
                return new EventBindingDescriptor[] {
                        new EventBindingDescriptor() {

                            @Override
                            public String getEventName() {
                                return EVENT_NAME;
                            }

                            @Override
                            public Class<? extends Event> getEventClass() {
                                return DummyEventImpl.class;
                            }
                        }
                };
            }

            @Override
            public String getServiceName() {
                return "TestService";
            }

            @Override
            public String getServiceNamespace() {
                return "ns";
            }

            @Override
            public ServiceVersion getServiceVersion() {
                return new ServiceVersion("v1.0");
            }


            @Override
            public Protocol getServiceProtocol() {
                return Protocol.JMS;
            }
        };
    }

    private void setupDefaultMockedConnectionBehaviour() throws JMSException {
        when(mockedConnection.createSession(anyBoolean(), anyInt())).thenReturn(mockedSession);
    }

    private Matcher<List<Class<? extends Event>>> contains(final Class<DummyEventImpl> dummyEventClass) {
        return new ArgumentMatcher<List<Class<? extends Event>>>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof List) {
                    List list = (List) o;
                    return list.contains(dummyEventClass);
                }
                return false;
            }
        };
    }

    @Test
    public void testSubscribe() throws JMSException {
        setupDefaultMockedConnectionBehaviour();
        //try subscribing to an event that doesn't exist
        try {
            cut.subscribe("bobTheBuilder", null, null);
            fail("Should have thrown an exception here");
        } catch (CougarValidationException anticipated) {}

        cut.notify(evsd, EventTransportMode.Subscribe);
        try {
            cut.subscribe("bobTheBuilder", null, null);
            fail("Should have thrown an exception here");
        } catch (CougarValidationException anticipated) {}

        //Check that a subscription identifier name is expected
        cut.setDestinationType(JmsEventTransportImpl.DestinationType.DurableTopic);
        cut.subscribe(EVENT_NAME, null, inbredExecutionObserver);
        assertEquals("Should have thrown a CougarFrameworkException as a subscription id must be supplied", CougarFrameworkException.class, this.observedResult.getClass());

        observedResult = null;

        cut.subscribe(EVENT_NAME, new Object[] { "myConnectionId" }, inbredExecutionObserver);
        assertNotNull(observedResult);
        assertTrue(observedResult instanceof Subscription);

        ArgumentCaptor<Monitor> monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorRegistry, times(2)).addMonitor(monitorCaptor.capture());

        Assert.assertTrue(containsInstanceOf(monitorCaptor.getAllValues(), TopicSubscriberPingMonitor.class));
        Assert.assertTrue(containsInstanceOf(monitorCaptor.getAllValues(), ConnectionMonitor.class));

        TextMessage mockedMessage = Mockito.mock(TextMessage.class);
        mockedMessage.setText("{\"name\":\"bob\", \"address\":\"100 chancellors\"}");
        messageListener.onMessage(mockedMessage);
        assertNotNull(observedResult);
    }

    private boolean containsInstanceOf(List<Monitor> allValues, Class c) {
        for (Monitor m : allValues) {
            if (c.isAssignableFrom(m.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testPublish() throws JMSException {
        setupDefaultMockedConnectionBehaviour();
        cut.setDestinationType(JmsEventTransportImpl.DestinationType.Queue);
        cut.initThreadPool();
        cut.notify(evsd);
        cut.publish(DummyEventImpl.BOBS_ADDRESS);

        //check exception wrapping mechanism for when a JMS exception is thrown
        JMSDestinationResolver alwaysResolvesNull = new JMSDestinationResolver<String>() {
            @Override
            public String resolveDestination(Class<? extends Event> eventClass, Object[] args) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        // Test exceptions thrown at publication time are passed out correctly
        cut.setDestinationResolver(alwaysResolvesNull);
        when(mockedSession.createProducer(null)).thenThrow(new JMSException("xx"));
        try {
            cut.publish(DummyEventImpl.BOBS_ADDRESS);
            fail("An exception should have been thrown here");
        } catch (CougarException ignored) {}
    }

    @Test
    // Test exceptions thrown when session created are passed out correctly
    public void testSessionCreationFailure() throws JMSException {
        cut.setDestinationType(JmsEventTransportImpl.DestinationType.Queue);
        cut.initThreadPool();
        cut.notify(evsd);

        when(mockedConnection.createSession(anyBoolean(),anyInt())).thenThrow(new JMSException("xx"));

        try {
            cut.publish(DummyEventImpl.BOBS_ADDRESS);
            fail("An exception should have been thrown here");
        } catch (CougarException ignored) {}
    }

    @Test
    public void testOnMessage() throws JMSException {
        TopicSubscriberPingMonitor pingMonitor = mock(TopicSubscriberPingMonitor.class);
        JmsEventTransportImpl.SubscriptionMessageListener sml=new JmsEventTransportImpl(null).new SubscriptionMessageListener(null,null, pingMonitor);
        sml.onMessage(null);
    }

    @Test
    public void testOnMessage2() throws JMSException {
        TopicSubscriberPingMonitor pingMonitor = mock(TopicSubscriberPingMonitor.class);
        JmsEventTransportImpl.SubscriptionMessageListener sml=new JmsEventTransportImpl(null).new SubscriptionMessageListener(faultyExecutionObserver,null, pingMonitor);
        TextMessage mockedMessage = Mockito.mock(TextMessage.class);
        sml.onMessage(mockedMessage);
    }

    @Test
    public void testOnMessage3() throws JMSException {
        cut.setErrorHandler(faultyErrorHandler);
        TopicSubscriberPingMonitor pingMonitor = mock(TopicSubscriberPingMonitor.class);
        JmsEventTransportImpl.SubscriptionMessageListener sml=cut.new SubscriptionMessageListener(faultyExecutionObserver,null, pingMonitor);
        TextMessage mockedMessage = Mockito.mock(TextMessage.class);
        sml.onMessage(mockedMessage);
    }

    @Test
    public void testExecutionContextCreation1() {
        cut.initialiseExecutionContext();
        assertNotNull(cut.getExecutionContext());
        //Start with an empty execution context
        List<EventTransportIdentity> transportIdentityChain = cut.getExecutionContext().getIdentity().getIdentities(EventTransportIdentity.class);
        assertNotNull(transportIdentityChain);
        assertFalse(transportIdentityChain.isEmpty());
        EventTransportIdentity eti = transportIdentityChain.get(0);
        assertEquals(eti.getEventTransportName(), "JmsEventTransportImpl");
    }

    @Test
    public void testExecutionContextCreation2() {
        final List<Identity> identities = new ArrayList<Identity>();
        identities.add(new Identity() {
            @Override
            public Principal getPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return "TestPrincipal";
                    }
                };
            }
            public Credential getCredential() { return null; }
        });
        final IdentityChain identityChain = new IdentityChain() {
            @Override
            public List<Identity> getIdentities() {
                return identities;
            }

            @Override
            public void addIdentity(Identity identity) {
                identities.add(identity);
            }

            @Override
            public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
                List<T> identityList = new ArrayList<T>();
                for (Identity i : identities) {
                    if (clazz.isAssignableFrom(i.getClass())) {
                        identityList.add((T)i);
                    }
                }
                return identityList;
            }
        };

        cut.setExecutionContext(new ExecutionContext() {
            public IdentityChain getIdentity() { return identityChain; }

            public GeoLocationDetails getLocation() { return null; }
            public RequestUUID getRequestUUID() { return null; }
            public Date getReceivedTime() { return null; }
            public Date getRequestTime() { return null; }
            public boolean traceLoggingEnabled() { return false; }
            public int getTransportSecurityStrengthFactor() { return 0; }
            public boolean isTransportSecure() { return false; }
        });
        cut.initialiseExecutionContext();
        assertNotNull(cut.getExecutionContext());
        assertTrue(cut.getExecutionContext().getIdentity().getIdentities().size() == 2);

        List<EventTransportIdentity> transportIdentityList =
                cut.getExecutionContext().getIdentity().getIdentities(EventTransportIdentity.class);

        assertNotNull(transportIdentityList);
        assertFalse(transportIdentityList.isEmpty());
        EventTransportIdentity eti = transportIdentityList.get(0);
        assertEquals(eti.getEventTransportName(), "JmsEventTransportImpl");
    }


    private ExecutionObserver faultyExecutionObserver = new ExecutionObserver() {
        @Override
        public void onResult(ExecutionResult executionResult) {
            throw new RuntimeException(executionResult.getResultType() + "-told you it was faulty.");
        }
    };

    private EventErrorHandler faultyErrorHandler=new EventErrorHandler() {
        @Override
        public void handleEventProcessingError(Object errorEvent, Throwable exception) {
            throw new RuntimeException(" handleEventProcessingError-Told you it was faulty.");
        }
    } ;
}
