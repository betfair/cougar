---
layout: default
---
Debugging in Cougar
===================

# Launching the Application from the IDE

Create a debug profile in your IDE with the following characteristics:

| Main class |Your `Launcher` classs, which is in the `launcher` project if you used the archetype |
| VM parameters | \- |
| Program parameters | \- |
| Project/module | `<yourService>-launcher` |

Of course you may choose to use a non-launcher project, or create a wrapper around `com.betfair.cougar.core.impl.Main` if that suits you.

# Launching the Application from Maven

The `launcher` project was created for you by the Cougar Maven archetype.

Open up a shell window and type `mvnDebug -P run initialize` in the `launcher` project.

Maven will start a JVM but wait for you to attach a debugger on port 8000 before continuing.

# Trace logging of Requests

You might also want to [trace](Tracing_Requests_in_Cougar.html) your requests.

# Diagnostics

Cougar logs the effective properties on the JMX Console. It can be accessed via operation listProperties on MBean **ApplicationProperties**
[https://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DApplicationProperties](https://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DApplicationProperties)

The HTTP endpoints exposed by via Cougar is exposed on MBean **EndPoints** under operation listEndpoints as in
[https://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DEndPoints](https://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DEndPoints)