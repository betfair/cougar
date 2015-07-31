---
layout: default
---
# BSIDL Reference

## Overview

The Betfair Service Interface Definition Language (Betfair Service IDL or BSIDL for short) is an XML-based DSL (domain
specific language) for describing versioned service interfaces. The DSL uses a combination of ideas taken from
[Thrift](http://thrift.apache.org) with additional support for events and information on users of interfaces.

The format allows us to fully describe the methods, request/response parameters, exceptions as well as events and all
documentation associated with these entities.

It is the main input into the Maven [Cougar Codegen Plugin](codegen.html).

## Benefits

* Generated artifacts created in a consistent way meaning that users of these artifacts are familiar with
conventions used.
* All users benefit from the debugging of generated output. For example WSDLs are notoriously hard to create that work
consistently across technology stack (a whole specification was created to try and fix this [Basic Profile](http://www.ws-i.org/Profiles/BasicProfile-1.0.html))
* Data can be processed and validated before [generation](codegen.html) (e.g. Topological Sorts on data types for Thrift)
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

## File structure reference

Note that a document describing an interface is an interface definition document (IDD) and is written in the interface definition language (BSIDL). It may directly contain [extensions](#extensions) or these may be specified in a seperate extensions document.

Main IDD (Interface.xml):

<pre>
<code>
  &lt;<a href='#interface'>interface</a>>
    &lt;<a href='#authors'>authors</a>>
      &lt;author/>
    &lt;/authors>
    &lt;description/>
    &lt;<a href='#operation'>operation</a>>
      &lt;description/>
      &lt;parameters>
        &lt;request>
          &lt;<a href='#parameter'>parameter</a>>
            &lt;description/>
            &lt;validValues>
              &lt;value>
                &lt;description/>
              &lt;/value>
            &lt;/validValues>
          &lt;/parameter>
        &lt;/request>
        &lt;<a href='#response'>response</a>>
          &lt;description/>
        &lt;/response>
        &lt;exceptions>
          &lt;<a href='#exception'>exception</a>>
            &lt;description/>
          &lt;/exception>
        &lt;/exceptions>
      &lt;/parameters>
    &lt;/operation>
    &lt;<a href='#event'>event</a>>
      &lt;description/>
      &lt;<a href='#parameter'>parameter</a>>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/event>
    &lt;<a href='#exceptionType'>exceptionType</a>>
      &lt;description/>
      &lt;parameter>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/dataType>
    &lt;<a href='#dataType'>dataType</a>>
      &lt;description/>
      &lt;parameter>
        &lt;description/>
        &lt;validValues>
          &lt;value>
            &lt;description/>
          &lt;/value>
        &lt;/validValues>
      &lt;/parameter>
    &lt;/dataType>
    &lt;<a href='#simpleType'>simpleType</a>>
      &lt;description/>
      &lt;validValues>
        &lt;value>
          &lt;description/>
        &lt;/value>
      &lt;/validValues>
    &lt;/simpleType>
  &lt;/interface>
</code>
</pre>

IDD Extensions (Interface-Extensions.xml):

<pre>
<code>
  &lt;<a href='#interface'>interface</a>>
    &lt;<a href='#operation'>operation</a>>
      &lt;parameters>
        &lt;request>
          &lt;<a href='#parameter'>parameter</a>>
            &lt;<a href='#parameter-extensions'>extensions</a>>
              &lt;style/>
            &lt;/extensions>
          &lt;/parameter>
        &lt;/request>
      &lt;/parameters>
      &lt;<a href='#operation-extensions'>extensions</a>>
        &lt;path/>
        &lt;method/>
      &lt;/extensions>
    &lt;/operation>
    &lt;event>
      &lt;parameter>
        &lt;<a href='#parameter-extensions'>extensions</a>>
          &lt;style/>
        &lt;/extensions>
      &lt;/parameter>
    &lt;/event>
  	&lt;<a href='#interface-extensions'>extensions</a>>
  		&lt;path/>
  	&lt;/extensions>
  &lt;/interface>
</code>
</pre>

### &lt;interface>

The `interface` element describes the top-level attributes of service interface defined by this IDD.

    <?xml version="1.0" encoding="utf-8"?>
    <interface name="Test" owner="Joe Bloggs" version="0.1" date="01/01/1970">
        <authors>
            <author name="Joe Bloggs" email="joe@bloggs.com"/>
        </authors>
        <description>This is here to provide an example of all features in order for tests to be made</description>
    </interface>


The attributes are explained in more detail:
 * _name_: The name of the service, this should match exactly with the file name without file extension (e.g. filename: FooBar.xml corresponds to name: FooBar).
 * _owner_: The person who should be contacted with any queries regarding this service interface, in many cases one of the authors but this isn't necessary.
 * _version_: The version of the interface - please see [versioning](versioning.html).
 * _date_: The date that the interface version was frozen - in development service interface can use the now() function that will insert the current date time - the date should be formatted as an [[ISO 8601 date|http://en.wikipedia.org/wiki/ISO_8601]] in UTC.

The `description` element provides a description of the interface (usually in English).

<a name="interface-extensions"></a>
#### Supported extensions

*TODO*

#### &lt;authors>

The `authors` element must contain one or more `author` elements which give a name and email address for each author.

### &lt;operation>

<a name="operation-extensions"></a>
#### Supported extensions


### &lt;parameter>

<a name="parameter-extensions"></a>
#### Supported extensions


### &lt;response>


### &lt;exception>


### &lt;event>


### &lt;exceptionType>


### &lt;dataType>


### &lt;simpleType>



