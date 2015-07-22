---
layout: default
---
Configuration
=============

Cougar configuration is performed via a custom spring property configurer specified in your module spring configuration:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean class="com.betfair.cougar.util.configuration.PropertyConfigurer">
        <constructor-arg index="0" ref="cougar.core.EncryptorRegistry"/>
        <property name="defaultConfig" value="classpath:conf/module-name-defaults.properties"/>
        <property name="configOverride" value="overrides.properties"/>
        <property name="placeholderPrefix" value="$MODULE_NAME{"/>
    </bean>

    <bean id="someBean" class="com.betfair.cougar.somepackage.SomeClass">
        <property name="someProperty" value="$MODULE_NAME{cougar.module.property}"/>
    </bean>
</beans>
```

The ```placeholderPrefix``` and ```defaultConfig``` property values should be unique to your module to prevent clashes,
you should avoid starting either of these with the string "cougar" as this is reserved for the framework.

To use properties you need to use the correct module prefix to ensure that the correct default properties file is used.
Property names should be qualified such that they are unique as all get put into a singleton Properties object after resolution.

Defaults
--------

Default values for properties are specified in the resource specified by property ```defaultConfig``` in the configurer. This
is generally expected to be a classpath resource that is provided by the module in question. This is a standard Java properties
file and may contain comments. Defaults are overridden using one of the 3 mechanisms described below.

Application overrides
---------------------

A Cougar application may provide deployment environment agnostic configuration values using a properties file named
```cougar-application.properties```. Cougar will search for this file in the root of the classpath, and so it's
generally recommended to package this up in a Jar as part of your Maven build. These settings may be overridden by the mechanisms below.


Runtime overrides
-----------------

Runtime overrides are specified in a file called ```overrides.properties```. By default this file is looked for in the classpath
 in location ```/conf/```. However, by setting the system property ```betfair.config.host```, this can be changed to any valid url,
 critically either a fully qualified directory, or a remote config server.

System properties
-----------------

Finally, any property may be overridden by specifying as a system property.

Encrypted properties
--------------------

Cougar also supports reading of encrypted properties via the Jasypt framework. Encrypted property values are specified using the
form ```ENC(encryptedPropertyValue)```.

Decryption is performed using an implementation of ```org.jasypt.encryption.StringEncryptor```, which can be registered using the
following Spring config snippet, which *MUST* be specified in a bootstrap spring configuration file (ie named ```cougar-spring-bootstrap.xml```)
to ensure it is loaded prior to any configuration being read. This means that the bootstrap file may not use any of the standard
Cougar configuration mechanisms:
```
    <bean id="strongEncryptor"
          class="org.jasypt.encryption.pbe.StandardPBEStringEncryptor">
        <property name="algorithm" value="PBEWithMD5AndDES"/>
        <property name="password" value="somePassword"/>
    </bean>

    <bean parent="cougar.core.EncryptorRegisterer" lazy-init="false">
        <constructor-arg ref="strongEncryptor"/>
    </bean>
```