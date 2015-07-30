---
layout: default
---
# Events and BSIDL

Cougar includes support for Event emission/consumption based upon any JMS offering, Cougar offers an ActiveMQ specific
implementation out of the box, so this page will talk about that. If you have an alternate JMS provider, you can sub-class
the abstract transport and then change the dependencies mentioned throughout this page as appropriate. Throughout this
page are a number of config options with the key `<providerType>` within, for ActiveMQ, this has the value "activemq".
This page describes the implementation in two sections:

* Event emission
* Event consumption

## Event emission

To emit an event, it must be described in both your BSIDL and extensions documents. An example event is shown below:

### BSIDL Event definition

    <event name="MatchedBet" since="2.0">
        <description>An event to store matched bet events</description>
        <parameter type="MatchedBetStruct" name="matchedBet" mandatory="true">
            <description>details of the bet</description>
        </parameter>
        <parameter type="MarketStruct" name="market" mandatory="true">
            <description>details of the market</description>
        </parameter>
    </event>

Note that it is only necessary to describe the object you wish to be emitted onto the message bus.

### BSIDL Extensions document

    <event name="MatchedBet">
        <parameter name="matchedBet">
            <extensions><style>body</style></extensions>
        </parameter>
        <parameter name="market">
            <extensions><style>body</style></extensions>
        </parameter>
    </event>

The only thing necessary is to describe the style of each parameter. Currently only body is supported, though if there
is enough interest then JMS header parameters could be supported, as well as parameterized subscriptions. Both are
awaiting use-cases/requests.


### Application Launch dependencies

The pom of your launcher project needs the following Cougar dependency (note that you won't specifically need to reference
any ActiveMQ jars; these dependencies are resolved by the activemq-transport project:

    <dependency>
      <groupId>com.betfair.cougar</groupId>
      <artifactId>activemq-transport</artifactId>
      <version>${cougar.version}</version>
      <scope>runtime</scope>
    </dependency>

### Spring Wiring

After modifying the BSIDL, and including the activemq-transport dependency, run a `mvn clean install` on your app. This
will generate a `<serviceName>JMSServiceBindingDescriptor` class, as well as one TO for each BSIDL defined event in a
separate events package as part of the generated source.

In the spring wiring for your application, you'll need to configure two separate components:

#### End point definition

    <bean id="eventTransport" parent="com.betfair.cougar.transport.activemq.AbstractActiveMQTransport" init-method="init" destroy-method="destroy">
        <property name="destinationUrl" value="$BASELINE{cougar.transport.activemq.url}"/>
        <property name="destinationType" value="Topic"/>
        <property name="username" value="$BASELINE{cougar.transport.activemq.username}"/>
        <property name="password" value="$BASELINE{cougar.transport.activemq.password}"/>
        <property name="destinationResolver">
            <bean class="com.betfair.cougar.transport.jms.EventNameDestinationResolver">
                <constructor-arg value="$BASELINE{activemq.destinationResolver.destinationBase}"/>
            </bean>
        </property>
        <property name="threadPoolSize" value="$BASELINE{cougar.transport.activemq.eventThreadPool.size}"/>
        <property name="transportIdentifier" value="myTransportIdentifier"/>
        <property name="monitorRegistry" ref="cougar.core.MonitorRegistry"/>
    </bean>

Examining each property in turn:

* **destinationUrl** \- This is a url for your activemq message brokers. It can contain a comma separated list of
addresses for redundancy purposes
* **destinationType** \- This is the type of destination, and can be one of Queue, Topic and Durable topic. If you're
using a durable topic, you must supply a unique identifier (so ActiveMQ can know who you are and where you're upto
following connection/reconnection)
* **username** \- Username of the destination
* **password** \- Passsword of the destination
* **destinationResolver** \- if you're using the recommended approach, a different destination will be created for every
unique message type on your transport - the EventNameDestinationResolver in the spring wiring snippet above will take care
of creating the appropriate destination name appended onto the supplied destination.
* **threadPoolSize** \- Non mandatory property (will default to 1 if not set). This property sets how many threads (each
containing a JMS session) will be created to service requests to emit events.
* **transportIdentifier** \- Non mandatory property (defaults to null). Allows you to distinguish between multiple instances
of event transports.
* **monitorRegistry** \- Non mandatory property (defaults to null). If set, the transport will register connection,
publisher, and subscriber monitors with this monitor registry.

There are other properties you may wish to set, but which are optional:

* **acknowledgementMode** - Defaults to _SINGLE_MESSAGE_ACKNOWLEDGE_; Can be one of:
 * _AUTO\_ACKNOWLEDGE_ — The session automatically acknowledges the client’s receipt of a message before the next call
 to receive (synchronous mode) or when the session MessageListener successfully returns (asynchronous mode). In the event
 of a failure, the last message might be redelivered.
 * _CLIENT\_ACKNOWLEDGE_ — An explicit acknowledge( ) on a message acknowledges the receipt of all messages that have
 been produced and consumed by the session that gives the acknowledgement. In the event of a failure, all unacknowledged
 messages might be redelivered.
 * _INDIVIDUAL\_ACKNOWLEDGE_ — An explicit acknowledge( ) on a message acknowledges only the current message and no
 preceding messages. In the event of a failure, all unacknowledged messages might be redelivered. This mode is an ActiveMQ
 extension to the JMS standard.
 * _DUPS\_OK\_ACKNOWLEDGE_ — The session “lazily” acknowledges the delivery of messages to consumers, possibly allowing
 multiple deliveries of messages after a system outage.

#### Wiring this into your application

In your application's spring, you'll already have a ServiceRegistration bean defined, which introduces your service into
the container. It will roughly look as follows:

    <bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
        <property name="resolver">
            <bean class="com.betfair.baseline.v2.BaselineSyncServiceExecutableResolver">
                <property name="service" ref="baselineAppService"/>
            </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.betfair.baseline.v2.BaselineServiceDefinition"/>
        </property>
        <property name="service" ref="baselineAppService"/>
        <property name="bindingDescriptors">
            <util:set>
                <bean class="com.betfair.baseline.v2.rescript.BaselineRescriptServiceBindingDescriptor"/>
            </util:set>
        </property>
    </bean>

To this, you need to add your JMSServiceBindingDescriptor to the bindingDescriptor set, and set the eventExecutionContext property with an execution context, which will leave it looking as follows:

    <bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
        <property name="resolver">
            <bean class="com.betfair.baseline.v2.BaselineSyncServiceExecutableResolver">
                <property name="service" ref="baselineAppService"/>
            </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.betfair.baseline.v2.BaselineServiceDefinition"/>
        </property>
        <property name="service" ref="baselineAppService"/>
        <property name="eventTransports">
            <util:set>
                <ref bean="eventTransport"/>
            </util:set>
        </property>
        <property name="bindingDescriptors">
            <util:set>
                <bean class="com.betfair.baseline.v2.rescript.BaselineRescriptServiceBindingDescriptor"/>
                <bean class="com.betfair.baseline.v2.events.BaselineJMSServiceBindingDescriptor"/>
            </util:set>
        </property>
        <property name="eventExecutionContext">
            <bean class="com.betfair.cougar.api.ExecutionContextImpl"/>
        </property>
    </bean>

Why does an execution context need to be defined for emitting events? This is the execution context that is used at the
time you subscribe to events (we'll discuss why you need to subscribe to events when you're emitting them). If you have
no particular requirements around this, then leave the default definition in place and forget about it.

##### Implementing your App to emit events

If you've compiled your app with events defined in your BSIDL, then you'll see that the application defining interface
now has additional method(s) for subscription to be implemented. You should think of this process as the Event transport
subscribing to your application - and that your app appears as a source to your Event transport.

The method to be implemented will look similar to the following:

    public void subscribeToMatchedBet(ExecutionContext ctx, Object[] args, ExecutionObserver executionObserver);

Providing your wiring is correct, this subscribe method will be called when your Event Transport enabled application boots up.

Hold onto the executionObserver handed to you by the execution venue. After app startup, you can emit an event by calling
onResult on this executionObserver as follows:

    try {
        matchedBetObserver.onResult(new ExecutionResult(matchedBet));
    } catch (Throwable ex) {
    	LOGGER.log(Level.SEVERE, "An exception occurred emitting the matched bet event:", ex);
    }

### Subscribing to Other Services' Events

The process for subscribing to events emitted by another Cougar service follows similar format to publication. The major
difference is that your subscribing Cougar application does not describe the same event in IDL, rather you specify the
dependency on the emitting application's BSIDL project in your client app. Also, as above, your application needs to
depend on the activemq-transport library (see above).

To the build section of your client application's pom, add the following plugin definition:

    <plugins>
        <plugin>
            <groupId>com.betfair.plugins</groupId>
            <artifactId>cougar-codegen-plugin</artifactId>
            <version>${cougar.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <services>
                    <service>
                        <serviceName>BaselineService</serviceName>
                    </service>
                </services>
                <iddAsResource>true</iddAsResource>
                <client>true</client>
            </configuration>
        </plugin>
    </plugins>

You need to add the IDD as a project dependency:

    <dependency>
        <!-- Use the maven coordinates of the emitting application's IDD project here -->
        <groupId>com.betfair.cougar</groupId>
        <artifactId>baseline-idd</artifactId>
        <version>2.0-SNAPSHOT</version>
    </dependency>

#### Spring configuration

You will need two components - the endpoint definition (which is defined in the same fashion as the emitter, detailed
above), and the wiring for your application.

    <bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration">
        <property name="resolver">
            <bean class="com.betfair.baseline.v2.BaselineClientExecutableResolver" init-method="init">
                <property name="defaultOperationTransport" ref="rescriptTransport"/>
                <property name="eventTransport" ref="eventTransport"/>
            </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.betfair.baseline.v2.BaselineServiceDefinition"/>
        </property>
    </bean>

All class references in this bean definition will be generated for you by the IDLTODS plugin. Ensure that you configure
in an appropriate `activemqTransport` into the eventTransport property of the resolver, and you're done.

#### Consuming Events

Providing you've correctly configured the execution venue client (which should already exist if you've created your
application from the archetype - but if not, see [Invoking Remote Cougar Services with the Cougar Client](Invoking_Remote_Cougar_Services_with_the_Cougar_Client.html)
, consuming events is straightforward. On your generated client call the appropriate subscribe method:

    public void subscribeToMatchedBet (ExecutionContext ctx, Object[] args, ExecutionObserver observer);

Note that the same method is exposed for both the Sync and aSync client.

In this context, the executionContext is not currently used.

If you're using a durable topic, then args\[0\] should be configured to have your subscriber identifier. ActiveMQ uses this
to keep track of who you are and which messages you've received.

You'll need to implement an observer - and you will be called back when one of the following occurs:

* ExecutionResult.ResultType.Subscription - a subscription is successfully established with the messaging infrastructure.
Hold onto this subscription object as you can use this to close your subscription.
* ExecutionResult.ResultType.Fault - this will happen when an exception occurs. Why would an exception occur when you're
listening to an event stream? A notifier is configured so that if your subscription fails, then your observer will be
notified this has occurred.
* ExecutionResult.ResultType.Success - this will be returning you events from the stream.

Use one observer per EventType you wish to listen for.

# Monitoring and Events over JMS

Cougar supports ping's being sent over message streams for the purposes of determining if a stream is healthy.
Monitoring is only enabled if you set the **monitorRegistry** property on your EventTransport bean.

## Connection Monitoring

Cougar provides a connection monitor which allows you to include the status of the connection to the JMS brokers in
your overall application health. You can indicate whether you expect always to be connected to a JMS Broker via the
`jmsTransport.connectionMonitor.permanentConnectionExpected` property (defaults to `true`). If so, then failure
to connect will result in the connection monitor having a status of FAIL, if false, then it will be WARN. You can also
limit the impact the connection monitor has on overall health via `jmsTransport.connectionMonitor.maxImpact`
(defaults to `FAIL`).

If monitoring is enabled the following JMX bean will be registered for each service definition the transport is bound to:

* `CoUGAR.<providerType>.transport.monitoring:type=connection,serviceName=<serviceName>,serviceVersion=<serviceVersion>\[,transportIdentifier=<transportIdentifier>\]`,
of type ConnectionMonitor.

Additionally, regardless as to whether monitoring is enabled, if your transport is configured within a Cougar container, the following bean will be registered for each service definition the transport is bound to:

* `CoUGAR.<providerType>.transport:type=connection,serviceName=<serviceName>,serviceVersion=<serviceVersion>\[,transportIdentifier=<transportIdentifier>\]`,
of type ManagedActiveMQConnection.

## Pings

If you're emitting events from your service because they're declared on your interface, Cougar enables the addition of
ping messages within your message stream, which downstream consumers can use to determine the health of the stream. If
they in turn are using Cougar, these ping messages are automatically stripped out before events are passed to the application
and used to drive monitoring.

### Ping Emission

By default, ping emission is enabled for all events declared by a service interface bound to a ActiveMQ event transport. If
there is an error in emitting ping messages, then the associated monitor for the event will be put into a `FAIL` state.
There are some properties which allow you to control this behaviour:

* `cougar.<providerType>.monitor.<eventName>.publisher.emitPing` (default `true`) - whether to emit a ping for this event.
* `cougar.<providerType>.monitor.<eventName>.publisher.pingPeriod` (default `10000`ms) - how often to emit a ping down this
stream (in ms).
* `cougar.<providerType>.monitor.<eventName>.publisher.maxImpactForPing` (default `FAIL`) - the maximum impact the associated
monitor should have on the overall health of your application.

The exported monitor will have the name `TopicPublisherPingMonitor-<destinationName>`, unless you have specified a
**transportIdentifier**, in which case it will be `TopicPublisherPingMonitor-<transportIdentifier>-<destinationName>`.

if your transport is configured within a Cougar container, the monitor will be exported in JMX under the following name:

* `CoUGAR.<providerType>.transport.monitoring:type=publisher,serviceName=<serviceName>,serviceVersion=<serviceVersion>,eventName=<eventName>,destination=<destination>\[,transportIdentifier=<transportIdentifier>\]`

### Ping Receipt

On the receiving side, non-receipt of ping messages can be tuned as to the effect on the associated monitor:

* `cougar.<providerType>.monitor.<eventName>.subscriber` (default `true`) - Whether to create a ping monitor - if not
required then ping messages are consumed and discarded on receipt.
* `cougar.<providerType>.monitor.<eventName>.subscriber.pingFailureTimeout` (default `60000`) - If a ping message is not
received in this time period then the associated monitor will be put into a FAIL state.
* `cougar.<providerType>.monitor.<eventName>.subscriber.pingWarningTimeout` (default `pingFailureTimeout/2`) - If a ping
message is not received in this time period then the associated monitor will be put into a WARN state.
* `cougar.<providerType>.monitor.<eventName>.subscriber.maxImpactForPing` (default `FAIL`) - the maximum impact the
associated monitor should have on the overall health of your application.

The exported monitor will have the name `TopicSubscriberPingMonitor-<destinationName>`, unless you have specified
a **transportIdentifier**, in which case it will be `TopicSubscriberPingMonitor-<transportIdentifier>-<destinationName>`.
If a subscriptionId has been specified (for example for a durable subscription), then the name will be appended with
`\[<subscriptionId>\]`.

if your transport is configured within a Cougar container, the monitor will be exported in JMX under the following name:

* `CoUGAR.<providerType>.transport.monitoring:type=subscriber,serviceName=<serviceName>,serviceVersion=<serviceVersion>,eventName=<eventName>,destination=<destination>\[,transportIdentifier=<transportIdentifier>\]`
