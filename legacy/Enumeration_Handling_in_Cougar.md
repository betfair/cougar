---
layout: default
---
# Enumeration Handling in Cougar

As mentioned when we talked about how to [define your service interface in BSIDL](Defining_Your_Service_in_BSIDL_for_Cougar.html),
parameters or simple types for which there are ```validValues``` elements defined are generated as Java enumerations in
the generated binding code.

However, since we do not wish the addition of a new enumerated value to instantly break all clients, the enum is generated
with an additional value of UNRECOGNIZED_VALUE which is used when the passed enumeration value is not recognised. This enum
soft failure handling is enabled by default in generated Cougar clients and disabled by default in Cougar servers.

## Configuration

You can change this behaviour by changing the following properties (varies by underlying protocol) for the client and server:

* ```cougar.socket.enums.hardFailure``` - defaults to ```true```
* ```cougar.http.enums.hardFailure``` - defaults to ```true```
* ```cougar.client.socket.enums.hardFailure``` - defaults to ```false```
* ```cougar.client.http.enums.hardFailure``` - defaults to ```false```

## Obtaining the raw value

In the case that an enumerated type is used within a more complex data type then the raw value of the enumerated type
will be available in a ```raw<paramName>Value``` field within that complex type. If the enumerated type is returned
directly from an operation call then it will be wrapped in an EnumWrapper in the generated client interface, from which
can be obtained the raw value.

The raw value will always contain exactly what was passed in, and so care should be taken since this is a possible source
of an injection attack.