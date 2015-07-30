---
layout: default
---
Modules
=======

Cougar is a modular framework, requiring you to run a minimal core, but then allowing you to select which optional extras
you want to run on top. Discovery is done at runtime, by searching the classpath for module descriptors.

Cougar has 4 types of module, which are wired into the system in the order shown below:
1. Bootstrap - As the name implies, modules which change base behaviour before Cougar initialises. Currently only used
   for setting up encrypted configuration file support (see [configuration](configuration.html)). Descriptor: `cougar-bootstrap-spring.xml`
2. Core - Core Cougar module (singleton), required by all Cougar based applications. Descriptor: `cougar-core-spring.xml`
3. Framework - Optional Cougar modules, may have dependencies on other framework modules, encompasses third party integrations
   such as caching or tracing, as well as Cougar transports. Descriptor: `cougar-module-spring.xml`
4. Application - Applications/services running within the Cougar framework. Descriptor: `cougar-application-spring.xml`

Descriptors
===========

Descriptors are just Spring bean definition files, although by convention, ones provided by the Cougar project have the
same form:

    <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.springframework.org/schema/beans
                http://www.springframework.org/schema/beans/spring-beans.xsd">

        <bean class="com.betfair.cougar.util.configuration.PropertyConfigurer">
            <constructor-arg index="0" ref="cougar.core.EncryptorRegistry"/>
            <property name="defaultConfig" value="classpath:conf/cougar-modulename-defaults.properties"/>
            <property name="configOverride" value="overrides.properties"/>
            <property name="placeholderPrefix" value="$COUGAR-MODULENAME{"/>
        </bean>

        <import resource="classpath:conf/module-config-cougar-modulename.xml" />

    </beans>

Where `modulename` and `MODULENAME` are replaced the the name of the module. As you can see, this includes the standard
Cougar `PropertyConfigurer` to support encrypted property files and using per-module default files and placeholder prefixes.
See [configuration](configuration.html) for more details.
