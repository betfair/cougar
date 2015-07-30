---
layout: default
---
# Service Health Status Exposure

## Setting up Monitors

Internally, Cougar uses [Tornjak](http://github.com/betfair/tornjak), please see this [guide](http://betfair.github.io/tornjak/master/legacy/Monitor_Usage_Spring.html)
for details of how to configure that framework, paying particular attention to the [active monitoring setup](http://betfair.github.io/tornjak/master/legacy/Active_Monitoring.html)
if you plan to use the resulting status to drive a load balancer. To expose the status of your application to the Cougar
data services container, you are required to expose an instance of the `MonitorRegistry` interface from your `Service`.
This should have been exemplified in the archetype you used as a basis for your service (look at your `cougar-application-spring.xml`), you just need to tailor it to your needs.

## Including the Cougar Health Service

Your service's status information will then be available to you if you use the Cougar Health Service module (included by default in the archetype).

    <dependency>
        <groupId>com.betfair.cougar</groupId>
        <artifactId>cougar-health-service-app</artifactId>
        <version>${cougar.version}</version>
        <scope>runtime</scope>
    </dependency>

## Cougar Health Service URLs

Port is usually 8080 unless over-ridden by `jetty.http.port`.

<table>
<tr>
<th>What</th><th>Where</th><th>Notes</th></tr>
<tr>
<td>Health check summary (RESCRIPT)</td>
<td>http://localhost:8080/healthcheck/v2/summary</td>
<td>Use this for webping style checks</td>
</tr>
<tr>
<td>Health check detail (RESCRIPT)</td>
<td>http://localhost:8080/healthcheck/v2/detailed</td>
<td>-</td>
</tr>
<tr>
<td>Health check WSDL</td>
<td>http://localhost:8080/wsdl/CougarHealthService.wsdl</td>
<td>-</td>
</tr>
<tr>
<td>Health check SOAP Endpoint</td>
<td>http://localhost:8080/HealthService/v3</td>
<td>-</td>
</tr>
</table>

# Cougar Container Monitoring Services

The cougar data services container has a secondary HTTP port, independent of the deployed transport that allows remote monitoring of the cougar application.

It has 4 distinct interfaces, available on different urls, although this number may increase or decrease depending on the modules installed.

The port is usually 9999 unless over-ridden by `jmx.html.port`.


<table style='background-color: #FFFFCE;'>
     <tr>
       <td valign='top'><img src='warning.gif' width='16' height='16' align='absmiddle' alt='' border='0'></td>
       <td><p>The protocol for the services on port 9999 is usually HTTP when the application is run up in dev mode (e.g. via `exec:java`), and HTTPS everywhere else.
       In this document HTTP is used, simply replace with HTTPS where necessary.</p></td>
      </tr>
</table>


The port is protected by HTTP basic auth.  The username is always `jmxadmin`.  The default password (usually used in dev) is `password`.

The username and password are governed by the `jmx.html.username` and `jmx.html.password` properties.

## SSL

By default, Cougar runs this monitoring interface with SSL enabled. This is strongly recommended for production. Unfortunately,
it's not possible for us to distribute certificates, so you need to specify 3 properties to setup SSL properly:
`jmx.html.keystore.filepath`, `jmx.html.keystore.password` and `jmx.html.keystore.certpassword`. If you want to
disable SSL for use in a development environment, then use `jmx.html.tls.enabled`, setting it to `false`.

## JMX HTML Bean Browser

The simplest interface is to browse all exported JMX beans in the system as web pages. Using this interface, the operator
both interrogate and interact with beans (i.e. operations can be called to change the state of the application, should
they be exposed). This interface may be reached at: https://localhost:9999/ (or http://localhost:9999/ if you have SSL disabled)

## Thread interrogator.

This interface is used to interrogate the state of the JVM threads. The page reports deadlocks and blocked threads, as
well as giving an overview of all threads in the system and their current state. It is reached at
https://localhost:9999/administration/threaddump.jsp (or http://localhost:9999/administration/threaddump.jsp if you have SSL disabled)

## JMX Bean API

### Parameters

This is an API (if we're going to be **really** generous) that allows beans and attributes to be returned in an easy-to-parse format,
either individually or in batches. It is accessed at `https://localhost:9999/administration/batchquery.jsp`
(or `http://localhost:9999/administration/batchquery.jsp` if you have SSL disabled) and takes the following attributes:

* **on** The object name of the bean being interrogated.
 * This may be a [wildcard](http://download.oracle.com/javase/6/docs/api/javax/management/ObjectName.html) name matching multiple beans.
 * It may also be the word _SYSTEM_ if system properties are to be returned.
* **an** The name of the attribute the be returned.
 * If not specified all attributes of the matching beans are returned.
 * In the case of system properties being returned, this is the name of the property required, or may be omitted for all properties.
* **sep** the character separator used in the response. By default it is a tilde (~), but if this character might be returned
in the string, a different value may be specified as no escaping is performed if this character is found in any values to be returned.
* **t** if present, this attribute forces the current system time to be provided in the response.

### Response format

All responses to the batchquery.jsp are returned in the following format (assuming the separator character is a tilde):

(Time)(BeanName)(Attribute)(Attribute)|(BeanName)(Attribute)(Attribute)

where

* (TimeField) is there only if requested in the parameters. It's format is: Time~2009-10-29 14:39:52.800~
* (BeanName) is the name of the bean. It's format is: java.lang:type=Runtime
* (Attribute) is an attribute. It's format is: ~Name~Biff

### Example calls

All these examples should work on the Baseline Service. Other deployed services may need some tweaking.

#### Return all the attributes of a single bean

All the attributes of a bean are listed by not specifying an attribute ("an") parameter.

* `https://localhost:9002/administration/batchquery.jsp?on=java.lang%3Atype=Runtime`

The skeleton of the return value is shown below. It has been formatted for ease of reading.

    java.lang:type=Runtime
        ~Name~2344@HOSTNAME
        ~ClassPath~...
        ~VmName~Java HotSpot(TM) Server VM
        ~VmVendor~Sun Microsystems Inc.
        ~VmVersion~14.0-b16
        ~BootClassPathSupported~true
        ...

#### Return the a single attribute of a bean and the current time

* `https://localhost:9999/administration/batchquery.jsp?on=java.lang%3Atype=Runtime&t&an=VmVersion`

The formatted return value is shown below.

    Time~2009-10-29 14:51:28.932~
    java.lang:type=Runtime
        ~VmVersion~14.0-b16

#### Return a System property

* `https://localhost:9999/administration/batchquery.jsp?on=SYSTEM&an=java.runtime.name`

The skeleton formatted return value is shown below.

    System Properties
        ~java.runtime.name~Java(TM) SE Runtime Environment

#### Return the same property from many beans

* `https://localhost:9999/administration/batchquery.jsp?on=CoUGAR.service.Baseline.v1.0:type=operation,name=**&an=Calls`

The formatted return value is shown below.

    CoUGAR.service.Baseline.v1.0:type=operation,name=testException
      ~Calls~67
    |
    CoUGAR.service.Baseline.v1.0:type=operation,name=testStringableLists
      ~Calls~0
    |
    CoUGAR.service.Baseline.v1.0:type=operation,name=testLargePost
      ~Calls~275
    ...

#### Return all properties of many beans

As above, but don't specify an attribute ("an") parameter.

* `https://localhost:9999/administration/batchquery.jsp?on=CoUGAR.service.Baseline.v1.0:type=operation,name=**`

#### Run an mBean operation

The operation must accept no parameters.

* `https://localhost:9999/administration/batchquery.jsp?on=CoUGAR:name=Logging&op=getNumErrors`

the Formatted return value is in the form (BeanName)(OperationName)(Result)

    CoUGAR:name=Logging~getNumErrors~0~

# Adding your own beans

You can add your own MBeans by using the `javax.management` API, either in Java code, or in Spring.  Here's an example in Spring using annotations.

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:aop="http://www.springframework.org/schema/aop"
        xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
            http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd">
        <bean id="myBean" class="com.betfair.services.example.EchoBean"/>
        <bean id="jmxAttributeSource"
            class="org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource" />
        <bean class="org.springframework.jmx.export.MBeanExporter" lazy-init="false">
        <property name="server">
            <bean class="org.springframework.jmx.support.MBeanServerFactoryBean">
                <property name="locateExistingServerIfPossible" value="true"/>
            </bean>
            </property>
        <property name="assembler">
                <bean class="org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler">
            <property name="attributeSource" ref="jmxAttributeSource" />
            </bean>
            </property>
        <property name="namingStrategy">
                <bean class="org.springframework.jmx.export.naming.KeyNamingStrategy"/>
            </property>
        <property name="autodetect" value="false" />
        <property name="registrationBehaviorName" value="REGISTRATION_FAIL_ON_EXISTING"/>
        <property name="beans">
                <map>
                    <entry key="Foo.Bar:wizz=bang" value-ref="myBean"/>
                </map>
        </property>
        </bean>
    </beans>

Here's an example of what `myBean` could look like, using annotations.

    import org.springframework.jmx.export.annotation.*;
    @ManagedResource(description="My Managed Bean")
    public class EchoBean {
        private String prefix;
        @ManagedAttribute(description="Get the prefix")
        public String getPrefix() {
            return prefix;
        }
        @ManagedAttribute(description="Set the prefix")
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        @ManagedOperation(description="Echoes the argument, prepending the prefix")
        @ManagedOperationParameters({
            @ManagedOperationParameter(name = "argument", description = "The argument")
        })
        public String echo(String arg) {
            return prefix <u> ": " </u> arg;
        }
        public void dontExposeMe() {
            //
        }
    }

There are many possible ways of [adding MBeans in Spring](http://static.springsource.org/spring/docs/2.0.x/reference/jmx.html)
or in [plain old Java](http://download.oracle.com/javase/6/docs/technotes/guides/jmx/index.html), this is just one of them
exposed as an example.  The key thing is to use the platform MBean server.

It's unwise to set `autodetect` to true, as this can expose some classes in other modules (Cougar core or otherwise)
that weren't supposed to be.

# Other

## Relevant source packages

    com.betfair.cougar.core.api.jmx
    com.betfair.cougar.core.impl.jmx