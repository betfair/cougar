---
layout: default
---
# Threading, Sync, Async and Timeouts

# Overview

Over time, as Cougar has grown up, the threading model has been adjusted and tweaked, new transports (both client and
server) have been developed, and everything has become rather complex. This page attempts to explain away some of this
complexity and enable Cougar users to make informed decisions on how to configure/develop their applications.

## Async core

One thing to remember as you read all this and wonder about the complexities: **_Cougar is asynchronous at it's core_**

We provide synchronous constructs to hide the complex nature of asynchronous implementation, but it does affect how things
work, and to truly understand Cougar and how your app will work within it you need to be aware of this.

## Abbreviations used

**EV** - Execution venue

# Server concerns

## Server interface / implementation style

### Async

EV thread:

* Performs identity resolution (and tokenises for response)
* Invokes pre-interceptor
* Invokes service method

Any thread may call the onXXX methods on the observer, allowing the EV thread to be freed during calls that suffer from
wait on external resources (DB, remote call etc), which then:

* Invokes the post-interceptor
* Serialises the response
* Hands off to the transport

### Sync

* Is actually an async call (due to Cougar's core nature), adapted onto a sync interface:
 * Service method is called on an EV thread
 * Any blocking call out continues to consume the EV thread
 * Callback as a result of the method return is made on the same EV thread

## Server transports

### Jetty transport (for HTTP based protocols)

* Acceptor thread is used to open the connection and setup various fluff around that
* Selector threads actually read bytes from the wire
* Remaining threads do the following:
 * Deserialise request
 * Validation
 * Identity token resolution
 * Hand off to EV

The jetty threads also flush the data back after handoff from the EV after the service has returned

### Socket transport

* Acceptor thread is used to open the connection and setup various fluff around that
* Selector threads actually read bytes from the wire
* Remaining threads do the following:
 * Deserialise request
 * Validation
 * Identity token resolution
 * Hand off to EV

The MINA threads also write the serialised response to the wire after the service has returned

# Client concerns

## Generated client implementations

### Sync

* Makes the EV execution on the callers thread.
* If the client transport doesn't use the callers thread to write back to the observer then the optional timeout has
effect from the time the execute method completes (normally after the request has actually been sent).
* **The optional timeout has no effect when using a synchronous client transport**

### Async

* Makes the EV execution on the configured Executor (normally the EV pool).
* Ensures the transport callback to the observer is executed on the configured Executor (normally the EV pool).

## Client transports

### Socket

Does the following on the calling thread:

* Obtain a connection
* Write the request

Does the following on the **??** thread pool:

* Deserialise the response
* Call the observer onXXX method

### Sync HTTP

Does everything on the calling thread:

* Obtain a connection
* Write the request
 * **Blocks awaiting the response**
* Deserialise the response
* Call the observer onXXX method

### Async HTTP

Does the following on the calling thread:

* Obtain a connection
* Write the request

Does the following on it's internal thread pool (```CoUGAR.socket.transport.client:name=asyncHttpWorkerExecutor``` in JMX):

* Deserialise the response
* Call the observer onXXX method

# Observations

Once we know all of the above, we can make some observations on sensible / unwise combinations..

**TODO**