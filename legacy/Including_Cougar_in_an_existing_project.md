---
layout: default
---
# Including Cougar in an existing project

If you want to integrate Cougar into an existing application either by providing a service or by consuming one as a client,
you will need to follow the steps outlined herein in order to integrate Cougar into your code.

If you are adding a Cougar server you will need to [describe the service in BSIDL](Defining_Your_Service_in_BSIDL_for_Cougar.html),
generate the server side stubs via idlToDs, and [code up](Implementing_a_Cougar_Service_Interface_in_Java.html) the server
side implementations of each service call.

If you are creating a Cougar client you will need to bring the cougar dependencies into your project, define the Cougar
client in Spring, inject the client into
your code and then implement  code to use the provided client.

# Client side

###  Spring configuration

In the Spring configuration for your Cougar client, you must inject a client implementation, the underlying transport
mechanism (rest, binary, etc.), the service to be called and the resolvers to be used in passing various identity information
items between client and server.

Cougar offers both Asynchronous and Synchronous client implementations. The simplest use case from a client point of view
is to call a synchronous service as if it were a local method call.

#### Client

The Cougar synchronous client is a wrapper around the asynchronous client, behind the scenes it makes asynchronous calls
and blocks until it receives a response or until a specified timeout is reached. Figure Fig null shows an example Spring
configuration defining a synchronous client.

```
<!-- Synchronous client -->
<bean name="exampleClient" class="com.betfair.example.v2.ExampleAdapter" depends-on="exampleRegistration">
    <property name="asynchronousClient">
        <!-- Asynchronous client -->
        <bean class="com.betfair.example.v2.ExampleClientImpl">
            <constructor-arg ref="cougar.core.ExecutionVenue"/>
        </bean>
    </property>
</bean>
```

#### Execution venue

The local execution venue must be passed to the client as a parameter since it is here that the defined operations are
registered for use.

#### Namespace

Cougar uses the concept of a namespace to disambiguate method calls in cases where the method must be called on a specific
server instance.

#### Underlying transport

A transport is the mechanism by which requests, responses and events flow in and out of Cougar services.The transport
includes all the required details of the transport mechanism to be used, the precise details required will depend upon
the transport mechanism selected. In this case we are using a rescript based transport

```
<!-- Underlying transport -->
<bean parent="abstractRescriptTransport" id="exampleClientTransport">
    <constructor-arg>
        <bean class="com.betfair.example.v2.rescript.ExampleRescriptServiceBindingDescriptor"/>
    </constructor-arg>
    <property name="remoteAddress" value="http://$EXAMPLE_SERVICE{example.hostname}/api"/>
    <property name="exceptionFactory">
        <bean class="com.betfair.example.v2.exception.ExampleExceptionFactory"/>
    </property>
    <property name="identityTokenResolver" ref="pcidentityTokenResolver"/>
    <property name="identityResolver" ref="pcidentityResolver"/>
</bean>
...
<bean id="pcidentityResolver" class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityResolver" />
<bean id="pcidentityTokenResolver" class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityTokenResolver" />
```

The _identityTokenResolver_ and _identityResolver_ are required in order to define how a client identity is resolved and
how it is communicated over the chosen transport. In general you should use the provided resolvers as these provide
behaviour which will match the majority of use cases.

#### Registration

The Client must be registered with a local cougar instance

```
<!-- Register the client with local Cougar -->
<bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration" id="exampleRegistration">
    <property name="resolver">
        <bean class="com.betfair.example.v2.ExampleClientExecutableResolver" init-method="init">
            <property name="defaultOperationTransport" ref="exampleClientTransport"/>
        </bean>
    </property>
    <property name="serviceDefinition">
        <bean class="com.betfair.example.v2.ExampleServiceDefinition"/>
    </property>
</bean>
```

{note:title=Why}Why is this? Some attempt a explanation could be edifying here.{note}

#### Gate registerer

As noted [here](Waiting_for_Cougar_to_Start.html), it is sometimes necessary to wait until Cougar has completed startup
before starting some aspects of your application.

If your Cougar client immediately starts making calls on startup, it is possible that the execution venue is not ready to
handle the first few requests (essentially this is a timing issue). To work around this, use a gate listener to start the
cougar client process _after_ startup has completed.

```
<!-- this starts the eroModelManager AFTER cougar has been initialised -->
<bean id="cacheFactoryRegisterer" parent="cougar.core.GateRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener" p:bean-ref="eroModelManager" p:method="start" p:priority="-20"/>
    </constructor-arg>
</bean>
```

### Client code

You can now inject the 'exampleClient' into your application for use. Using the synchronous Cougar client is almost as
simple as making a local method call, the key differences being that the call _might_ timeout and that you are required
to provide a 'context' to the client.

```
import com.betfair.cougar.api*;
import com.betfair.cougar.core.*;
import com.betfair.cougar.logging.*;
import com.betfair.cougar.component.cougar.client.helper.ExecutionContextHelper;
import com.betfair.cougar.support.util.NetUtil;
import com.betfair.sports.example.v2.ExampleSyncClientAdapter;
import com.betfair.sports.example.v2.to.Thingy;
```

```
private int timeout = 30000;//30 seconds
private String applicationKey;//usually defined in properties file and injected via Spring
private static final CougarLogger logger = CougarLoggingUtils.getLogger(RemoteEROModelBuilder.class);
```

```
public Thingy getThingy() throws Exception {
    Thingy thingy=null;
    try {
        //Get Thingy from client
        ExecutionContext context = ExecutionContextHelper.createContext(applicationKey, NetUtil.getLocalIPAddress());
        Thingy thingy = client.getThingy(context, timeout);
        return thingy;
    } catch (CougarServiceException e) {
        logger.log(Level.WARNING, "Input/Output exception (%s) connecting to cougar2 %s %s", e, e.getMessage(), e.getResponseCode(), e.getServerFaultCode(), e.getFault());
        throw e;
    }
    return thingy;
}
```

The developer must create an ExecutionContext, and for this reason an ExecutionContextHelper is provided to assist in
creating one. In this example an application key and local IP address (as a String) must be provided.

#### Ip Address

The utility method NetUtil.getLocalIPAddress() will _attempt_ to obtain the correct local IP address but is not guaranteed
to succeed, for this reason it is recommended that if you rely on this method you should call it at startup in order to
'fail fast'. You should also note that this method will always return the IP address it found on startup, even if the Ip
address of the machine subsequently changes.

#### Application key

Each version of a deployed application is assigned an application key as a 'shared secret' which the calling client must
provide when accessing the service.

```
<!--================================ Cougar Client=======================-->
```

```
<!-- Synchronous client -->
<bean name="exampleClient" class="com.betfair.example.v2.ExampleAdapter" depends-on="exampleRegistration">
    <property name="asynchronousClient">
        <!-- Asynchronous client -->
        <bean class="com.betfair.example.v2.ExampleClientImpl">
            <constructor-arg ref="cougar.core.ExecutionVenue"/>
        </bean>
    </property>
</bean>
```

```
<!-- Underlying transport -->
<bean parent="abstractRescriptTransport" id="exampleClientTransport">
    <constructor-arg>
        <bean class="com.betfair.example.v2.rescript.ExampleRescriptServiceBindingDescriptor"/>
    </constructor-arg>
    <property name="remoteAddress" value="http://$EXAMPLE_SERVICE{example.hostname}/api"/>
    <property name="exceptionFactory">
        <bean class="com.betfair.example.v2.exception.ExampleExceptionFactory"/>
    </property>
    <property name="identityTokenResolver" ref="pcidentityTokenResolver"/>
    <property name="identityResolver" ref="pcidentityResolver"/>
</bean>
...
<bean id="pcidentityResolver" class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityResolver" />
<bean id="pcidentityTokenResolver" class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityTokenResolver" />
```

```
<!-- Register the client with local Cougar -->
<bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration" id="exampleRegistration">
    <property name="resolver">
        <bean class="com.betfair.example.v2.ExampleClientExecutableResolver" init-method="init">
            <property name="defaultOperationTransport" ref="exampleClientTransport"/>
        </bean>
    </property>
    <property name="serviceDefinition">
        <bean class="com.betfair.example.v2.ExampleServiceDefinition"/>
    </property>
</bean>
```

## Pom configuration

The Cougar modules will obviously need to be included in a project before they can be used.
The relevant modules are:

* cougar-client
* cougar-marshalling-impl
* cougar-codegen-plugin

```
        <dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-client</artifactId>
            <version>${cougar.version}</version>
        </dependency>
        <dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-marshalling-impl</artifactId>
            <version>${cougar.version}</version>
        </dependency>
```

## generating code from IDL

```
<build>
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
                    <configuration>
                        <services>
                            <service>
                                <serviceName>MyService</serviceName>
                            </service>
                        </services>
                        <iddAsResource>true</iddAsResource>
                        <client>true</client>
                        <server>true</server>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
```
