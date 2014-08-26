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
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.BindingDescriptor;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.events.Event;
import com.betfair.cougar.core.api.events.EventTransportIdentity;
import com.betfair.cougar.core.api.events.EventTransportIdentityImpl;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transports.AbstractRegisterableTransport;
import com.betfair.cougar.core.api.transports.EventTransport;
import com.betfair.cougar.core.api.transports.EventTransportMode;
import com.betfair.cougar.core.impl.ev.DefaultSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.protocol.events.EventBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.EventErrorHandler;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.events.EventUnMarshaller;
import com.betfair.cougar.transport.api.protocol.events.jms.JMSDestinationResolver;
import com.betfair.cougar.transport.jms.monitoring.ConnectionMonitor;
import com.betfair.cougar.transport.jms.monitoring.TopicPublisherPingMonitor;
import com.betfair.cougar.transport.jms.monitoring.TopicSubscriberPingMonitor;
import com.betfair.cougar.transport.jms.monitoring.PingEvent;
import com.betfair.cougar.util.JMXReportingThreadPoolExecutor;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import com.betfair.cougar.util.jmx.JMXControl;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * This class implements an abstract JMS based transport implementation.  The implementation
 * contains a set of defaults for a JMS transport that are overrideable through spring
 * config if you'd like to do something different. This class must be sub-classed to create a concrete impl.
 */
public class JmsEventTransportImpl extends AbstractRegisterableTransport implements EventTransport, ApplicationContextAware, InitializingBean {

    public static enum DestinationType {
        Queue,
        Topic,
        DurableTopic
    }

    private SessionManager sessionManager = new SessionManager();

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsEventTransportImpl.class);
    private EventTransportMode transportMode;

    //Connection arguments, spring injected
    private DestinationType destinationType;
    private String username;
    private String password;
    private int acknowledgementMode = defaultAcknowledgementMode();


    //Other spring properties
    private JMSEventMarshaller eventMarshaller;
    private EventUnMarshaller eventUnMarshaller;
    private EventErrorHandler errorHandler;
    private JMSDestinationResolver<String> destinationResolver;
    private ExecutionContext executionContext;
    private String transportIdentifier;

    private Map<String, EventBindingDescriptor> bindingDescriptorMap = new HashMap<String, EventBindingDescriptor>();
    private EventServiceBindingDescriptor eventServiceBindingDescriptor;
    private ConnectionFactory connectionFactory;

    private AtomicReference<Connection> connection = new AtomicReference<Connection>();
    //Synchronisation object for connection creation locking
    private final Object connectionMonitorObject = new Object();

    // monitoring bits
    private MonitorRegistry monitorRegistry;
    private ConnectionMonitor connectionMonitor;
    private final ScheduledExecutorService reconnectorService = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("SonicReconnector-"));
    private List<TopicPublisherPingMonitor> publisherPingMonitors = new ArrayList<TopicPublisherPingMonitor>();
    private Map<Session, TopicSubscriberPingMonitor> subscriberMonitorsBySession = new ConcurrentHashMap<Session, TopicSubscriberPingMonitor>();
    // used to get the jmx control
    private ApplicationContext applicationContext;
    private JMXControl jmxControl;

    //Event thread pool size, defaulted to 1 but can be overridden by spring injection
    private int threadPoolSize = 1;
    private JMXReportingThreadPoolExecutor threadPool = null;


    public JmsEventTransportImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void init() throws Exception {
        register();
        initConnectionFactory(connectionFactory);

        initialiseExecutionContext();

        initThreadPool();
    }

    protected void initConnectionFactory(ConnectionFactory connectionFactory) {
        // for sub-classes to use if they so desire..
    }

    protected int defaultAcknowledgementMode() {
        return Session.CLIENT_ACKNOWLEDGE;
    }

    protected String getTransportName() {
        return getClass().getSimpleName();
    }

    protected String getTransportShortName() {
        return "jms";
    }

    // Initialise the thread pool to have the requested number of threads available, life span of threads (set to 0) not used as threads will never be eligible to die as coreSize = maxSize
    public void initThreadPool() {
        CustomizableThreadFactory ctf = new CustomizableThreadFactory();
        ctf.setDaemon(true);
        ctf.setThreadNamePrefix(getTransportName()+"-Publisher-");
        threadPool = new JMXReportingThreadPoolExecutor(threadPoolSize, threadPoolSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), ctf);
    }

    public void initialiseExecutionContext() {
        final Identity transportIdentity = new EventTransportIdentityImpl(JmsEventTransportImpl.this, transportIdentifier);

        if (executionContext == null) {
            executionContext = new ExecutionContext() {

                @Override
                public GeoLocationDetails getLocation() { return null; }

                @Override
                public IdentityChain getIdentity() {
                    return new IdentityChain() {
                        List<Identity> identities= new ArrayList<Identity>(1) {{ add(transportIdentity);}};

                        @Override
                        public List<Identity> getIdentities() {
                            return identities;
                        }

                        @Override
                        public void addIdentity(Identity identity) {
                            identities.add(identity);
                        }

                        @SuppressWarnings({"unchecked"})
                        @Override
                        public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
                            List<T> filteredIdentityList = new ArrayList<T>();
                            for (Identity identity : identities) {
                                if (clazz.isAssignableFrom(identity.getClass())) {
                                    filteredIdentityList.add((T)identity);
                                }
                            }
                            return filteredIdentityList;
                        }

                        public String toString() {
                            StringBuffer sb = new StringBuffer("Sonic IdentityChain_");
                            for (int i=0; i<identities.size(); i++) {
                                sb.append("Identity:").append(i).append(" ").append(identities.get(i)).append(" ");
                            }
                            return sb.toString();
                        }
                    };
                }

                @Override
                public RequestUUID getRequestUUID() { return null; }

                @Override
                public Date getReceivedTime() { return null; }

                @Override
                public Date getRequestTime() { return null; }

                @Override
                public boolean traceLoggingEnabled() { return false;}

                @Override
                public int getTransportSecurityStrengthFactor() {
                    return 0;  // todo: where do we get this from??
                }

                @Override
                public boolean isTransportSecure() {
                    return getTransportSecurityStrengthFactor() > 1;
                }
            };
        } else {
            if (executionContext.getIdentity() != null &&
                    executionContext.getIdentity().getIdentities(EventTransportIdentity.class).isEmpty()) {

                final ExecutionContext delegateExecutionContext = executionContext;
                final List<Identity> augmentedIdentityChainList = new ArrayList<Identity>(delegateExecutionContext.getIdentity().getIdentities());
                augmentedIdentityChainList.add(transportIdentity);

                executionContext = new ExecutionContext() {
                    @Override
                    public IdentityChain getIdentity() {
                        IdentityChain ic = new IdentityChain() {
                            @Override
                            public List<Identity> getIdentities() {
                                return augmentedIdentityChainList;
                            }

                            @Override
                            public void addIdentity(Identity identity) {
                                augmentedIdentityChainList.add(identity);
                            }

                            @Override
                            public <T extends Identity> List<T> getIdentities(Class<T> clazz) {
                                List<T> filteredIdentityChain = new ArrayList<T>();
                                for (Identity identity : augmentedIdentityChainList) {
                                    if (clazz.isAssignableFrom(identity.getClass())) {
                                        filteredIdentityChain.add((T)identity);
                                    }
                                }
                                return filteredIdentityChain;
                            }

                            public String toString() {
                                StringBuffer sb = new StringBuffer("Sonic IdentityChain_\n");
                                for (int i=0; i<augmentedIdentityChainList.size(); i++) {
                                    sb.append("Identity:").append(i).append(" ").append(augmentedIdentityChainList.get(i)).append(" ");
                                }
                                return sb.toString();
                            }
                        };
                        return ic;
                    }
                    @Override
                    public RequestUUID getRequestUUID() { return delegateExecutionContext.getRequestUUID(); }
                    @Override
                    public Date getReceivedTime() { return delegateExecutionContext.getReceivedTime(); }
                    @Override
                    public Date getRequestTime() { return delegateExecutionContext.getRequestTime(); }
                    @Override
                    public boolean traceLoggingEnabled() { return delegateExecutionContext.traceLoggingEnabled(); }
                    @Override
                    public GeoLocationDetails getLocation() { return delegateExecutionContext.getLocation(); }
                    @Override
                    public int getTransportSecurityStrengthFactor() { return delegateExecutionContext.getTransportSecurityStrengthFactor(); }
                    @Override
                    public boolean isTransportSecure() { return delegateExecutionContext.isTransportSecure(); }
                };
            }
        }
    }

    public void destroy() throws Exception {
        unregister();
        if (connection.get() != null) {
            closeConnection();
        }
    }

    /**
     * Publish the supplied event to the destination.  The destination address (to publish the event to)
     * will be derived using the plugged destination resolver.  If you want to implement different / more
     * advanced destination resolution, implement your own JMSDestinationResolver.  Note that obviously
     * both the publisher and consumer must arrive at the same destination name, so should be using the
     * same convention (since they're unlikely to be running in the same Cougar instance)
     * The event will be published using a thread from the pool and its associated jms session
     *
     * @param event
     * @throws com.betfair.cougar.core.api.exception.CougarException
     * @see com.betfair.cougar.transport.api.protocol.events.jms.JMSDestinationResolver
     */
    @Override
    public void publish(Event event) throws CougarException {
        String destinationName = destinationResolver.resolveDestination(event.getClass(), null);
        publish(event, destinationName, eventServiceBindingDescriptor);
    }

    /**
     * Publish the supplied event to the destination.
     * The event will be published using a thread from the pool and its associated jms session
     *
     * @param event
     * @throws com.betfair.cougar.core.api.exception.CougarException
     * @see com.betfair.cougar.transport.api.protocol.events.jms.JMSDestinationResolver
     */
    public void publish(Event event, String destinationName, EventServiceBindingDescriptor eventServiceBindingDescriptor) throws CougarException {
        try {
            EventPublisherRunnable publisherRunnable = new EventPublisherRunnable(event, destinationName, eventServiceBindingDescriptor);
            threadPool.execute(publisherRunnable); // Publish the event using a thread from the pool
            publisherRunnable.lock();

            if (!publisherRunnable.isSuccess()) { // If publication failed for any reason pass out the exception thrown
                Exception e = publisherRunnable.getError();
                LOGGER.error("Publication exception:", e);
                throw new CougarFrameworkException("Sonic JMS publication exception", e);
            }
        } catch (InterruptedException ex) { // Interrupted while waiting for event to be published
            LOGGER.error("Publication exception:", ex);
            throw new CougarFrameworkException("Sonic JMS publication exception", ex);
        }
    }

    // Runnable object that emits an event using the jms session belonging to the thread running it
    private class EventPublisherRunnable implements Runnable {

        private Event event;
        private String destinationName;
        private EventServiceBindingDescriptor descriptor;
        private boolean success = false;
        private Exception error;
        private CountDownLatch publishLock;

        public EventPublisherRunnable(Event event, String destinationName, EventServiceBindingDescriptor descriptor) {
            this.event = event;
            this.destinationName = destinationName;
            this.descriptor = descriptor;
            publishLock = new CountDownLatch(1);
        }

        public boolean isSuccess() {
            return success;
        }

        public Exception getError() {
            return error;
        }

        public void lock() throws InterruptedException {
            publishLock.await();
        }

        public void unlock() {
            publishLock.countDown();
        }

        @Override
        // Get the jms session belonging to the thread publishing this event
        public void run() {
            MessageProducer messageProducer = null;
            try {
                Session session = sessionManager.get();
                Destination destination = createDestination(session, destinationName);
                messageProducer = session.createProducer(destination);
                TextMessage textMessage = getEventMarshaller().marshallEvent(descriptor, event, session);
                messageProducer.send(textMessage, textMessage.getJMSDeliveryMode(), textMessage.getJMSPriority(), textMessage.getJMSExpiration());
                success = true;
            } catch (CougarFrameworkException cfe) { // Catch possible exception thrown from session creation
                success = false;
                error = cfe;
            } catch (JMSException ex) { // Catch any other exception thrown during message publication
                success = false;
                error = ex;
            } finally {
                if (messageProducer != null) {
                    try {
                        messageProducer.close();
                    } catch (JMSException e) {
                        LOGGER.warn("Failed to close message producer", e);
                    }
                }
                unlock();
            }
        }
    }

    private Destination createDestination(Session session, String destinationName) throws JMSException {
        try {
            Destination destination = null;
            switch (destinationType) {
                case DurableTopic:
                case Topic:
                    destination = session.createTopic(destinationName);
                    break;
                case Queue:
                    destination = session.createQueue(destinationName);
                    break;
            }
            return destination;
        }
        catch (InvalidDestinationException ide) {
            throw new CougarFrameworkException("Error creating "+destinationType+" for destination name '"+destinationName+"'",ide);
        }
    }


    private Class<? extends Event> getEventClass(String eventName) {
        if (!bindingDescriptorMap.containsKey(eventName.toLowerCase())) {
            throw new CougarValidationException(ServerFaultCode.NoSuchOperation, "Unable to find binding for event named[" + eventName + "]");
        }
        EventBindingDescriptor eventBindingDescriptor = bindingDescriptorMap.get(eventName.toLowerCase());
        final Class<? extends Event> eventClass = eventBindingDescriptor.getEventClass();
        return eventClass;
    }

    protected void connectionCreated(Connection c) {

    }

    private Connection getConnection() throws JMSException {
        Connection c = connection.get();
        if (c == null) {
            synchronized (connectionMonitorObject) {
                c = connection.get();
                if (c == null) {
                    c = connectionFactory.createConnection(getUsername(), getPassword());
                    c.start();
                    connectionCreated(c);
                    connectionMonitor.connectionStarted(c);
                    setupPingEmitters();
                    connection.set(c);
                }
            }
        }
        return c;
    }

    /**
     * Requests that this transports attempts to connect to the broker. Occurs asynchronously from the caller initiation.
     */
    public Future<Boolean> requestConnectionToBroker() {
        FutureTask futureTask = new FutureTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean ok = false;
                try {
                    getConnection();
                    ok = true;
                } catch (JMSException e) {
                    LOGGER.warn("Error connecting to JMS", e);
                }
                return ok;
            }
        });
        reconnectorService.schedule(futureTask, 0, TimeUnit.SECONDS);
        return futureTask;
    }

    protected void connectionClosed(Connection c) {

    }

    public void closeConnection() throws JMSException {
        synchronized (connectionMonitorObject) {
            Connection c = connection.getAndSet(null);
            if (c != null) {
                c.close();
                connectionMonitor.connectionClosed(c);
                connectionClosed(c);
                sessionManager.clear();
                pausePingEmitters();
            }
        }
    }

    private MessageConsumer createConsumer(Session session, String destinationName, String subscriptionId) throws JMSException {
        MessageConsumer consumer = null;
        switch (destinationType) {
            case DurableTopic:
                consumer = session.createDurableSubscriber(session.createTopic(destinationName), subscriptionId);
                break;
            case Topic:
                consumer = session.createConsumer(session.createTopic(destinationName));
                break;
            case Queue:
                consumer = session.createConsumer(session.createQueue(destinationName));
                break;
        }
        return consumer;
    }

    /**
     * Subscribe to events of the supplied name.  Your passed in observer will be called back in each of the following
     * circumstances:
     * <ul>
     * <li>onResult for each message
     * <li>onSubscription will be called when a connection is successfully established to the message broker,
     * supplying you a Subscription object, which can use to close your connection
     * <li>onException if an exception occurs when establishing the connection.
     * <li>if the related SubscriptionEventListener (if supported by this JMS impl) detects loss or failure of connection to the broker.
     * </ul>
     * The destination address (to listen for events from) will be derived using the plugged destination resolver.
     * If you want to implement different / more advanced destination resolution, implement your own
     * JMSDestinationResolver.  Note that both the publisher and consumer must arrive at the same destination name,
     * so should be using the same convention (since they're unlikely to be running in the same Cougar instance)
     *
     * @param eventName - the name of the event, matching the event name defined in the IDD
     * @param args      - subscription arguments, note that if you've asked for a durable subscription, then you MUST supply
     *                  a durable subscription identifier as the zeroth argument
     * @param observer  - the callback to be notified on message events
     */
    @Override
    public void subscribe(String eventName, Object[] args, final ExecutionObserver observer) {

        final Class<? extends Event> eventClass = getEventClass(eventName);
        String destinationName = destinationResolver.resolveDestination(eventClass, null);
        String subId = null;

        if (destinationType == destinationType.DurableTopic) {
            if (args == null || "".equals(args[0])) {
                observer.onResult(new ExecutionResult(new CougarFrameworkException("Durable subscription requires a subscription Identifier to be set as the zeroth arg!")));
                return;
            }
            subId = args[0].toString();
        }

        try {
            final Session session = getConnection().createSession(false, acknowledgementMode);

            final TopicSubscriberPingMonitor pingMonitor = setupPingConsumer(eventName, destinationName, subId, session);

            MessageConsumer consumer = createConsumer(session, destinationName, subId);

            consumer.setMessageListener(new SubscriptionMessageListener(observer, eventClass, pingMonitor));

            subscriptionAdded(observer);

            observer.onResult(new ExecutionResult(new DefaultSubscription() {
                @Override
                public void preClose(CloseReason reason) {
                    try {
                        if (pingMonitor != null) {
                            if (monitorRegistry != null) {
                                monitorRegistry.removeMonitor(pingMonitor);
                            }
                            subscriberMonitorsBySession.remove(pingMonitor);
                        }
                        session.close();
                    } catch (JMSException ex) {
                        LOGGER.error("Exception occurred when trying to close JMSConnection", ex);
                    }
                }
            }));
        } catch (JMSException ex) {
            observer.onResult(new ExecutionResult(new CougarFrameworkException("Subscription exception!", ex)));
        } catch (CougarException ce) {
            observer.onResult(new ExecutionResult(ce));
        }
    }

    protected void subscriptionAdded(ExecutionObserver observer) {
    }

    public class SubscriptionMessageListener implements MessageListener {
        private ExecutionObserver observer;
        private Class<? extends Event> eventClass;
        private TopicSubscriberPingMonitor pingMonitor;

        public SubscriptionMessageListener(ExecutionObserver observer, Class<? extends Event> eventClass, TopicSubscriberPingMonitor pingMonitor) {
            this.observer = observer;
            this.eventClass = eventClass;
            this.pingMonitor = pingMonitor;
        }

        @Override
        public void onMessage(Message message) {
            try {
                if (!(message instanceof TextMessage)) {
                    handleInvalidMessageType(message);
                    return;
                }

                Event e = eventUnMarshaller.unmarshallEvent(Arrays.asList(PingEvent.class, eventClass), eventClass, message);
                if (e instanceof PingEvent) {
                    // if pingMonitor not setup then just swallow..
                    if (pingMonitor != null) {
                        pingMonitor.pingReceived((PingEvent) e);
                    }
                }
                else {
                    observer.onResult(new ExecutionResult(e));
                }
                message.acknowledge();
            } catch (Throwable t) { //NOSONAR
                handleThrowable(message, t);
            }
        }

        private void handleThrowable(Message m, Throwable t) {
            try {
                errorHandler.handleEventProcessingError(m, t);
            } catch (Throwable t2) { //NOSONAR
                //This method MUST NEVER THROW ANYTHING
            }
        }

        private void handleInvalidMessageType(Message m) {
            String type = "null";
            if (m != null) {
                type = m.getClass().getName();
            }
            try {
                observer.onResult(new ExecutionResult(new CougarFrameworkException("Received message not a text message!",
                        new ClassCastException(
                                "Could not convert received message from type [" + type + "] to TextMessage"))));
            } catch (Exception e) {
                LOGGER.error("",e);
            }
            LOGGER.error("Class of message unsupported by this container [" + type + "]");
        }
    }//SubscriptionMessageListener

    private class SessionManager {
        private Map<Thread, Session> sessionMap = new ConcurrentHashMap<Thread, Session>();
        public Session get() {
            Thread t = Thread.currentThread();
            Session s = sessionMap.get(t);
            if (s == null) {
                try {
                    s = getConnection().createSession(false, acknowledgementMode);
                } catch (JMSException ex) {
                    throw new CougarFrameworkException("Error Creating Session", ex);
                }
                sessionMap.put(t, s);
            }
            return s;
        }

        public void clear() {
            sessionMap.clear();
        }
    }//SessionManager

    private TopicSubscriberPingMonitor setupPingConsumer(String eventName, String destinationName, String subscriptionId, Session session) {
        TopicSubscriberPingMonitor ret = null;
        if (destinationType == DestinationType.Topic || destinationType == DestinationType.DurableTopic) {
            boolean monitorThisEvent = monitorEvent(eventName);
            if (monitorThisEvent) {
                long pingFailureTimeout = getSubscriberPingFailureTimeout(eventName);
                long pingWarningTimeout = getSubscriberPingWarningTimeout(eventName);
                Status maxStatus = getSubscriberMaxEffectOnOverallStatus(eventName);
                ret = new TopicSubscriberPingMonitor(transportIdentifier, destinationName, subscriptionId, pingWarningTimeout, pingFailureTimeout, maxStatus);
                subscriberMonitorsBySession.put(session, ret);
                if (monitorRegistry != null) {
                    monitorRegistry.addMonitor(ret);
                }
                if (jmxControl != null) {
                    String name = "CoUGAR."+getTransportShortName()+".transport.monitoring:type=subscriber,serviceName="+eventServiceBindingDescriptor.getServiceName()+",serviceVersion="+eventServiceBindingDescriptor.getServiceVersion()+",eventName="+eventName+",destination="+destinationName;
                    if (transportIdentifier != null) {
                        name += ",transportIdentifier="+transportIdentifier;
                    }
                    jmxControl.registerMBean(name, ret);
                }
            }
        }
        return ret;
    }

    private void setupPingEmitters() {
        if (publisherPingMonitors.isEmpty()) {
            for (EventBindingDescriptor descriptor : eventServiceBindingDescriptor.getEventBindings()) {
                boolean emitPing = sendPingForEvent(descriptor.getEventName());
                if (emitPing) {
                    String destination = getDestinationResolver().resolveDestination(descriptor.getEventClass(), null);
                    long pingPeriod = getPingPeriod(descriptor.getEventName());
                    Status maxImpact = getPublisherMaxEffectOnOverallStatus(descriptor.getEventName());
                    TopicPublisherPingMonitor monitor = new TopicPublisherPingMonitor(this, pingPeriod, destination, maxImpact);
                    publisherPingMonitors.add(monitor);
                    if (monitorRegistry != null) {
                        monitorRegistry.addMonitor(monitor);
                    }
                    if (jmxControl != null) {
                        String name = "CoUGAR."+getTransportShortName()+".transport.monitoring:type=publisher,serviceName="+eventServiceBindingDescriptor.getServiceName()+",serviceVersion="+eventServiceBindingDescriptor.getServiceVersion()+",eventName="+descriptor.getEventName()+",destination="+destination;
                        if (transportIdentifier != null) {
                            name += ",transportIdentifier="+transportIdentifier;
                        }
                        jmxControl.registerMBean(name, monitor);
                    }
                }
            }
        }
        for (TopicPublisherPingMonitor monitor : publisherPingMonitors) {
            monitor.connectionOpened();
        }
    }

    private void pausePingEmitters() {
        for (TopicPublisherPingMonitor monitor : publisherPingMonitors) {
            monitor.connectionClosed();
        }
    }

    private boolean monitorEvent(String eventName) {
        return transportMode == EventTransportMode.Subscribe && "true".equalsIgnoreCase(getMonitoringProperty(eventName, ".subscriber", "true"));

    }

    private long getSubscriberPingFailureTimeout(String eventName) {
        return Long.parseLong(getMonitoringProperty(eventName, ".subscriber.pingFailureTimeout", "60000"));
    }

    private long getSubscriberPingWarningTimeout(String eventName) {
        return Long.parseLong(getMonitoringProperty(eventName, ".subscriber.pingWarningTimeout", String.valueOf(getSubscriberPingFailureTimeout(eventName)/2)));
    }

    private Status getSubscriberMaxEffectOnOverallStatus(String eventName) {
        return Status.valueOf(getMonitoringProperty(eventName, ".subscriber.maxImpactForPing", "FAIL"));
    }

    private boolean sendPingForEvent(String eventName) {
        return transportMode == EventTransportMode.Publish && "true".equalsIgnoreCase(getMonitoringProperty(eventName, ".publisher.emitPing", "true"));
    }

    private long getPingPeriod(String eventName) {
        return Long.parseLong(getMonitoringProperty(eventName, ".publisher.pingPeriod", "10000"));
    }

    private Status getPublisherMaxEffectOnOverallStatus(String eventName) {
        return Status.valueOf(getMonitoringProperty(eventName, ".publisher.maxImpactForPing", "FAIL"));
    }

    private String getMonitoringProperty(String eventName, String suffix, String defaultValue) {
        String s = PropertyConfigurer.getAllLoadedProperties().get("cougar."+getTransportShortName()+".monitor."+eventName+suffix);
        if (s == null) {
            return defaultValue;
        }
        return s;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    @Required
    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public JMSEventMarshaller getEventMarshaller() {
        return eventMarshaller;
    }

    @Required
    public void setEventMarshaller(JMSEventMarshaller eventMarshaller) {
        this.eventMarshaller = eventMarshaller;
    }

    public EventUnMarshaller getEventUnMarshaller() {
        return eventUnMarshaller;
    }

    @Required
    public void setEventUnMarshaller(EventUnMarshaller eventUnMarshaller) {
        this.eventUnMarshaller = eventUnMarshaller;
    }

    public EventErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Required
    public void setErrorHandler(EventErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void notify(BindingDescriptor bindingDescriptor) {
        notify(bindingDescriptor, EventTransportMode.Publish);
    }

    @Override
    public void notify(BindingDescriptor bindingDescriptor, EventTransportMode mode) {
        if (bindingDescriptor.getServiceProtocol() == Protocol.JMS) {
            eventServiceBindingDescriptor = (EventServiceBindingDescriptor) bindingDescriptor;
            this.transportMode = mode;

            for (EventBindingDescriptor eventBinding : eventServiceBindingDescriptor.getEventBindings()) {
                this.bindingDescriptorMap.put(eventBinding.getEventName().toLowerCase(), eventBinding);
            }

            connectionMonitor.setTransport(this);
            String transportIdentifierSuffix = transportIdentifier != null ? ",transportIdentifier="+transportIdentifier:"";
            if (monitorRegistry != null) {
                monitorRegistry.addMonitor(connectionMonitor);
                if (jmxControl != null) {
                    jmxControl.registerMBean("CoUGAR."+getTransportShortName()+".transport.monitoring:type=connection,serviceName=" + eventServiceBindingDescriptor.getServiceName() + ",serviceVersion=" + eventServiceBindingDescriptor.getServiceVersion()+transportIdentifierSuffix, connectionMonitor);
                }
            }
            if (jmxControl != null) {
                registerMBeans(jmxControl, eventServiceBindingDescriptor, transportIdentifierSuffix);
                jmxControl.registerMBean("CoUGAR."+getTransportShortName()+".transport:type=threadPool,serviceName="+eventServiceBindingDescriptor.getServiceName()+",serviceVersion="+eventServiceBindingDescriptor.getServiceVersion()+transportIdentifierSuffix, threadPool);
            }
        }
    }

    protected void registerMBeans(JMXControl jmxControl, EventServiceBindingDescriptor eventServiceBindingDescriptor, String transportIdentifierSuffix) {

    }


    public JMSDestinationResolver<String> getDestinationResolver() {
        return destinationResolver;
    }

    @Required
    public void setDestinationResolver(JMSDestinationResolver<String> destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    public void setMonitorRegistry(MonitorRegistry monitorRegistry) {
        this.monitorRegistry = monitorRegistry;
    }

    @Required
    public void setConnectionMonitor(ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            jmxControl = JMXControl.getFromContext(applicationContext);
        }
        catch (RuntimeException re) {
            // ignore, jmxControl will be null
        }
    }


    public int getAcknowledgementMode() {
        return acknowledgementMode;
    }

    /**
     * Sets the acknowledgement mode for messages
     *
     * @param acknowledgementMode
     */
    public void setAcknowledgementMode(int acknowledgementMode) {
        this.acknowledgementMode = acknowledgementMode;
    }

    /**
     * Returns the execution context associated with this instance of the SETI transport
     * @return returns the executionContext
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Sets the execution context associated with this SETI instance. Note that there
     * must be one of these things - and that this class will create a default one for
     * you. If you don't need anything special, this is almost certainly fine for you.
     * Note that if you're setting this property and you want to use multiple event
     * transports, you MUST set something in the identity chain to differentiate one
     * instance from another, otherwise you won't be able to tell which transport
     * is which when your app is called back and you start it up.
     */
    public void setExecutionContext(final ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Used for differentiation for multiple event transports
     * @return returns the namespace associated with this transport
     */
    public String getTransportIdentifier() {
        return transportIdentifier;
    }

    /**
     * Used for differentiation for multiple event transports
     * @param transportIdentifier used
     */
    public void setTransportIdentifier(String transportIdentifier) {
        this.transportIdentifier = transportIdentifier;
    }

    public String getUsername() {
        return username;
    }

    @Required
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Required
    public void setPassword(String password) {
        this.password = password;
    }
}
