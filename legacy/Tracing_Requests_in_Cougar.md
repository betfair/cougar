---
layout: default
---
# Tracing Requests in Cougar

You can add a very high degree of debug to only specific requests (which will always get logged, regardless of logging level,
and to a specific file) by adding trace lines:

Example:

```
public class ExampleServiceImpl implements ExampleService {
    public SimpleResponseObject getSimpleResponse(RequestContext ctx, String message) throws SimpleException {
        ctx.trace("Starting getSimpleResponse for %s", message);
        ...
```

To enable tracing, you must associate the HTTP header ```X-Trace-Me``` on your RESCRIPT or SOAP request (any value will
trigger tracing), or if you are using the Cougar client, have the ```ExecutionContext``` implementation you pass into the
remote call have its ```traceLoggingEnabled``` method return ```true```.

The traces will be logged to ```<hostname>-trace.log[.<archiveSuffix>\]``` files in the logs directory on the Cougar server.

There is currently no support for tracing events, although there could be in the future by using an ```X-Trace-Me``` JMS header.
