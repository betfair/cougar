---
layout: default
---
# CougarStartingGate and GateListeners

{toc:minLevel=2}

## Overview

Cougar is able to notify listeners that it has started up. This is useful for services or components which should not start to interact with external entities until Cougar is up and running.

The notification happens once Cougar receives a Spring ```onApplicationEvent()``` event. This means that Cougar's "start up" mechanism is intended for components which can't or shouldn't be started up as part of Spring's context initialisation. Note that Cougar itself doesn't start accepting connections until all listeners have been notified.

The requirements are simply:

1. a component needs to be notified when Cougar has started up (typically it shouldn't "start" until Cougar has started)

2. a component may need to be notified only _after_ other components are notified (ie. it should be possible to specify dependencies)

3. it is desirable to be able to invoke an arbitrary method on a  POJO instead of the POJO having to implement a listener interface. (viz. Spring's ```depends-on``` bean attribute).

{info}
The most notable components using this mechanism are ```SpringAwareCacheFactory``` and dependent beans like ```TangosolLeaderElector``` and ```DeltaCacheManager```. These are not started as part of Spring context initialisation because of Coherence oddities. If we solved the Coherence problem these components could be started up inline, but the functionality remains useful.
{info}

{info}
It is worth drawing a distinction between "initialising" and "starting" components. Spring describes the ```init-method``` functionality as a way of doing a bit of work to set up the bean's state, after all properties have been set by Spring. You could see this as being different to how we often use the functionality to "start" a component (particularly components which fire off their own threads, or start interacting with external components).
{info}

## The easy way to register a listener

You should include a maven dependency on ```cougar-core-impl``` (no, that's not ideal...).

Say you have a bean named "myBean" with a method "startMe" that should be invoked after Cougar has started up. The wiring would be:

```xml
<bean parent="cougar.core.GateRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener">
            <property name="bean" ref="myBean"/>
            <property name="method" value="startMe"/>
        </bean>
    </constructor-arg>
</bean>
```

Note that the above bean is anonymous (and that the parent bean is a prototype).

If you use Spring's [p-namespace](http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#xml-config-shortcuts) (section 3.3.2.6.2 of the manual), this simplifies to:

```xml
<bean parent="cougar.core.GateRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener" p:bean-ref="myBean" p:method="startMe"/>
    </constructor-arg>
</bean>
```

If you wanted to register another bean which should only be started after ```myBean```, you would use a ```depends-on``` tag, with the two declarations being:

```xml
<bean id="myBeanRegisterer" parent="cougar.core.GateRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener" p:bean-ref="myBean" p:method="startMe"/>
    </constructor-arg>
</bean>
...
<bean parent="cougar.core.GateRegisterer" depends-on="myBeanRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener" p:bean-ref="yetAnotherBean" p:method="anInitMethod"/>
    </constructor-arg>
</bean>
```

Notes:
{info:icon=false}
1. yes, GateRegisterer is not a pretty name. Suggestions for improvements welcome.
2. GateRegisterer can take a list of Listeners.
{info}

## Priorities

GateRegisterer can have a priority, e.g.:

```
<bean parent="cougar.core.GateRegisterer" depends-on="myBeanRegisterer">
    <constructor-arg>
        <bean parent="cougar.core.GateListener" p:bean-ref="yetAnotherBean" p:method="anInitMethod" p:priority="1000"/>
    </constructor-arg>
</bean>
```

The higher the number, the higher priority (i.e. how soon after Cougar core startup will the method execute).

Some parts of Cougar use this prioritization mechanism to start themselves up, including for example introducing operations into the EV.  So usage of a GateRegisterer doesn't necessarily guarantee that _all_ of Cougar will be started and usable at that point.

If you require all of Cougar to have been started and to be usable (which is typical of application code) then you can use a ```p:priority``` of -1.

## Gory details

The above examples make use of pre-defined abstract beans for registering listeners. This section shows what's under the hood.

You register ```GateListener``` instances with a ```CougarStartingGate```. The only ```CougarStartingGate``` bean in Cougar's Spring context is ```cougar.core.RequestProcessor```. You could implement the ```GateListener``` interface and you could programmatically register yourself with the ```CougarStartingGate```, but the ```GateRegisterer``` and ```GateListenerAdapter``` classes allow you to keep things decoupled.

You could declare your own ```GateListener``` and ```GateRegisterer``` beans from scratch, but as with the example above, it's easier to use the abstract beans ```cougar.core.GateRegisterer``` and ```cougar.core.GateListener```, which are defined in config file ```conf/cougar-core-spring.xml``` in project ```cougar-core-impl```. They take care of the basics (specifying the starting gate, and marking the beans as prototypes to keep them from polluting the context).

The ```GateRegisterer``` (```com.betfair.cougar.core.api.GateRegisterer```) simply has a constructor with signature

```java
public GateRegisterer(CougarStartingGate gate, GateListener... listeners) { 
    // validate and register listeners with gate
}
```

The class is stateless.

The ```GateListenerAdapter``` is a convenience implementation of ```GateListener```, intended to mimic and provide the decoupling benefits of Spring's ```init-method``` functionality. You specify two properties: "bean" - the object to start, and "method" - the name of the method to be invoked. The given method is invoked on the given bean when the listener is called.

Using ```GateListenerAdapter``` means your client code doesn't need to implement the ```GateListener``` interface, reducing your component's dependency on Cougar.


