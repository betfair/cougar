---
layout: default
---
Getting started with Cougar
===========================

This guide is intended as a 5 minute guide to getting a service up and running with Cougar. Cougar has a rich set of
features which we couldn't hope to cover in such a manner, so for other guides and references, please check out our
[documentation hub](documentation.html).

We're going to create an initial Cougar based project, define and implement an operation on a service interface, run
it and call it.

Basic project structure
-----------------------

Creating the basic project structure and config can be a little tedious, so we're going to use on of the provided cougar
archetypes to generate the basic layout:

    C:\>mvn archetype:generate -DarchetypeGroupId=com.betfair.cougar.archetypes \
            -DarchetypeRepository=https://oss.sonatype.org/content/repositories/snapshots \
            -DarchetypeArtifactId=simple-service-archetype -DarchetypeVersion=3.2-SNAPSHOT
    [INFO] Scanning for projects...
    [INFO]
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Stub Project (No POM) 1
    [INFO] ------------------------------------------------------------------------
    [INFO]
    [INFO] >>> maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom >>>
    [INFO]
    [INFO] <<< maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom <<<
    [INFO]
    [INFO] --- maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom ---
    [INFO] Generating project in Interactive mode
    [INFO] Archetype repository missing. Using the one from [com.betfair.cougar.archetypes:simple-service-archetype:3.0-SNAPSHOT] found in catalog local
    Define value for property 'groupId': : wibble
    Define value for property 'artifactId': : wibble
    Define value for property 'version':  1.0-SNAPSHOT: :
    Define value for property 'package':  wibble: :
    Confirm properties configuration:
    groupId: wibble
    artifactId: wibble
    version: 1.0-SNAPSHOT
    package: wibble
     Y: : Y


Enter the values shown when prompted.

Now a little pom hackery is required to remove the vestiges of our archetype project, open up the ```pom.xml``` in the
directory just created (`wibble`) and remove these lines from the top:

    <parent>
        <groupId>com.betfair.cougar.archetypes</groupId>
        <artifactId>cougar-archetypes</artifactId>
        <version>3.0-SNAPSHOT</version>
    </parent>

Finally, we need to run a little maven magic to get all the files with the right names/content:

    C:\>cd wibble
    C:\wibble>mvn -Prename -Dupper=Wibble -Dlower=wibble install -N


Your first service interface
----------------------------

Now we need to define an operation that we can call later, we're going to expose a little echo operation to show you the
basics.

Service interfaces are described in an XML document called an IDD (Interface Definition Document), which meets the Cougar
IDL Schema. This document is enhanced by extensions which provide transport specific binding information, which can either
be held in a seperate document, or added inline. You can't mix and match the 2 styles.

Open up `wibble\wibble-idd\src\main\resources\WibbleService.xml` and add the following xml snippet at line 34, just after
the `</operation>` tag:

    <operation name="echo">
      <description/>
      <parameters>
        <request>
          <parameter name="message" mandatory="true" type="string">
            <description/>
          </parameter>
        </request>
        <response type="string">
          <description/>
        </response>
      </parameters>
    </operation>


And similarly in `wibble/wibble-idd/src/main/resources/WibbleService-Extentsions.xml` at line 28:

    <operation name="echo">
      <parameters>
        <request>
          <parameter name="message">
            <extensions>
              <style>query</style>
            </extensions>
          </parameter>
        </request>
      </parameters>
      <extensions>
        <path>/echo</path>
        <method>GET</method>
      </extensions>
    </operation>


This defines an operation called `echo` which will be exposed as an operation that can be called via a GET request when
invoked via HTTP using Cougar's own Rescript protocol. It defines a single parameter called `message` which is
mandatory and is a string, and when invoked via Rescript will be passed as a query parameter. It also declares that the
returned type will be a string.

As you can see, all elements have a description tag. Currently you must specify this tag, but it may be empty. We intend
to remove this restriction soon.

Cougar supports a wide range of datatypes, both simple and complex. For more information checkout the
[IDL reference](bsidl-reference.html).

Generate your stubs
-------------------

Simply building your project will generate the required stubs. Compilation will fail in the application module since the
new operation hasn't been implemented yet in your service implementation:

    C:\wibble>mvn install

Code your implementation
------------------------

Now we've got our interface stub generated we can implement our new method. We're just going to be implementing the
synchronous version of the interface for now, as it's simpler.

**TODO**: Need to fix that package!
Open up `C:\wibble\wibble-application\src\main\java\com\betfair\cougar\wibble\WibbleServiceImpl.java` in your favourite
editor (or open the whole project in your IDE) and add the following method body:

    @Override
    public String echo ( RequestContext ctx , String message , TimeConstraints timeConstraints) {
        return message;
    }

You'll notice the method takes 3 parameters, only one of which is the one declared in the interface, the other 2 are added
to all method signatures generated from the IDD (async methods get another - the observer to callback, but then they return
void).

The `RequestContext` contains other information about your request such as where it came from, uuids to enable transaction
correlation across distributed systems, resolved identities, geolocation and transport security information. It also provides
hooks to enable request, connected object and trace logging.

The `TimeConstraints` contains information on any time constraints the caller (or system in case of QoS protections) wish
to be placed on the operation invocation. A time constraint generally means that any response after the time has expired will
be ignored, and so allows implementors to cancel long running tasks or set timeouts themselves if this is of benefit.

Running your service
--------------------

Before we can run your new service we need to build it:

    mvn install


And now to launch:

    cd wibble-launch
    mvn exec:java

You will now see a bunch of console output, which should end with the last few lines as follows:

    2015-07-23 17:54:56.503: org.eclipse.jetty.server.ServerConnector INFO - Started ServerConnector@4482496e{HTTP/1.1}{0.0.0.0:8080}
    2015-07-23 17:54:56.503: org.eclipse.jetty.server.Server INFO - Started @9553ms
    2015-07-23 17:54:56.506: com.betfair.cougar.core.impl.ev.ContainerAwareExecutionVenue INFO - Cougar returned status OK at startup
    2015-07-23 17:54:56.507: com.betfair.cougar.core.impl.ev.ContainerAwareExecutionVenue INFO - **** COUGAR HAS STARTED *****


We're now ready to call our operation.


Calling your service
--------------------

Viewing the admin console
-------------------------
