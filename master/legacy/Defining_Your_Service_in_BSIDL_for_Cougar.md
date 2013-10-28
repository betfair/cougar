---
layout: default
---
# Introduction

The BSIDL document describes your service in an implementation-free way.

Cougar takes this document and generates Cougar framework compatible Java code from it.

The name of the document can be in the following patterns:

* ```<ServiceName>Service.xml```
* ```<ServiceName>-<version>.xml```

When you create a new Cougar module using the archetype, you're given an example BSIDL and
[RESCRIPT mappings](Defining_RESCRIPT_mappings_for_Cougar.html) documents to work with in the ```src/main/resources```
directory of the ```idd``` submodule.

You need to alter these documents to reflect the service you want to expose.  Note that you're required to keep your BSIDL
and [RESCRIPT mappings](Defining_RESCRIPT_mappings_for_Cougar.html) in sync (i.e. there must exist a RESCRIPT mappings
document for your BSIDL, which must enumerate all the necessary extensions for the operations you have defined, otherwise
the ```idd``` build will fail).

Whenever you want to validate your changes are valid, simply run a ```mvn install``` on your ```idd``` module. 

## Types

### Base

Example:  ```type="string"```

The base types supported by the IDL are as follows:
* bool - a boolean value, true or false
* byte - an 8 bit byte
* i32 - a 32 bit integer
* i64 - a 64 bit integer
* float - a 32 bit floating point number
* double - a 64 bit floating point number
* string - An encoding agnostic text or binary string
* dateTime - Date & Time expressed in xsd:dateTime format: ```\[-\]CCYY-MM-DDThh:mm:ss\[Z\|(+\](-)hh:mm\.html)```
* list(subType) - a parametric, stable, ordered none unique collection of entities
* set(subType) - a parametric unstable, unordered unique collection of entities
* map(keyType,valueType) - a parametric associative array of keys to values where keys follow the rules of a set
{quote}

Note that generics are specified with '()' rather than '&lt;>' as chevrons break the xml.

### Simple

Example: ```<simpleType name="someType" type="i64"/>```

These are used when you need to reference the base types supported by BSIDL.  if your type isn't simply a base type,
use the ```dataType``` construct instead.

### Data

Example:

```
<dataType name="MyType">
    <description>My Type</description>
    <parameter name="message" type="string" mandatory="true">
        <description>message</description>
    </parameter>
    <parameter name="message" type="list(OtherDataType)" mandatory="true">
        <description>message</description>
    </parameter>
</dataType>
```

### Exception

These can be returned by your service if something goes wrong.

Example:

```
<exceptionType name="SimpleException" prefix="SEX">
    <!-- 
        Cougar currently requires that all Exceptions have a first parameter
	that is defined by its valid values, all defined by an incrementing id 
    -->
    <description>This exception is thrown when an operation fails</description>
    <parameter name="errorCode" type="string">
        <description>the unique code for this error</description>
	<validValues>
	    <value id="1" name="GENERIC">
	        <description>Generic Error</description>
	    </value>
	    <value id="2" name="NULL">
	        <description>Null Input</description>
	    </value>
	    <value id="3" name="TIMEOUT">
	        <description>Timeout</description>
	    </value>
	    <value id="4" name="FORBIDDEN">
	        <description>Forbidden to call this operation</description>
	    </value>
	</validValues>
    </parameter>
    <parameter name="reason" type="string">
        <description>A human readable description of this error</description>
    </parameter>		
</exceptionType>
```

# Interface structure

Example:

```
<?xml version="1.0" encoding="ISO-8859-1"?>
<interface name="Baseline" 
    owner="Joe Bloggs' Manager"
    version="1.0.0" 
    date="now()" 
    namespace="com.betfair.baseline"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:noNamespaceSchemaLocation="http://www.betfair.com/BSIDL/4.0.xsd"
    xmlns:xi="http://www.w3.org/2001/XInclude">
    <authors>
        <author name="Joe Bloggs" email="joe@bloggs.com"/>
    </authors>
    <description>The Baseline Service</description>
    <!-- operation definitions here -->
    <!-- event definitions here -->
    <!-- dataType definitions here -->
    <!-- exceptionType definitions here -->
    <!-- xi:includes here -->
</interface>
```

The attribute and element names and values are mostly self-evident.

It's unclear what the ```date``` is used for (if at all), and what precisely it relates to.

## Version

Versions follow the pattern ```<major>.<minor>```.

<table>
<tr>
<th>What</th><th>Significance</th></tr>
<tr>
<td>```major```</td>
<td>interface breaking change</td>
</tr>
<tr>
<td>```minor```</td>
<td>non-breaking interface change</td>
</tr>
</table>

## Operations

Example:

```
    <operation name="testSimpleGet" since="1.0">
        <description>test of an idempotent service.  takes a single arg and echos it back</description>
	<parameters>
	    <request>
	        <parameter name="message" type="string" mandatory="true">
		    <description>the message to echo</description>
		</parameter>
	    </request>
	    <response type="SimpleResponse">
	        <description>The response</description>
	    </response>
	    <exceptions>
	        <exception type="SimpleException">
    		    <description>If the echo service fails for any reason</description>
    	        </exception>
	    </exceptions>
        </parameters>
    </operation>
```

It should be clear that an operation involves a request and a response.  If an error occurs, an exception can be sent
back instead of a response.

The operation can have many request parameters, which can be of type base, simple or data.

The operation can have zero request parameters, in which case it must specify ```<request/>``` (leaving the ```request```
block out will result in a code generation problem).

You can return a BSIDL base type by declaring the response to be simple:

```
    ...
    <simpleResponse type="i32">
        <description>The response</description>
    </simpleResponse>
```

An operation can return ```void``` like so:

```
    ...
    <simpleResponse type="void">
        <description>No response</description>
    </simpleResponse>
```

## Events

An event describes information that is emitted by the service.

Example:

```
    <event name="TestSimpleGet" since="1.0">
        <description>An event to store a message to send</description>
        <parameter type="string" name="message" mandatory="true">
            <description>The message</description>
        </parameter>
    </event>
```

An event can have many parameters, which can be of type base, simple or data.

## Valid values

Any ```string``` typed ```parameter```, ```response``` or ```simpleType``` may optionally specify a restricted set of valid values:

```
    <parameter name="errorCode" type="string">
        <description>the unique code for this error</description>
	<validValues>
	    <value id="1" name="GENERIC">
	        <description>Generic Error</description>
	    </value>
	    <value id="2" name="NULL">
	        <description>Null Input</description>
	    </value>
	    <value id="3" name="TIMEOUT">
	        <description>Timeout</description>
	    </value>
	    <value id="4" name="FORBIDDEN">
	        <description>Forbidden to call this operation</description>
	    </value>
	</validValues>
    </parameter>
```

This will be generated into an appropriately named Java enum. However, since we do not wish the addition of a new enumerated
value to instantly break all clients, the enum is generated with an additional value of ```UNRECOGNIZED_VALUE``` which is used
when the passed enumeration value is not recognised. This [enum soft failure handling](Enumeration_Handling_in_Cougar.html)
is enabled by default in generated Cougar clients and disabled by default in Cougar servers.

## Includes

You can include BSIDL-snippets in your document with include directives.

Example:

```
    <xi:include href="baseline-datatypes.inc" />
```

Includes could be useful for splitting large BSIDL documents up, or sharing data types between multiple services.

Note, however, that this syntactic sugar works only at the xml document level, if a data type or exception is included in
2 different interfaces, or 2 different versions of the same interface, at this point they become distinct entities, and
code generation steps will generate you 2 different classes.