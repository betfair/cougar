---
layout: default
---
{:toc}
# Cougar Client

# Choosing between the sync and async client

Generally we recommend only to use the async client interface with an async client implementation and that when running
these inside a Cougar service you should generally only use them when implementing the async service interface, otherwise
you will find yourself consuming too many threads and increasing the risk of dead- or live-lock. Similarly, we recommend
using the sync client interface with a sync client implementation (if available) and within a sync service implementation
if running on a server.

# From within a Cougar Application

This will allow you to use EV-local (that is to say application-co-located) Cougar services in the proper manner.

_Obviously you will need to change the package name, version, and interface name parts of the class names._

## Creating the client(s)

### In Spring

```xml
    <!-- Asynchronous client interface -->
    <bean name="asyncClient" class="com.betfair.services.example.v1.ExampleClientImpl" parent="cougar.client.AbstractClient"/>
    ...
    <!-- Synchronous client interface -->
    <bean name="syncClient" class="com.betfair.baseline.v2.BaselineSyncClientImpl">
        <constructor-arg ref="cougar.core.ExecutionVenue"/>
        <constructor-arg value="trousers" />
    </bean>
```

Inject the style of client you want into the bean that will use it.  You can create these classes in Java code instead of
Spring, but you will need to get hold of the bean ```cougar.core.ExecutionVenue``` from the Spring context anyway,
for the construction of the asynchronous client.

## Using the synchronous client interface

```java
        // The execution context
        ExecutionContext context = ...
        // Will throw something if (a) interrupted, (b) timed out, (c) cougar or application exception raised
        client.echoMessage(context, "Hello, World", TIMEOUT_MILLIS); // The timeout is optional
```

## Using the asynchronous client interface

```java
        // The ExecutionContext
        ExecutionContext context = ...
        // The ExecutionObserver (the result, which can be a value or an exception, will be passed to this)
        ExecutionObserver observer = ...
        // Doesn't throw anything - result will be returned to the observer
        asyncClient.echoMessage(context, "Hello, World", observer);
```

# From a non-Cougar Application

## Maven Configuration

### cougar-codegen-plugin

add the cougar-codegen-plugin to the project pom.&nbsp; Ensure the _client_ property is set to _true_

```
			<plugin>
				<groupId>com.betfair.cougar</groupId>
				<artifactId>cougar-codegen-plugin</artifactId>
				<version>${cougar.version}
				</version>
				<executions>
					<execution>
						<goals>
							<goal>process</goal>
						</goals>
						<configuration>
							<services>
								<service>
									<serviceName>SomeService</serviceName>
								</service>
							</services>
							<iddAsResource>true</iddAsResource>
							<client>true</client>
						</configuration>
					</execution>
				</executions>
			</plugin>
```

### Required dependencies

```
	<dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-standalone-ev</artifactId>
            <version>${cougar.version}</version>
        </dependency>
        <dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-marshalling-impl</artifactId>
            <version>${cougar.version}</version>
        </dependency>
	<dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-api</artifactId>
            <version>${cougar.version}</version>
        </dependency>
        <dependency>
            <groupId>com.betfair.cougar</groupId>
            <artifactId>cougar-util</artifactId>
            <version>${cougar.version}</version>
        </dependency>
```


## Application configuration

Cougar is configured using spring.&nbsp; Creation of the proxy instance and registering the client with cougar should be in a spring file called cougar-application-spring.xml in a directory called 'conf'.&nbsp;


### Create proxy instance

The step above will have generated a class to act as a proxy to the service.&nbsp; Create an instance of the proxy (bean cougar.core.ExecutionVenue supplied by cougar-standalone-ev)

```xml
    <bean name="eroClient" class="com.yourdomain.someservice.v1.SomeServiceSyncClientAdapter">
        <property name="asynchronousClient">
            <!-- Asynchronous client -->
            <bean class="com.yourdomain.someservice.v1.SomeServiceClientImpl">
                <constructor-arg ref="cougar.core.ExecutionVenue"/>
            </bean>
        </property>
    </bean>
```

### Register client with cougar

#### Sync rescript client (over HTTP)

Classes com.betfair.sportex.exchange.readonly.\* are generated classes and should be substituted with the appropriate generated classes for your service

The last pair of properties on the defaultOperationTransport are to do with identity tokens; the first is tasked with marshalling and unmarshalling an identity token to/from the request/response, and it is the responsibility of the second to turn the identity token(s) into a populated identity object. If your Cougar Service is not using security, this is not necessary.

```xml
    <!-- Register the client with Cougar -->
    <bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration" id="eroRegistration">
        <property name="resolver">
            <bean class="com.yourdomain.someservice.v1.SomeServiceClientExecutableResolver" init-method="init">
                <property name="defaultOperationTransport">
                    <!--Transport-->
                    <bean parent="cougar.client.AbstractRescriptTransport">
                        <constructor-arg>
                            <bean class="com.yourdomain.someservice.v1.rescript.SomeServiceRescriptServiceBindingDescriptor"/>
                        </constructor-arg>
                        <property name="remoteAddress" value="$API_FRAMEWORK{cougar.ero.rescript.remoteaddress}"/>
                        <property name="exceptionFactory">
                           <bean class="com.yourdomain.someservice.v1.exception.SomeServiceExceptionFactory"/>
                        </property>
                        <property name="identityTokenResolver">
                           <bean class="com.betfair.cougar.baseline.security.BaselineAsyncClientIdentityTokenResolver"/>
                        </property>
                        <property name="identityResolver">
                           <bean class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityResolver" />
                        </property>
                    </bean>
                </property>
            </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.yourdomain.someservice.v1.SomeServiceServiceDefinition"/>
        </property>
    </bean>
```

#### Async rescript client (over HTTP)

```
    <!-- Register the client with Cougar -->
    <bean class="com.betfair.cougar.core.impl.ev.ClientServiceRegistration" id="baselineRegistration">
        <property name="resolver">
            <bean class="com.betfair.baseline.v2.BaselineClientExecutableResolver" init-method="init">
                <property name="defaultOperationTransport">
                    <!--Transport-->
                    <bean parent="cougar.client.AbstractAsyncRescriptTransport">
                        <constructor-arg>
                            <bean class="com.betfair.baseline.v2.rescript.BaselineRescriptServiceBindingDescriptor"/>
                        </constructor-arg>
                        <property name="remoteAddress" value="$APPLICATION{cougar.client.rescript.remoteaddress}"/>
                        <property name="exceptionFactory">
                            <bean class="com.betfair.baseline.v2.exception.BaselineExceptionFactory"/>
                        </property>
                        <property name="identityTokenResolver">
                            <bean class="com.betfair.cougar.baseline.security.BaselineAsyncClientIdentityTokenResolver"/>
                        </property>
                        <property name="identityResolver">
                           <bean class="com.betfair.cougar.component.cougar.client.identity.resolver.PlatformClientIdentityResolver" />
                        </property>
                    </bean>
                </property>
            </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.betfair.baseline.v2.BaselineServiceDefinition"/>
        </property>
    </bean>
```

### Configuration

There are some properties you must specify in your child bean definition:

* remoteAddress
* exceptionFactory
* identityTokenResolver
* identityResolver

Additionally there are extra properties you may choose to define in your own child bean definition:

* client
* httpMethodFactory
* transportSSLEnabled
* httpsKeyPassword
* httpsKeystore
* httpsKeystoreType - Defaults to JVM's default KeyStore type (normally JKS).
* httpsTrustPassword
* httpsTruststore
* httpsTruststoreType - Defaults to JVM's default KeyStore type (normally JKS).
* httpTimeout - Sets socket and connection timeout (in milliseconds).
* hostnameVerificationDisabled - Disables certificate hostname checks (defaults to false).
* hardFailEnumDeserialisation - Whether to reject unrecognized enum values - coming from a validValues element in the IDD (defaults to false)

## Start Cougar

the easiest way to start cougar is to use CougarSpringContextFactoryImpl.&nbsp; This context factory searches the classpath for cougar applications, cougar modules and the cougar core spring configurations.

```
        CougarSpringCtxFactoryImpl cougarSpringCtxFactory = new CougarSpringCtxFactoryImpl();
        cougarSpringCtxFactory.create();
```
