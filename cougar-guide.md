---
layout: default
---
What is Cougar?
===============

Elevator Pitch
--------------

Cougar is an open source framework for implementing well defined service interfaces with true transport independence, freeing you up to write your core logic. Originally written by Betfair, and powering their core services, it is a high performance framework, easily supporting high concurrency requirements.

Oh, you wanted more detail than the front page? Well, in that case read on, after all, it's always a little more complex than that..

Core Cougar
-----------

At it's core, Cougar provides an execution venue within which executables can be run. Executables are asynchronous in nature, proving results (be those responses or faults) via a callback mechanism. In it's simplest form, an execution venue consists of a queue of outstanding execution requests, and a pool of threads which pull work from that queue.

Executables are often simple wrapper classes which map to method calls on a service interface, which is generated from an interface definition, written in Cougar's very own IDL (it looks rather like Thrift but in XML), the implementation of which is written by you (or your developers if you have some at your beck and call).

Execution requests are accompanied by the arguments passed in and some contextual information about the call which provides access to (amongst other things):

* Caller identity
* Remote address(es) and geolocation
* Transport security information

Cougar supports 3 core paradigms of interaction:

* RPC - your bog standard 'call an operation and get a response back'.
* Events - emission/consumption of events (typically to/from a JMS implementation - although not restricted to such).
* Connected Objects - replicant objects that can be mutated on a server and the state of which will be replicated to one or more clients.

Transports
----------

Layered over the top of this core, Cougar provides a set of transports for interacting with the outside world. You can extend this set of transports with your own as required, and we expect to expand this set over time:

* HTTP transport - runs on an embedded Jetty server. Supports RPC in a number of styles:
 * JSON-RPC
 * XML/JSON over HTTP (affectionally called Rescript)
 * SOAP (yes, we hate it too, but some people can't escape it)
* Socket transport - implementing a custom binary protocol which multiplexes requests over a TCP connection. This supports both RPC and connected objects.
* JMS transport - an abstract base transport for supporting JMS implementations. Supports events.
* ActiveMQ transport - a concrete event transport.

All these transports support varying security levels, all the way from none to 2-way SSL for non-repudiated client identification.

Cougar Client
-------------

Obviously at some point you're going to want to call other Cougar based service interfaces, and Cougar provides a client framework to do so. Supporting JSON over HTTP (RPC), our custom binary protocol (RPC & Connected Objects) and ActiveMQ or any other JMS provider (enabling subscription to other interfaces' events), Cougar clients are generated stubs from the target's interface definition document, which you can then bind to the transport of choice.

Other bits
----------

Alongside the main features above there are a bunch of other handy extras:

* An admin console (exposed on a seperate port), providing a JMX console, remote thread dumping capability and an SPI to enable you to add additional capabilities.
* Our code generator, mentioned above, is provided as a Maven plugin.
* Component health exposure is provided by a dedicated Health service interface which you may deploy alongside your own service interfaces in the same JVM. It gathers sub-component statuses via the [Tornjak](http://betfair.github.io/tornjak) framework, and exposes them in a form that can be read from a load balancer or from monitoring tooling. 
* The Tornjak integration also provides the capability to record performance metrics which are exposed in a manner sympathetic for capture into [OpenTSDB](http://opentsdb.net).
* Whilst the core of Cougar is asynchronous, it does provide synchronous wrappers and adapters for both service implementors and client consumers, reducing maintenance complexity when the power of async is not required.
* Configuration simplicity in the form of a strict 4 level hierarchy, ensuring that only those items of configuration which vary by deployment environment (e.g. dev vs production) need to be changed. Cougar also supports encrypted configuration items by integration with [Jasypt](http://www.jasypt.org).
