---
layout: default
---
{:toc}

# What is Cougar?

This analysis from one of our developers seems to describe what Cougar is and isn't very well:

"Cougar is not an application server. By application server, I mean a component that serves as a container for deploying
some application artefacts, such as war and ear files. There is no concept of artefact in Cougar. I would rather classify
Cougar as a [design by convention](http://en.wikipedia.org/wiki/Convention_over_configuration), or even simpler, I would
say that Cougar platform is a template for [Spring](http://en.wikipedia.org/wiki/Spring_Framework) based applications
with some extensions for logging, monitoring, remote services, messaging, etc.

Note that, Cougar is well suited for developing and serving network services that are made available through different
transports (HTTP, ActiveMQ) and protocols (JSON, RESCRIPT (Cougar xml based custom protocol)), but it is not a platform
for web applications, which provide a rich html based content to web browsers."

# Why was it written?

It was written by a team of platform developers as a basis for creating the next generation of their services.  Since
then it has 'caught on', and its usage is now an Architectural policy within Betfair. We thought it so useful we shared
it with the world.

# Why not use Technologies X, Y and Z instead of Cougar?

Cougar exposes services over multiple transports, and can handle multiple protocols. If used everywhere it standardises
implementation of typical NFRs: logging, security concerns, monitoring.  It's high performance.  It's extensible.  It helps you focus
on writing your service.  Its creation was envisaged by intelligent people as a fundamental stepping stone to a long-term
vision of the Technology platform and landscape at Betfair.  These are just a few of the very good reasons _to_ use it.

# Concepts

## BSIDL

The Betfair Service Interface Definition Language ([BSIDL](Betfair_Service_IDL.html)) is Betfair's way of expressing
versioned, implementation-free service interfaces.

When creating a service, we describe the interface in BSIDL and then generate Cougar-compatible code (beans, service
interfaces and stubs) from it.

## Transports

A transport is the mechanism by which requests, responses and events flow in and out of Cougar services.

Cougar currently supports HTTP, a customer binary & Event transports.

## Protocols

A protocol defines the rules for sending and receiving messages over a transport.

Cougar currently supports SOAP and RESCRIPT protocols over the HTTP transport, and JSON over the ActiveMQ transport.

### SOAP

Simple Object Access Protocol ([SOAP](http://en.wikipedia.org/wiki/SOAP)) is a widely-used public XML based web services
standard, overseen by the W3C. [We don't like it much](Whats_wrong_with_SOAP.html).

### RESCRIPT

Restish Script (RESCRIPT) is an in-house protocol that allows data to be sent and received using a variety of content
types (this is where the "restish" bit begins and ends).

Cougar RESCRIPT currently supports Plain Old XML (POX) and JSON encodings.

## Marshaller and Unmarshaller

Marshallers and unmarshallers perform the data bindings (mappings) between the messages that come into Cougar and their
in-Cougar Java representations.

## Module

A Cougar module contains an Spring XML assembly, configuration files and Java classes.

Modules may extend or hook into the Cougar framework or other Cougar modules at documented extension points.

## Application

A Cougar application is circumscribed by whatever modules Cougar can find when it is started, which is a function of what
is on the Java classpath.