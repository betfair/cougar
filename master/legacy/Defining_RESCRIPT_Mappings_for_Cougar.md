---
layout: default
---
# Introduction

The RESCRIPT mappings document describes how incoming RESCRIPT protocol operation requests and events for a given BSIDL
interface are bound to Java objects by Cougar.  It does this by mirroring the definitions of the operations to be found
in the BSIDL, and extending it with mappings elements.

Cougar module builds take this extensions document and the BSIDL and generates Java data binding code from them.

The name of the document can be in the following forms:

* ```<interfaceName>Service-Extension.xml```
* ```<interfaceName>-<version>-Extensions.xml```

In a Cougar projects this document normally lives in the ```idd``` project under ```src/main/resources```.

Whenever you want to validate your changes are valid, simply run a ```mvn install``` on your ```idd``` module. 

Note that you're required to keep your [BSIDL](Defining_Your_interface_in_BSIDL_for_Cougar.html) and RESCRIPT mappings
in sync (i.e. there must exist a RESCRIPT mappings document for your BSIDL, which must enumerate all the necessary
extensions for the operations you have defined, otherwise the ```idd``` build will fail).

# Anatomy of a RESCRIPT Path

The path to access an operation is:

```
[<interfacePath>/][v<majorVersion>]/<operationPath>
```

* The ```interfacePath``` is optional.  It defaults to your BSIDL interface name but can be changed.
* The ```majorVersion``` is ```x``` in your service's ```x.y[.z]``` BSIDL-defined version number.  There is a way of
omitting it if necessary.
* The ```operationPath``` is mandatory and defaults to the name of the operation in your BSIDL.  It can be changed.

# Document overview

```
<interface name="Baseline">
    <!-- operation mappings -->
    <!-- event mappings -->
    <!-- interface extensions -->
</interface>
```

# Extensions

## Operations

Example:

```
    <operation name="testSimpleGet">
        <parameters>
	    <request>
	        <parameter name="message">
		    <extensions>
		        <style>query</style>
		    </extensions>
	         </parameter>
	    </request>
	</parameters>
	<extensions>
	    <path>/simple</path>
            <method>GET</method>
	</extensions>
    </operation>
```

The request parameter names and types must match those of the operation in the BSIDL document proper.

### Parameter extraction

Each request parameter must have an extension that defines the parameter style.

<table>
<tr>
<th>Parameter Style</th><th>Extraction method</th><th>Restrictions</th></tr>
<tr>
<td>header</td>
<td>HTTP request header</td>
<td>Stringable</td>
</tr>
<tr>
<td>query</td>
<td>HTTP request query string</td>
<td>Stringable</td>
</tr>
<tr>
<td>body</td>
<td>Body parse (using the unmarshaller for the Content-Type specified in the HTTP request)</td>
<td>None</td>
</tr>
</table>

A stringable restriction means that the parameter's type may only be one of the following:
```bool, byte, i32, i64, float, double, string``` or a ```validValue``` of a ```simpleType```.

### Accessibility

The operation itself has extensions that describe the ```path``` element of the URL to the operation (relative to the
interface's own path, described in the 'interface extensions' section), and the HTTP ```method``` used to reach the operation.

The path to access an operation is:

```
<interfacePath>/v<majorDotMinorVersion>/<operationPath>
```

If omitted, the ```operationPath``` defaults to the name of the operation.

Supported HTTP methods are ```GET``` and ```POST```.  If the HTTP method of an operation is ```GET```, its request
parameters cannot have a bindings style of ```body```.

## Events

Example:

```
    <event name="TestSimpleGet" since="1.0">
        <description>An event to store a message to get</description>
        <parameter type="string" name="message" mandatory="true">
            <description>The message</description>
	    <extensions>
	        <style>body</style>
	    </extensions>
        </parameter>
    </event>
```

The event parameter names and types must match those of the operation in the BSIDL document proper.

The ```style``` of each event parameter must be ```body```.  Suppport for ```header``` ```style``` parameters may come. 

## Interface extensions

Example:

```
    <extensions>
        <path>/demo</path>
    </extensions>
```

### Path

The ```path``` extension of the interface partly contributes to how the service's operations are reached.

The path to access an operation is:

```
<interfacePath>/v<majorDotMinorVersion>/<operationPath>
```

The ```interfacePath``` and the ```version``` can both be omitted (the former by using the ```unversioned``` attribute,
and the latter by having an empty ```interfacePath```).

#### Default

If omitted completely (no element defined), the ```path``` defaults to the name of the interface.

#### Explicit

Example: ```<path>/baseline</path>```

#### Unversioned

By defining the ```path``` as ```unversioned```, you can tell Cougar to omit the service's version qualifier.

Example: ```<path unversioned="true">/baseline</path>```

#### Empty

By leaving the ```path``` empty, you are telling Cougar that your service will be accessed at ```/v<majorVersion>/<operationPath>```
or merely ```/<operationPath>``` if the interface path is ```unversioned```.

Example: ```<path></path>```