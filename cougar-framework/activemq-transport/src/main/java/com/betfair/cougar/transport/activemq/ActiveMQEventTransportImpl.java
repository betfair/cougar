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

package com.betfair.cougar.transport.activemq;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.transport.activemq.monitoring.ManagedActiveMQConnection;
import com.betfair.cougar.transport.api.protocol.events.EventServiceBindingDescriptor;
import com.betfair.cougar.transport.jms.JmsEventTransportImpl;
import com.betfair.cougar.util.jmx.JMXControl;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * This class implements an ActiveMQ JMS based transport implementation.  The implementation
 * contains a set of defaults for a Sonic transport that are overrideable through spring
 * config if you'd like to do something different
 */
public class ActiveMQEventTransportImpl extends JmsEventTransportImpl {

    //Connection arguments, spring injected
    private String destinationUrl;

    // ssl settings
    private String trustStorePath;
    private String trustStoreType;
    private String trustStorePassword;

    //Sonic properties
    private boolean loadBalancing = true;
    //Sonic uses this to determine if the url list should be traversed sequentially
    private boolean sequential = true;
    private boolean durableMessageOrder = true;
    private boolean faultTolerant = true;
    private int faultTolerantReconnectTimeoutSeconds = 30;
    private int socketConnectTimeoutSeconds = 10;
    private int initialConnectTimeoutInSeconds = 10;
    private int reconnectTimeoutInMinutes = 10;
    private int monitorIntervalInSeconds = 20;
    private int pingIntervalInSeconds = 20;

    //Other spring properties
    private ActiveMQSubscriptionEventListener activeMQSubscriptionEventListener;

    // monitoring bits
    private ManagedActiveMQConnection managedSonicConnection = new ManagedActiveMQConnection();

    public ActiveMQEventTransportImpl() throws JMSException {
        this(new ActiveMQSslConnectionFactory());
    }

    public ActiveMQEventTransportImpl(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    protected int defaultAcknowledgementMode() {
        return ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE;
    }

    @Override
    protected void initConnectionFactory(javax.jms.ConnectionFactory connectionFactory) {
        ActiveMQSslConnectionFactory activeMQConnectionFactory = (ActiveMQSslConnectionFactory) connectionFactory;//NOSONAR

        // TODO: Setup
//        activeMQConnectionFactory.setConnectionURLs(destinationUrl);

//        activeMQConnectionFactory.setLoadBalancing(loadBalancing);
//        activeMQConnectionFactory.setSequential(sequential);
//        activeMQConnectionFactory.setDurableMessageOrder(durableMessageOrder);
//        activeMQConnectionFactory.setFaultTolerant(faultTolerant);
//        activeMQConnectionFactory.setFaultTolerantReconnectTimeout(faultTolerantReconnectTimeoutSeconds);
//        activeMQConnectionFactory.setSocketConnectTimeout(socketConnectTimeoutSeconds * 1000);
//        activeMQConnectionFactory.setInitialConnectTimeout(initialConnectTimeoutInSeconds);
//        activeMQConnectionFactory.setReconnectTimeout(reconnectTimeoutInMinutes);
//        activeMQConnectionFactory.setMonitorInterval(monitorIntervalInSeconds);
//        activeMQConnectionFactory.setPingInterval(pingIntervalInSeconds);
    }

    protected void connectionCreated(Connection c) {
        managedSonicConnection.setConnection((ActiveMQConnection)c);
        if (activeMQSubscriptionEventListener != null) {//NOSONAR
//            ((ActiveMQConnection)c).addTransportListener(activeMQSubscriptionEventListener);
        }
    }

    @Override
    protected void connectionClosed(Connection c) {
        managedSonicConnection.setConnection(null);
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    @Required
    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    @Override
    protected void subscriptionAdded(ExecutionObserver observer) {
        if (activeMQSubscriptionEventListener != null) {
            activeMQSubscriptionEventListener.addObserver(observer);
        }
    }

    protected void registerMBeans(JMXControl jmxControl, EventServiceBindingDescriptor eventServiceBindingDescriptor, String transportIdentifierSuffix) {
        jmxControl.registerMBean("CoUGAR.activemq.transport:type=connection,serviceName="+eventServiceBindingDescriptor.getServiceName()+",serviceVersion="+eventServiceBindingDescriptor.getServiceVersion()+transportIdentifierSuffix, managedSonicConnection);
    }

    @Override
    protected String getTransportShortName() {
        return "activemq";
    }

    //-------------------------- Sonic Overridable properties ----------------/

    public boolean isLoadBalancing() {
        return loadBalancing;
    }

    public void setLoadBalancing(boolean loadBalancing) {
        this.loadBalancing = loadBalancing;
    }

    public boolean isSequential() {
        return sequential;
    }

    /**
     * Sonic Sequential connection set searching:
     * If true, starts attempting to connect to the first broker in the list;
     * if false, starts attempting to connect to a random element in the list.
     * After that, tries to connect to each broker in sequence until a
     * successful connect occurs, or the list is exhausted.
     *
     * @param sequential - if true, start with the first broker in the list, if false choose one randomly
     */
    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public boolean isDurableMessageOrder() {
        return durableMessageOrder;
    }

    /**
     * Enable or disable preservation of message order for reconnecting durable suscribers
     *
     * @param durableMessageOrder, if true, reconnecting durable subscribers will preserve message order
     */
    public void setDurableMessageOrder(boolean durableMessageOrder) {
        this.durableMessageOrder = durableMessageOrder;
    }

    public boolean isFaultTolerant() {
        return faultTolerant;
    }

    /**
     * Enables fault tolerant connection creation.
     *
     * @param faultTolerant
     */
    public void setFaultTolerant(boolean faultTolerant) {
        this.faultTolerant = faultTolerant;
    }

    public int getFaultTolerantReconnectTimeoutSeconds() {
        return faultTolerantReconnectTimeoutSeconds;
    }

    /**
     * Indicates how long (in seconds) the client will try to establish a connection,
     * only applicable to fault tolerant connections
     *
     * @param faultTolerantReconnectTimeoutSeconds
     *
     */
    public void setFaultTolerantReconnectTimeoutSeconds(int faultTolerantReconnectTimeoutSeconds) {
        this.faultTolerantReconnectTimeoutSeconds = faultTolerantReconnectTimeoutSeconds;
    }

    public int getSocketConnectTimeoutSeconds() {
        return socketConnectTimeoutSeconds;
    }

    /**
     * Sets a timeout for establishing a connection to the broker.  A value of zero indicates an infinite timeout
     *
     * @param socketConnectTimeoutSeconds - the socket connection timeout in seconds to use
     */
    public void setSocketConnectTimeoutSeconds(int socketConnectTimeoutSeconds) {
        this.socketConnectTimeoutSeconds = socketConnectTimeoutSeconds;
    }

    public int getInitialConnectTimeoutInSeconds() {
        return initialConnectTimeoutInSeconds;
    }

    /**
     * Sets the initial connection timeout for fault tolerant connections.  Default is set to 30s
     * A value of zero indicates an indefinite timeout, and -1 indicates that client runtime will
     * attempt each URL (in your list) one at a time until a connection has been made.
     *
     * @param initialConnectTimeoutInSeconds - timeout for fault tolerant connection establishment
     */
    public void setInitialConnectTimeoutInSeconds(int initialConnectTimeoutInSeconds) {
        this.initialConnectTimeoutInSeconds = initialConnectTimeoutInSeconds;
    }

    public int getReconnectTimeoutInMinutes() {
        return reconnectTimeoutInMinutes;
    }

    /**
     * Sets how long a persistent client will attempt to reconnect in minutes.
     * If offline, persistent client sends are stored in the local client.
     *
     * @param reconnectTimeoutInMinutes
     */
    public void setReconnectTimeoutInMinutes(int reconnectTimeoutInMinutes) {
        this.reconnectTimeoutInMinutes = reconnectTimeoutInMinutes;
    }

    public int getMonitorIntervalInSeconds() {
        return monitorIntervalInSeconds;
    }

    /**
     * Sets the admin flow control monitoring interval.  A value of zero indicates that no monitoring will occur
     *
     * @param monitorIntervalInSeconds
     */
    public void setMonitorIntervalInSeconds(int monitorIntervalInSeconds) {
        this.monitorIntervalInSeconds = monitorIntervalInSeconds;
    }

    public int getPingIntervalInSeconds() {
        return pingIntervalInSeconds;
    }

    /**
     * Sets the interval between pings.  A value of zero will disable the ping
     *
     * @param pingIntervalInSeconds
     */
    public void setPingIntervalInSeconds(int pingIntervalInSeconds) {
        this.pingIntervalInSeconds = pingIntervalInSeconds;
    }

    /**
     * Sets the setSonicSubscriptionEventListener
     *
     * @param activeMQSubscriptionEventListener
     */
    public void setActiveMQSubscriptionEventListener(ActiveMQSubscriptionEventListener activeMQSubscriptionEventListener) {
        activeMQSubscriptionEventListener.setEventTransport(this);
        this.activeMQSubscriptionEventListener = activeMQSubscriptionEventListener;

    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
