---
layout: default
---
To invoke Cougar services using ```curl``` you need a very good understanding of how RESCRIPT works, and your efforts
may take considerable time and effort.  If time and effort are factors, avoid using this method, even for testing.
Use the Cougar Client (Java only - for now) instead.

However, if you are for some reason dead set on writing your own Cougar RESCRIPT client, using ```curl``` is a useful
way to get familiar with what kind of requests and responses
you'll need to be catering for.

The example below gives you a quick idea of what ```curl``` invocations will look like.  We're invoking a operation using
POST, supplying JSON and wanting XML back.  We've also enabled
[tracing](Tracing_Requests_in_Cougar.html).

```
curl -v --header "Accept: application/xml" --header "Content-Type: application/json" --header "X-Trace-Me:true" --data-binary "{\"message\":{\"bodyParam\":\"bodyParamValue\"}}" http://localhost:9001/myservice/v1.0/myoperation?queryParam=queryValue
```

Now the best thing to do is to read about [RESCRIPT](Communicating_with_Services_using_the_RESCRIPT_Protocol_in_Cougar.html),
then check out the Cougar 'baseline service' (a service that is maintained solely for the purpose of enumerating all possible
types of functionality afforded by Cougar) and play with its interface methods using ```curl```, for which
[very good documentation exists](Cougar_Baseline_Service_RESCRIPT_curls.html).