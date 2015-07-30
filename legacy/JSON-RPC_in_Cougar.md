---
layout: default
---
# JSON-RPC in Cougar

## What is JSON-RPC?

It’s a JSON protocol for calling operations by name as defined by the following standard:

[http://groups.google.com/group/json-rpc/web/json-rpc-2-0](http://groups.google.com/group/json-rpc/web/json-rpc-2-0)

The major new feature we realise is that you can make a batch of RPC calls in each Http request as a as follows:

    [{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2/testSimpleGet\", \"params\": [\"Hello JSON-RPC\"], \"id\": 1},{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2/testSimpleGet\", \"params\": [\"Hello JSON-RPC\"], \"id\": 2}]

A couple of points to notice straight off:

* The data is sent JSON encoded as body data -- so you’ll have to use Post to submit each JSON request.
* The above rpc request has 2 calls in the batch
* The id that you supply is free form, but whatever you supply, the server will return to you -- so it’s a correlation id
* The Form of operation identifier is as follows:
 * Service name / v major version . minor version / operation name
* Parameters are marshalled in order that corresponds to how they’re defined in the IDL -- so they don’t need to be named
* Content type headers, Accept and Content-Type can be either application/json or text/json


The server replies with the following:

    [{"jsonrpc":"2.0","result":{"message":"Hello JSON-RPC"},"id":1},{"jsonrpc":"2.0","result":{"message":"Hello JSON-RPC"},"id":2}]

## Setting it up in your Cougar application

In order to enable JSON_RPC, you need to setup a endpoint binding for it (which should live along side the other endpoint
bindings, likely defined in a spring document called http-transport-spring.xml):

    <bean parent="cougar.transport.AbstractProtocolBinding">
        <property name="contextRoot" value="json-rpc"/>
        <property name="identityTokenResolver" ref="SimpleRescriptIdentityTokenResolver"/>
        <property name="protocol" value="JSON_RPC"/>
    </bean>

If your app is not providing security through tokens / chains, then you can omit the identityTokenResolver, but the
context root (url) must be set, and the protocol must be "JSON_RPC"

You must also add your service's JSON-RPC binding descriptor to the binding descriptor util:set in your service registration
bean (in service-application.xml).

To test your JSON-RPC I suggest using curl or similar, and an example of that is as follows:

    curl --header "Accept: application/json" --header "Content-Type: application/json" --data "[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2/testSimpleGet\", \"params\": [\"Hello JSON-RPC\"], \"id\": 1},{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2/testSimpleGet\", \"params\": [\"Hello JSON-RPC\"], \"id\": 2}] " http://localhost:8080/json-rpc

Note that the url, unlike other protocols, needs nothing other than the JSON-RPC contextRoot (which you defined in your
protocol binding).


So:
* Jsonrpc:2.0 is always part of the result
* The result contains the output from the operation.  If it is a void method, then this will be null
* The id supplied at invocation time is returned to you.  The order the answers appear in the array should not be counted
upon
* If there is a failure for one of the calls -- a situation you could arrange by omitting a mandatory parameter -- then
you would see something like the following:

    [{"jsonrpc":"2.0","result":{"message":"Hello JSON-RPC"},"id":1},{"jsonrpc":"2.0","error":{"code":-32602,"message":"DSC-0018"},"id":2}]

* So it will successfully process requests as possible, but every request should have either a result or an error response

If you supply a request that cannot be parsed (I supplied alskdfjalksdjf as the json body), then you’ll get a total failure
type response as follows:

    {"jsonrpc":"2.0","error":{"code":-32700,"message":"DSC-0008"}}

Where possible, I’ve used the JSON error codes as they’ve supplied them.  The spec documents them, with the exception of \-32600 -- invalid request

\-32700 -- Parse error -- will be returned if there was some sort of complete fail parsing the RPC request
\-32601 -- Method not found - if the command processor was unable to find the method you’ve asked for
\-32602 -- if there was a problem parsing the parameters, or a mandatory parameter was not found
\-32603 -- internal error -- if there was an unexpected exception thrown processing the request
\-32099 -- Server Error -- if the invoked call threw a BSIDL defined exception

Quick tip: the JSON formatting for passing over list params would look like this:

    "params": [["100590665", "100590666"]]

