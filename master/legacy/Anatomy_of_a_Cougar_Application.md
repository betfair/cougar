---
layout: default
---
Anatomy of a Cougar Application
===============================

# idd - Interface Definition Documents

This project just holds your service interface's BSIDL and RESCRIPT extensions documents.  Anything that needs to generate code to connect to this service will depend on this project. Look
in the files located in src/main/resources for documentation on BSIDL and RESCRIPT.

Whenever you make a change to your BSIDL or RESCRIPT documents you need to re-install this artifact, then amend the interface implementation in the 'application' and 'rescript-client'
projects accordingly.

Maven:

|Install locally for use by 'application' and 'rescript-client' projects|mvn clean install|

Files:

|```/src/main/resources/<serviceName>Service.xml```|The BSIDL definition of the service interface|
|```/src/main/resources/<serviceName>Service-Extensions.xml```|The RESCRIPT mappings file for the service interface|

# application - The Application

This project contains an Cougar application module that includes an implementation of the interface defined in the 'idd' project.  This project generates a lot of Cougar framework code
from the 'idd' resources using the (inhouse) maven-idl2ds-plugin (see pom.xml).  Put your service's unit tests in this project.  The example implementation uses a synchronous interface,
but an asynchronous interface is also available if you prefer (note that the async interface has ZERO performance benefit, it only offers a different programming style that is suitable
for users who enqueue jobs internally).  Contact the Cougar support team if you need help with asynchronous interface implementation.

Whenever you make a change to your application's code, you need to re-install this artifact so that the 'launcher' project picks those changes up.

Maven:

|Unit test|mvn clean test|
|Install locally for use by 'launcher' project|mvn clean install|

Files:

|```/src/main/java```|Application code|
|```/src/test/java```|Unit tests|
|```/src/main/resources/conf```|Spring assembly files, default properties, other configuration|
|```/target/generated-sources```|Java sources generated from your ```idd``` project.  This folder needs to be included in your IDE as a source root|  
|```/target/generated-sources```|Non-source files generated from your ```idd``` project.  This folder needs to be included in your IDE as a source root|  
|```/target/generated-resources```|Your service's IDD (a merge of services' BSIDL and RESCRIPT mappings) plus WSDLs|  

# launcher - Application Launcher and Packager

This project is responsible for integration testing and packaging the Cougar 'application' module into a  component ZIP. The integration tests are transport-neutral and co-located
within the same Cougar instance.  However, these really are integration tests, since requests and responses traverse the entire 'Execution Venue' stack.

Maven:

|Run tests|mvn clean test|
|Run up a Cougar than will contain your application module (in test mode)|mvn exec:java|
|Debug (connect a debugger on port 8000)|mvnDebug exec:java|
|Build distributable|mvn assembly:assembly|

Files:

|```src/main/java/<package>/Launcher```|Main class|
|```src/main/resources/etc```|configuration|
|```src/main/resources/conf/overrides.properties```|Cougar property overrides|
|```src/test/java```|Integration tests|
|```src/test/resources/conf```|Cougar config to use in test mode|
|```src/main/assembly```|Maven assembly descriptors that will build distributables|

# rescript-client - Example RESCRIPT client app

This project is an example of how you'd write a Cougar service interface client application in Java, using the RESCRIPT transport.  The application just asks for input on stdin and
invokes the 'echoMessage' operation on the remote interface.  If you run this application or enable its integration tests, a remote Cougar must exist for it to connect to. By default,
it expects that interface to be available on http://localhost:8080.  You can change this by modding the 'cougar.client.rescript.remoteaddress' in src/test/resources/overrides.properties.

Maven:

|Run application in test mode|mvn exec:java (you must have built everything first)|
|Debug (connect a debugger on port 8000)|mvnDebug exec:java|
|Run integration tests (requires Cougar on localhost:8080)|mvn -P integration test|

Note: You can run your service on http://localhost:8080 for use in the tests by going to the 'launcher' project and doing 'mvn exec:java'.

Files:

|```src/main/java/<package>```|Main class and the sample Application|
|```src/main/resources/etc```|configuration|
|```/src/main/resources/conf```|Spring assembly files, default properties, other configuration|
|```src/tes/java```|Integration tests|
|```src/test/resources/conf```|Cougar config to use in test mode|
|```src/main/assembly```|Maven assembly descriptors that will build distributables|
