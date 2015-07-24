---
layout: default
---
What’s wrong with SOAP
======================

Many things.

Brittle
-------

Pretty much every change you might make to an interface is a breaking change for a SOAP client. Want to add a new parameter to a returned object - breaking change. Want to add a new optional parameter to a call - breaking change. Want to change the type of a request parameter from int32 to int64 - breaking change. You get the message. Some of these could be resolved by diverging from the spec on the server side, but we can’t do anything about the myriad of SOAP clients available.

Essentially, almost every minor release of an interface would have to be a major release if SOAP is to be supported. We don’t want to hobble Cougar like this, so we choose to quietly ignore this fact and suggest you don’t use SOAP unless you can control the clients or afford to run many parallel versions of your interface (Cougar can run parallel major versions but can’t run parallel minor versions - a conscious choice and fine since minor versions should be backwards compatible).

If you have to use XML, at least use XML with Rescript.

Big
---

Serialised requests / responses are much larger in SOAP than in other formats such as JSON. This is due both to the SOAP envelope which is a large overhead for small object trees and also due to the expressiveness of XML, compare:

    <SomeObject>
      <SomeParameter>SomeValue</SomeParameter>
    </SomeObject>

    {“SomeParameter”:”SomeValue”}


Granted, using tiny names would ameliorate this, but that’s gonna be a pain to debug.

Slow
----

Related to the previous - SOAP is far slower to serialise/deserialise than the alternatives, it’s CPU intensive. Cougar is intended to be high performance, SOAP serialisation costs could far outweigh the processing costs for the operation.

