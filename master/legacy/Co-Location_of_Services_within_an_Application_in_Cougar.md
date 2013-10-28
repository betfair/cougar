---
layout: default
---
# Co-locating multiple services within your Application

It is straightforward to run multiple services within your Cougar application. In fact, every App out of the box includes
the HealthCheck as well as your BSIDL defined service.

There are a few points that require special consideration:

### Protocol binding applicability

In your spring wiring for the HttpTransport, a set of ProtocolBindings are defined. Each binding contains:

* A URI prefix - say /www or /api, or /
* The protocol it applies to, one of RESCRIPT, SOAP, JSON_RPC
* An optional IdentityTokenResolver for any requests received on this URL

When the container starts up, all Protocol bindings, where the Protocol matches that of the BindingDescriptors listed in
your ServiceRegistration bean, will expose the service with the supplied prefix.

Consider the following set of Protocol Bindings:

```xml
<bean parent="cougar.transport.AbstractProtocolBinding">
  <property name="contextRoot" value=""/>
  <property name="identityTokenResolver" ref="SimpleRescriptIdentityTokenResolver"/>
  <property name="protocol" value="RESCRIPT"/>
</bean>
<bean parent="cougar.transport.AbstractProtocolBinding">
  <property name="contextRoot" value="www"/>
  <property name="identityTokenResolver">
    <bean class="com.betfair.cougar.baseline.security.AlternativeRescriptIdentityTokenResolver"/>
  </property>
  <property name="protocol" value="RESCRIPT"/>
</bean>
```

Against the following service definitions:

```xml
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
<bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
    <property name="resolver">
        <bean class="com.betfair.baseline.v1.AnotherSyncServiceExecutableResolver">
            <property name="service" ref="anotherService
        </bean>
    </property>
    <property name="serviceDefinition">
        <bean class="com.betfair.baseline.v1.AnotherServiceDefinition"/>
    </property>
    <property name="service" ref="anotherVersion"/>
    <property name="bindingDescriptors">
        <util:set>
            <bean class="com.betfair.another.v1.rescript.AnotherRescriptServiceBindingDescriptor"/>
        </util:set>
    </property>
</bean>
```

Will mean that each service will be exposed on both /www and / . So when you start Cougar up, you'll see the following endpoints:

```
/www/cougarBaseline/v2
/cougarBaseline/v2
/www/anotherService/v1
/anotherService/v1
```

### Running multiple versions of the same service simultaneously

Poses no particular problems, however you should be aware that only one major version of a service is permitted at a time.
This is to ensure that a request originating from a 1.3 client can be serviced by a 1.5 implementation without needing a
client upgrade.

### Running both a client and service for the same Service definition. 

Putting aside the question of why you'd need to do this (we need it specifically for building a distributed model that
needs to be symmetric at both ends - ERO needs prices from Australia and vice versa), it leads to a cougar problem - specifically
that you'll end up with the same operations keys (the unique string that defines an operation) for both client and service,
meaning that you cannot have both in your container simultaneously.

In order to resolve this problem, Cougar has a namespaces concept, which can be applied at ServiceRegistration time to
disambiguate them.

The following snippet shows the spring definition for defining and registering a namespaced client:

```xml
<bean name="syncClient" class="com.betfair.baseline.v2.BaselineSyncClientAdapter">
    <property name="asynchronousClient">
        <bean class="com.betfair.baseline.v2.BaselineClientImpl" parent="cougar.client.AbstractClient">
            <constructor-arg value="CLI"/>
        </bean>
    </property>
</bean>
<bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration">
    <property name="resolver">
        <bean class="com.betfair.baseline.v2.BaselineClientExecutableResolver" init-method="init">
            <property name="defaultOperationTransport">
                <bean parent="cougar.client.AbstractRescriptTransport">
                    <constructor-arg>
                        <bean class="com.betfair.baseline.v2.rescript.BaselineRescriptServiceBindingDescriptor"/>
                    </constructor-arg>
                    <property name="remoteAddress" value="$BASELINE{cougar.client.rescript.remoteaddress}"/>
                    <property name="exceptionFactory">
                        <bean class="com.betfair.baseline.v2.exception.BaselineExceptionFactory"/>
                    </property>
                </bean>
            </property>
        </bean>
    </property>
    <property name="serviceDefinition">
        <bean class="com.betfair.baseline.v2.BaselineServiceDefinition"/>
    </property>
    <property name="namespace" value="CLI"/>
</bean>
```

Note that both the client bean and the registration of that service both need to specify (the same) namespace.