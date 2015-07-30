---
layout: default
---

How you interact with a service depends on your use case.

If you're a developer using Java, you're probably best off using the Cougar client (using the RESCRIPT protocol
underneath) since it'll offer you better-than-SOAP performance at a reasonable development cost.
SOAP is definitely not recommended, it's heavy and brittle. Binary transport is your best bet if it's available to you.

If you're a non-Java-using developer then we'd encourage you to build your own Cougar/RESCRIPT/JSON client and contribute
it back to the project.

Whatever you decide, remember to talk to the service owner about it, to avoid nasty surprises.

# Using SOAP

**DON'T**

You'll need to point a SOAP stack at the WSDL, such as [Apache Axis2](http://axis.apache.org/axis2/java/core/docs/quickstartguide.html#clientadb)
(this is not mentioned as a recommendation, just an example) in the Java world.

The WSDL is available at `http://<host>:<port>/wsdl/<interfaceName>Service.wsdl`.  The port will be 8080 unless you've
over-ridden it (`jetty.http.port`).

The endpoint in the WSDL will be set to `http://localhost/this-should-be-set-programatically` and it's not clear if,
and if so how, this can be changed from within the application. So you're best off assuming that you'll always need to get
hold of endpoints with some other means, and set them on your client explicitly.

SOAP service endpoints are always at: `http://<host>:<port>/<interfaceName>Service/vX.Y`.

# RESCRIPT

The endpoint at which the interface is available over RESCRIPT (JSON or XML content-types) depends on what paths you've
declared in your [BSIDL Extensions Document](Defining_RESCRIPT_Mappings_for_Cougar.html).

Your options for communicating with a service using RESCRIPT are currently:

* Using the [Cougar client](Invoking_Remote_Cougar_Services_with_the_Cougar_Client.html) (Java only - for now)
* Building your own client based on a knowledge of the service's paths and [how the RESCRIPT protocol works](Communicating_with_Services_using_the_RESCRIPT_Protocol_in_Cougar.html)
* Using a comprehensively-featured HTTP tool such as `curl` in conjunction with a knowledge of [how the RESCRIPT protocol works](Communicating_with_Services_using_the_RESCRIPT_Protocol_in_Cougar.html) (might be suitable for testing if nothing else)

The first option is the best.

# Finding Your HTTP Endpoints

Cougar makes a record of the HTTP endpoints it is exposing, please refer to the [Cougar Logging](Cougar_Logging.html) document.