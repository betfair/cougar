---
layout: default
---
# Betfair Service IDL

## Overview

The Betfair Service Interface Definition Language (Betfair Service IDL or BSIDL for short) is an XML-based DSL (domain
specific language) for describing versioned service interfaces. The DSL uses a combination of ideas taken from
[Thrift](http://incubator.apache.org/thrift) with additional support for events and information on users of interfaces.

The format allows us to fully describe the methods, request/response parameters, exceptions as well as events and all
documentation associated with these entities. The format is already used throughout Betfair to describe APIs and services
interfaces and standardisation of this gives us the benefit of consistent documentation (generated from the XML) as well
as the ease of modification of wire formats and potential generation of serialisation/deserialisation code.


## Benefits

* Generated artifacts created in a consistent way across Betfair meaning that users of these artifacts are familiar with
conventions used.
* All users benefit from the debugging of generated output. For example WSDLs are notoriously hard to create that work
consistently across technology stack (a whole spec. was created to try and fix this [Basic Profile](http://www.ws-i.org/Profiles/BasicProfile-1.0.html))&nbsp;
* Data can be processed and validated before generation (e.g. Topological Sorts on data types for Thrift)
* Documentation is consistent and generated at low cost to the developer. They need only worry about content and not
format (no more editing word documents).
* Automated versioning tasks can be build around the IDL for marshalling from one version to another
* Enables the use of a consistent domain model throughout technology
* Creates a focal point for tooling and NFR support

## Types

The base types supported by the IDL are as follows:

* bool - a boolean value, true or false
* byte - an 8 bit signed byte
* i32 - a 32 bit signed integer
* i64 - a 64 bit signed integer
* float - a 32 bit signed floating point number (deprecated, please only use the double type)
* double - a 64 bit signed floating point number
* string - An encoding agnostic text or binary string
* dateTime - Date & Time expressed in xsd:dateTime format:&nbsp;[http://www.w3.org/TR/xmlschema-2/#dateTime](http://www.w3.org/TR/xmlschema-2/#dateTime)
* list(type) - a parametric, stable, ordered non unique collection of entities
* set(type) - a parametric unstable, unordered unique collection of entities
* map(keyType,valueType) - a parametric associative array of keys to values where keys follow the rules of a set

Note that generics are specified with '()' rather than '<>' as chevrons break the xml and are a pain to delimit.

### Elements

#### Preamble

```
<?xml version="1.0" encoding="ISO-8859-1"?>
<interface name="Test" owner="Joe Bloggs" version="0.1" date="01/01/1970">
	<authors>
		<author name="Joe Bloggs" email="joe@bloggs.com"/>
	</authors>
	<description>This is here to provide an example of all features in order for tests to be made</description>
</interface>
```

This is essentially the header of the IDL containing information about the service. Below each significant XPath is explained

<table>
<tr>
<th>XPath </th><th>Description </th>
</tr>
<tr>
<td> /interface/@name </td>
<td> The name of the service, this should match exactly with the file name without file extension (e.g. filename: FooBar.xml
corresponds to name: FooBar)  </td>
</tr>
<tr>
<td> /interface/@owner </td>
<td> The person who should be contacted with any queries regarding this service interface, in many cases one of the
authors but this isn't necessary  </td>
</tr>
<tr>
<td> /interface/@version </td>
<td> The version of the interface - please see [versioning](Interface_Versioning.html)</td>
</tr>
<tr>
<td> /interface/@date </td>
<td> The date that the interface version was frozen - in development service interface can use the now() function that
will insert the current date time - the date should be formatted as an [[ISO 8601 date|http://en.wikipedia.org/wiki/ISO_8601]] in UTC  </td>
</tr>
<tr>
<td> /interface/authors  </td>
<td> A list of people who have made alterations to the service. Sub-element author should include their name and email
address  </td>
</tr>
<tr>
<td> /interface/description </td>
<td> A description of the interface in English  </td>
</tr>
</table>

#### Operations

```xml
<operation name="someMethod" since="0.1.0">
	<description>Retrieves an account</description>
	<parameters>
		<request>
			<parameter name="mandatoryNumber" type="i64" mandatory="true">
				<description>abc</description>
			</parameter>
		</request>
		<simpleResponse type="i64">
			<description>Some Number</description>
		</simpleResponse>
	</parameters>
	<consumers>
		<product name="SoftGames"/>
	</consumers>
</operation>
```

<table>
<tr>
<th>XPath </th><th>Description </th></tr>
<tr>
<td> operation/@name </td>
<td> The name of the operation  </td>
</tr>
<tr>
<td> operation/@since </td>
<td> The version of the interface the operation was added in  </td>
</tr>
<tr>
<td> operation/description </td>
<td> A description of the purpose of this operation and how it should be used  </td>
</tr>
<tr>
<td> operation/parameters </td>
<td> Container for request/response sections  </td>
</tr>
<tr>
<td> operation/parameters/request </td>
<td> Container for the request parameters  </td>
</tr>
<tr>
<td> operation/parameters/request/parameter/description </td>
<td> A description of the parameter  </td>
</tr>
<tr>
<td> operation/parameters/request/parameter/@mandatory </td>
<td> Where the field is required or not (default false)  </td>
</tr>
<tr>
<td> operation/parameters/response </td>
<td> The return type for the operation if there is only one returned value  </td>
</tr>
<tr>
<td> operation/parameters/consumers </td>
<td> The known systems that use this operation  </td>
</tr>
</table>


#### Parameters

Parameters are an instantiation of a base type with a name, a nullability constraint, and a description. They can also
have valid values assigned to them.

##### Valid Values

Valid Values elements indicate the total set of available values which can be used for the named parameter, and which
should ultimately be validated in the interface implementation. An example is shown below

```xml
<parameter name="type" type="string">
	<description>The type of balance</description>
	<validValues>
		<value name="AVAILABLE_TO_BET">
			<description>The amount of the balance the user can Bet with</description>
		</value>
		<value name="AVAILABLE_TO_WITHDRAW">
			<description>The amount of the Balance that the user can Withdraw to a payment method</description>
		</value>
	</validValues>
</parameter>
```

So this is a string parameter called 'type' which can have the value AVAILABLE_TO_BET or AVAILABLE_TO_WITHDRAW

#### Simple types

These act as aliases for the base types supported by the IDL.

```xml
<simpleType name="someType" type="i64"/>
```

#### Data Types

```xml
<dataType name="Example">
	<description>This data type represents an example for a howto</description>
	<parameter name="account" type="Account">
		<description>The account being referenced in this Example</description>
	</parameter>
	<parameter name="master" type="bool">
		<description>If the account is a master account</description>
	</parameter>
</dataType>
```

#### Events

```xml
<event name="SomeChange" since="1.1.0">
	<description>Event indicating that a change has been made</description>
	<parameter type="i64" name="accountId" mandatory="true">
		<description>The Account ID to which the change has been made. Must be between 10^12 and -10^12</description>
	</parameter>
	<parameter type="i64" name="newAccountId" mandatory="false">
		<description>The new value for this account</description>
	</parameter>
</event>
```

#### Exceptions

```xml
<exceptionType name="SomeException">
	<description>This exception is thrown when Something bad happens</description>
	<parameter name="errorCode" type="string">
		<description>An error code</description>
	</parameter>
	<parameter name="message" type="string">
		<description>This is a message describes the reason for the exception in English and is for debug/logging purposes
		only and shouldn't be returned to users. There is also no need to localize this string.</description>
	</parameter>
</exceptionType>
```

#### Enumerations

BSIDL deliberately does not support enumerations as they make interfaces unnecessarily brittle on the possible values of
that enumerations, adding, removing or changing a value requires a re-release of the interface. Contraints on the values of a particular parameter should be enforced in the code and noted in the documentation.

#### Validation rules

More info required (regexps + size restrictions).

#### Extensions

Several elements within the IDL support extension markup which is specific to each modeled protocol. Extension elements
take the format of:

```xml
<extensions>
	<!-- Some Protocol name which supports extensions, which maps to the type generator name
             such as bfmq-java, or rest -->
	<rest>
		<httpmethod>GET</httpmethod>
	</rest>
</extensions>
```
