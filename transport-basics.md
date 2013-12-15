---
layout: default
title: Transport Basics
---
Transport Basics
================

Cougar supports 3 paradigms for communications with a service interface (RPC, Push, Events), each of which are implemented by one or more transports, for each combination of transport and paradigm we get at least one protocol.

Regardless as to the transport in use there are some basics to working with transports which apply equally to all.

Binding Descriptors (in Service Registration)
---------------------------------------------

When registering a service, you specify the set of transport binding descriptors you want to be used. If a binding descriptor for a protocol is not in this set, then it is not eligible for binding this service interface:

```
<bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
  ...
  <property name="bindingDescriptors">
    <util:set>
      <bean class="com.betfair.baseline.v2.rescript.BaselineRescriptServiceBindingDescriptor"/>
      <bean class="com.betfair.baseline.v2.soap.BaselineSoapServiceBindingDescriptor"/>
      <bean class="com.betfair.baseline.v2.events.BaselineJMSServiceBindingDescriptor"/>
      <bean class="com.betfair.baseline.v2.jsonrpc.BaselineJsonRpcServiceBindingDescriptor"/>
      <bean class="com.betfair.baseline.v2.socket.BaselineSocketServiceBindingDescriptor"/>
    </util:set>
  </property>
  ...
</bean>
```

Protocol Binding
----------------

In addition to the binding descriptor, for some protocols (currently just HTTP based ones), it's necessary to register a protocol binding, the purpose of which is to provide additional required information into the transport:

```
<bean parent="cougar.transport.AbstractProtocolBinding" scope="singleton">
  <property name="contextRoot" value="www"/>                   <!-- default: "" -->
  <property name="identityTokenResolver" ref="tokenResolver"/> <!-- no default -->
  <property name="protocol" value="RESCRIPT"/>                 <!-- no default -->
  <property name="enabled" value="true"/>                      <!-- default: true -->
</bean>
```

The ```protocol``` and ```enabled``` properties are pretty self explanatory, the ```enabled``` property is exposed to enable the capability to enable/disable a binding using a spring property, thereby enabling library authors to give implementers choice on whether to expose on a particular transport.

The combination of the ```contextRoot``` and ```identityTokenResolver``` enable support for multiple methods of identity token resolution on a single protocol within a single Cougar instance. For example at Betfair, this is used to support both an API capability as well as website traffic.


Event Transport Binding (in Service Registration)
-------------------------------------------------

Event transports are a bit unusual, in that it is possible to have more than one instance of an event transport implementation configured within the same Cougar based service. Because of this, it is necessary to specify exactly which event transport(s) you wish to bind your service interface to (those you wish to emit on):

```
<bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
  ...
  <property name="eventTransports">
    <util:set>
      <ref bean="jmsTransport"/>
      <ref bean="jmsTransport2"/>
    </util:set>
  </property>
  ...
</bean>
```

**Note that this is required in addition to the binding descriptor above.**
