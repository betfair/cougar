---
layout: default
---
Cougar Status Control
=====================

Cougar exposes service interfaces over a number of transports, but one thing that all transports that accept RPC calls
have in common is the concept of whether a node is in or out of service and able to accept invocations. Depending on the
transport used this manifests in a different way, for example with HTTP transports we tend to expect there to be a load
balancer which takes account of service health via health checks calls against the interface, whereas the socket transport
disconnects clients when the service is unhealthy and shuns new requests whilst winding down.

All this means that we need a consistent way of moving a node in or out of service so that it takes effect in the correct
manner on each transport.

In Cougar this is achieved by a Service Status Controller, which is tied into the monitoring framework by default and
allows an ops engineer to take a node out of service without shutting it down, this allows requests to drain down cleanly
before shutdown. It also persists the status value, so that when a node is started again it remains out of service until
being manually brought back in. This means that ops engineers no longer need to 'down' a service interface in the load
balancer.

# Accessing the Service Status Controller

This is done via the [JMX](Cougar_Monitoring.html#JMXHTMLBeanBrowser) interface exposed by Cougar:
[http://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DserviceStatusController.html](http://localhost:9999/ViewObjectRes//CoUGAR%3Aname%3DserviceStatusController.html).

To change the health of the service interfaces hosted by this Cougar instance, just change the `InService` parameter.
This will instantly affect the health of the service as returned by the `HealthService`.

## Persistence of service status

By default the controller assumes that `InService` is `true`. When a user changes the value of this via JMX, it
persists that status in a file, denoted by the overridable property `cougar.service.status.file`, which defaults to
`/tmp/cougar-service.status`, but should be set to `/var/run/<APP_NAME>/cougar-service.status` so that the value
is persisted across node restarts.

If the file is deleted at any point, then on next restart the status will revert to `true`.