---
layout: default
---
# Implementing a Cougar Service Interface in Java

Cougar provides you with two styles of interface to implement.

Most users will want to use the synchronous interface, since it's straightforward.

The only known good reason to use the asynchronous interface is if your application implementation is also asynchronous
(enqueues jobs internally), and needs to pass callback objects around.

There are no performance or efficiency benefits to using the asynchronous interface.  Under the covers the synchronous
interface is invoked asynchronously anyway.  The asynchronous interface only exists to service a particular style of
application implementation.

# Synchronous Interface

When you build your module, your service's synchronous interface was generated in the `target/generated-sources`
directory of the `application` submodule, at `<package>.<interfaceName>Service`.

The Cougar archetype implemented this interface in your usual source root at `<package>.<interfaceName>ServiceImpl`.

You will see it implements `<package>.v<major>.<interfaceName>Service` interface, which exists in `target/generated-sources`.

Example method implementation:

    @Override
    public SimpleResponseObject getSimpleResponse(RequestContext ctx, String message) throws SimpleException {
        if (badness()) {
            throw new SimpleException(ResponseCode.InternalError, ExampleServiceExceptionErrorCodeEnum.UNEXPECTED_ERROR);
        }
        SimpleResponseObject response = new SimpleResponseObject();
        response.setMessage(message);
        return response;
    }

When you change your `idd` project and rebuild, your `application` sources will be regenerated, and you'll have
to change your implementation to fit the new interface.

## Asynchronous Interface

The asynchronous interface is generated in the `target/generated-sources` directory of the `application` submodule,
in package `<package>.<interfaceName>AsyncService`.

Example method implementation:

    @Override
    public void getSimpleResponse(RequestContext ctx, String message, ExecutionObserver observer) {
        if (badness()) {
            observer.onResult(
                new ExecutionResult(
                    new SimpleException(ResponseCode.InternalError, ExampleServiceExceptionErrorCodeEnum.UNEXPECTED_ERROR)
                )
            );
            return;
        }
        SimpleResponseObject response = new SimpleResponseObject();
        response.setMessage(message);
        observer.onResult(new ExecutionResult(response));
    }

In order to run against the Async interface, you'll need to do both of the following:

1. Implement the Async interface

Start by locating the async interface in the generated source and develop and write your implementation of it
`<package>.<interfaceName>AsyncService`.

2. Wire it all up

Create your async implementation bean:

    <bean id="asyncService" class="<package>.YourAsyncImplementation/>

Change the service introduction spring from something that looks like this:

    <bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
        <property name="resolver">
        <bean class="com.betfair.services.example.v1.ExampleSyncServiceExecutableResolver">
            <property name="service" ref="com.betfair.services.example.application.ExampleServiceImpl"/>
        </bean>
        </property>
        <property name="serviceDefinition">
            <bean class="com.betfair.services.example.v1.ExampleServiceDefinition"/>
        </property>
        <property name="service" ref="com.betfair.services.example.application.ExampleServiceImpl"/>
        <property name="eventTransport" ref="com.betfair.services.example.application.EventTransport"/>
        <property name="bindingDescriptors">
            <util:set>
                <!-- Declare the binding descriptors for the SOAP and RESCRIPT transports -->
                <bean class="com.betfair.services.example.v1.rescript.ExampleRescriptServiceBindingDescriptor"/>
                <bean class="com.betfair.services.example.v1.soap.ExampleSoapServiceBindingDescriptor"/>
                <bean class="com.betfair.services.example.v1.events.ExampleJMSServiceBindingDescriptor"/>
            </util:set>
            </property>
        <property name="eventExecutionContext" ref="com.betfair.services.example.application.EventExecutionContext"/>
    </bean>

To this:

    <bean class="com.betfair.cougar.core.impl.ev.ServiceRegistration">
        <property name="resolver">
            <bean class="com.betfair.services.example.v1. ExampleServiceExecutableResolver ">
                <property name="service" ref="com.betfair.services.example.application.MyAsyncInterfaceService"/>
            </bean>
        </property>
        <property name="serviceDefinition">
           <bean class="com.betfair.services.example.v1.ExampleServiceDefinition"/>
        </property>
        <property name="service" ref=" com.betfair.services.example.application.MyAsyncInterfaceService"/>
        <property name="eventTransport" ref="com.betfair.services.example.application.EventTransport"/>
        <property name="bindingDescriptors">
            <util:set>
                <!-- Declare the binding descriptors for the SOAP and RESCRIPT transports -->
                <bean class="com.betfair.services.example.v1.rescript.ExampleRescriptServiceBindingDescriptor"/>
                <bean class="com.betfair.services.example.v1.soap.ExampleSoapServiceBindingDescriptor"/>
                <bean class="com.betfair.services.example.v1.events.ExampleJMSServiceBindingDescriptor"/>
            </util:set>
        </property>
        <property name="eventExecutionContext" ref="com.betfair.services.example.application.EventExecutionContext"/>
    </bean>

Note the change to the ExecutableResolver impl - this is a generated class, and you need to switch from the synchronous
one, named `<serviceName>SyncServiceExecutableResolver` to the async one, named `<serviceName}ServiceExecutableResolver`,
and update the two service references to your new Async service implementation bean.