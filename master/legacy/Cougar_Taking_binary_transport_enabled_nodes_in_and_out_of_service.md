---
layout: default
---
# Taking binary transport enabled nodes in and out of service

Currently we take nodes in and out of service by doing so on the load balancer. Whilst this works fine for HTTP calls
which route through the load balancer, this still leaves the service in a healthy state and so leaves the binary transport
connected. This means were we to follow our current process of taking nodes out of the load balancer followed by app shutdown
there is opportunity to sever connections mid-way through RPC requests. Whilst clients need to be able to handle this
situation (as it could happen in a failure scenario) it's not nice and we should strive not to do so. This is obviously
more important for transactional services.

Cougar contains a monitor (InOutServiceMonitor), which allows us to forcibly turn a service unhealthy or healthy via JMX.
It also persists the last requested state so that the monitor returns to the same state post application restart.

As such, the new processes should be:

Taking out of service in preparation for application restart or upgrade:

* Change InService to false, click apply (JMX: /ViewObjectRes//CoUGAR%3Aname%3DserviceStatusController)
* Wait for node to drop out of service on load balancer
* Check that all socket connections have closed (SessionsOpened == SessionsClosed on JMX: /ViewObjectRes//CoUGAR.socket.transport%3Aname%3Dhandler)
* Service now fully out of service

Starting up an application and QA sanity:

* Start application
* Ensure that the only failing monitor on the detailed healthcheck is the "Cougar Service Status Monitor" (HTTP: /healthcheck/v2/detailed)
* Check Inservice false (JMX: /ViewObjectRes//CoUGAR%3Aname%3DserviceStatusController)
* Check SessionsOpened and SessionsClosed == 0 (JMX: /ViewObjectRes//CoUGAR.socket.transport%3Aname%3Dhandler)
* Perform sanity

Putting a node into service:

* Change InService to true, click apply (JMX: /ViewObjectRes//CoUGAR%3Aname%3DserviceStatusController)
* Check SessionsOpened > SessionsClosed (JMX: /ViewObjectRes//CoUGAR.socket.transport%3Aname%3Dhandler)
* Wait for node to appear as in service on load balancer

It is our intention to codify the first and last processes into a handy script to enable init scripts of the form:

* service &lt;xyz> down
* service &lt;xyz> up