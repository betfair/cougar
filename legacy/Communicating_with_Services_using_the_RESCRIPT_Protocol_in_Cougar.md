---
layout: default
---

Note that if you're using Java, you're likely to want to use the [`curl` and the baseline service](Cougar_Baseline_Service_RESCRIPT_curls.html)
rather than writing your own, which will be much faster.

If you're dead set on writing your own Cougar client that uses the RESCRIPT protocol (and remember, this is not recommended),
read this document to generally familiarize yourself with RESCRIPT, then do some hands-on experimentation with
[`curl` and the baseline service](Cougar_Baseline_Service_RESCRIPT_curls.html).

## Introduction

The simplest protocol supported by Cougar is RESCRIPT - a combination of REST and RPC. This document will the process by
which an IDD is transformed into RESCRIPT.

Snippets of IDD will be used throughout this document, but all will be based on the following interface:

    <interface name="DemoIDD" owner="Angus McSporran" version="1.1" date="now()" ... >
        ...
        <dataType name="MyDataType">
            <parameter name="myInt" type="i32">
                ...
            </parameter>
            <parameter name="myString" type="string">
                ...
            </parameter>
            <parameter name="myEnum" type="MyEnum">
                ...
            </parameter>
            <parameter name="myNestedDataType" type="MyInnerDataType">
                ...
            </parameter>
        </dataType>
       ...
       <dataType name="MyInnerDataType">
            <parameter name="foo" type="string">
                ...
            </parameter>
            <parameter name="bar" type="string">
                ...
            </parameter>
        </dataType>
        ...
        <simpleType name="MyEnum" type="string">
            <validValues>
                <value name="FOO">
                    ...
                </value>
                <value name="BAR">
                    ...
                </value>
            </validValues>
        </simpleType>
        ...
        <extensions>
            <consumes>"application/xml","application/json"</consumes>
            <produces>"application/xml","application/json"</produces>
            <path>/demo</path>
        </extensions>
    </interface>

## Controlling the response format

RESCRIPT can currently return any data in XML or JSON. There are 2 ways of indicating to the framework what the desired
format is:

* the *alt* parameter: If there is an 'alt' query parameter available, it is used to defined the return type. Valid
values of this parameter are *xml* and *json*. For example, to define an XML return type on the
baseline service simple get, call [http://localhost:8080/baseline/v1.0/simple/foo?alt=xml.html](http://localhost:8080/baseline/v1.0/simple/foo?alt=xml.html)
* The *Accept* header: If the *alt* parameter is not defined, the HTTP standard 'Accept' header is used.


## XML Namespace

At build time, the IDD is transformed into a set of java classes and interfaces reflecting the IDD. At this stage the
XML namespace is also defined. It is set to `http://www.betfair.com/servicetypes/<MajorVersionNumber>/<ServiceName>/` - so
for the sample interface it would be:

    http://www.betfair.com/servicetypes/v1/DemoIDD

This namespace is common to all request and response XML data.

## Operation Invocations

All operations must have a uniquely defined endpoint from which data may be requested or to which it may be sent. This
endpoint is provided by the extensions tag. The extensions tags also define where else the data may be retrieved from
(these descriptions are based on HTTP, other transports may differ):

* *query* The data is passed as an HTTP query parameter. Parameter must be stringable.
* *header* The data is passed as an HTTP header. Parameter must be stringable.
* *body* The data will be POSTed as part of the HTTP body. If one or more body parameters is defined, the operation
extension method must be POST, not GET.

Stringable parameters are defined as data types that can be rendered as strings with no ambiguity:

* String
* integers (byte, i32, i64)
* floating point numbers (float, double)
* booleans
* enums (or, in the parlance of IDL - SimpleTypes with Valid Values defined)

If an operation is defined as a POST and has one or more body elements, then the POSTed data has to be provided in either
XML or JSON format.

## Requests

All parameters to an operation that are to be passed in the body are encapsulated within a request tag, whose name will
correspond to the name of the operation. Taking the following IDD:

    <operation name="bodyOperation" since="1.0.0">
        ...
        <parameters>
            <request>
                <parameter name="pathParam" type="string" mandatory="true">
                    ...
                    <extensions><style>path</style></extensions>
                </parameter>
                <parameter name="firstBodyParam" type="string" mandatory="true">
                    ...
                    <extensions><style>path</style></extensions>
                </parameter>
                <parameter name="secondBodyParam" type="MyDataType" mandatory="true">
                    ...
                    <extensions><style>path</style></extensions>
                </parameter>
                ...
        </parameters>
        ...
        <extensions>
            <path>/bodyop/{pathParam}</path>
            <method>POST</method>
        </extensions>
    </operation>

It can be seen that the the body must contain both the firstBodyParam and the secondBodyParam.

#### XML Request

The form of the XML request is:

    <BodyOperationRequest xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
        <firstBodyParam>value one</firstBodyParam>
        <secondBodyParam>
            <myInt>12345</myInt>
            <myString>string value</myString>
            <myEnum>FOO</myEnum>
            <myNestedDataType>
                <foo>foo string</foo>
                <bar>bar string</bar>
            </myNestedDataType>
        </secondBodyParam>
    </BodyOperationRequest>

#### JSON request

The form of the JSON request is:

    {
        "firstBodyParam":"value one"
        "secondBodyParam":{
            "myNestedDataType":{
                "foo":"foo string",
                "bar":"bar string"
            },
            "myInt":12345,
            "myString":"string value",
            "myEnum":"FOO"
        },
    }

## Responses

The response of an operation may be any single data type, which will always be wrapped in a response tag whose name will
correspond to the name of the operation. Taking the following IDD:

    <operation name="responseOperation" since="1.0.0">
        ...
        <parameters>
            ...
            <simpleResponse type="MyDataType">
                ...
            </simpleResponse>
            ...
        </parameters>
        ...
        <extensions>
            <path>/responseop</path>
            <method>GET</method>
        </extensions>
    </operation>

#### XML Response

The form of the XML response is:

    <ResponseOperationResponse xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
        <MyDataType>
            <myInt>12345</myInt>
            <myString>string value</myString>
            <myEnum>FOO</myEnum>
            <myNestedDataType>
                <foo>foo string</foo>
                <bar>bar string</bar>
            </myNestedDataType>
        </MyDataType>
    </ResponseOperationResponse>

#### JSON Response

The form of the JSON response is:

    {
        "myNestedDataType":{
            "foo":"foo string",
            "bar":"bar string"
        },
        "myInt":12345,
        "myString":"string value",
        "myEnum":"FOO"
    }

## Primitive Types

There are some issues with the display of primitive types in RESCRIPT, particularly with regard to maps and sets in XML.

#### dateTime objects

DateTimes are rendered as full ISO 8601 protocol. This format is simply defined as:

    [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].[mmm][Timezone]

A more detailed explanation can be found at [Wikipedia](http://en.wikipedia.org/wiki/ISO_8601). It should be noted
that shortened forms of ISO-8601 are not supported.

#### XML primitive list, set and map rendering

Since XML has no set way of rendering repeating data types, the data type name is used as a repeating value. For primitives
the data type name is not shown as the IDL primitive name, as this is an internal nomenclature, so names are shown as follows:

<table>
<tr>
<th>IDL Primitive Type</th><th>RESCRIPT tag</th></tr>
<tr>
<td> bool </td>
<td> Boolean </td>
</tr>
<tr>
<td> byte </td>
<td> Byte </td>
</tr>
<tr>
<td> i32 </td>
<td> Integer </td>
</tr>
<tr>
<td> i64 </td>
<td> Long </td>
</tr>
<tr>
<td> float </td>
<td> Float </td>
</tr>
<tr>
<td> double </td>
<td> Double </td>
</tr>
<tr>
<td> string </td>
<td> String </td>
</tr>
<tr>
<td> dateTime </td>
<td> Date </td>
</tr>
</table>

The fact that these RESCRIPT names are exactly the same as the Java primitive wrappers is entirely coincidental...

## Lists and Sets

The IDL has the capability to return sets and lists of both primitives and defined dataTypes. These requests are sent
over the wire in identical formats (as arrays) - the only difference is that at the Cougar end, sets are de-duped while
lists are left as-is. Taking the following IDL :

    <dataType name="ListsAndSets">
        ...
        <parameter name="dates" type="list(dateTime)">
            ...
        </parameter>
        <parameter name="dataTypes" type="set(MyInnerDataType)">
            ...
        </parameter>
        <parameter name="integers" type="set(i32)">
            ...
        </parameter>
    </dataType>

#### XML Representation

There is no native XML form for representing arrays, so RESCRIPT uses repeating tags to render the information. It can
 also be seen that each inner tag has no explicit name as defined in the IDL, so it maps to the class name.

    <ListsAndSets xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
      <dates>
        <Date>2009-07-05T18:54:55.876Z</Date>
        <Date>1971-01-27T00:42:51.888Z</Date>
      </dates>
      <dataTypes>
        <MyInnerDataType>
            <foo>foo string</foo>
            <bar>bar string</bar>
        </MyInnerDataType>
        <MyInnerDataType>
            <foo>foo 2 string</foo>
            <bar>bar 2 string</bar>
        </MyInnerDataType>
      </dataTypes>
      <i32s>
        <Integer>-138</Integer>
        <Integer>2627</Integer>
      </i32s>
    </ListsAndSets>

#### JSON Representation

JSON has the concept of an array, so all lists and sets are rendered using this construct:

    {
        "dates":[
            "2009-07-05T18:54:55.876Z",
            "1971-01-27T00:42:51.888Z"
        ],
         "dataTypes": [
              {
                  "foo":"foo string",
                  "bar":"bar string"
              },
              {
                  "foo":"foo 2 string",
                  "bar":"bar 2 string"
              }
         ],
         "i32s":[
             -138,
             262.9
         ]
    }

## Maps

The IDL has the capability to return maps of both primitives and defined dataTypes, although RESCRIPT imposes a restriction
that the key of the map must be Stringable, and therefore natively comparable. Taking the following IDL :

    <dataType name="MapDataType">
        ...
        <parameter name="cache" type="map(i32,ComplexObject)" mandatory="true">
            ...
        </parameter>
        <parameter name="someMap" type="map(string,dateTime)" mandatory="true">
            ...
        </parameter>
    </dataType>

#### XML Representation

As for lists and sets, there is no native XML form for representing arrays, so RESCRIPT uses a repeating listof name-value
pairs. Again, similarly to the list implementation, it can also be seen that each inner tag has no explicit name as defined
in the IDL, so it maps to the class name.

    <MapDataType xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
      <cache>
        <entry key="0">
           <MyInnerDataType>
              <foo>foo string</foo>
              <bar>bar string</bar>
          </MyInnerDataType>
        </entry>
        <entry key="1">
          <MyInnerDataType>
            <foo>foo 2 string</foo>
            <bar>bar 2 string</bar>
          </MyInnerDataType>
        </entry>
      </cache>
      <someMap>
        <entry key="String-1">
          <Date>1957-08-25T17:08:50.199+01:00</Date>
        </entry>
        <entry key="String-0">
          <Date>1954-03-07T17:23:06.360Z</Date>
        </entry>
      </someMap>
    </MapDataType>

#### JSON Representation

JSON is all about maps, so rendering a map is easy as pie:

    {
        "cache": {
            "0": {
                 "foo":"foo string",
                 "bar":"bar string"
            },
            "1": {
                 "foo":"foo 2 string",
                 "bar":"bar 2 string"
            }
        },
        "someMap": {
            "String-1":"1957-08-25T17:08:50.199+01:00",
            "String-0":"1954-03-07T17:23:06.360Z"
        }
    }

## Direct returning of Abstract Data Types

There is a special case that should be mentioned explicitly. If an operation is specified to return an abstract data type
(map, list or set), it may not be obvious exactly the form of the response. This section explicitly defines these responses.

### Direct response for Lists and Sets

As described above, Lists and Sets are handled the same way by RESCRIPT. Responses are defined for the following IDD:

    <operation name="responseOperation" since="1.0.0">
        ...
        <parameters>
            ...
            <simpleResponse type="list(MyDataType)">
                ...
            </simpleResponse>
            ...
        </parameters>
        ...
    </operation>

#### XML Array Response

The form of the XML response is:

    <ResponseOperationResponse xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
        <MyDataType>
            <myInt>12345</myInt>
            <myString>string value</myString>
            <myEnum>FOO</myEnum>
            <myNestedDataType>
                <foo>foo string</foo>
                <bar>bar string</bar>
            </myNestedDataType>
        </MyDataType >
        <MyDataType>
            <myInt>23456</myInt>
            <myString>string value 2</myString>
            <myEnum>BAR</myEnum>
            <myNestedDataType>
                <foo>foo 2 string</foo>
                <bar>bar 2 string</bar>
            </myNestedDataType>
        </MyDataType >
    </ResponseOperationResponse>

#### JSON Array Response

The form of the JSON response is:

    [
       {
          "myInt":12345,
          "myString":"string value",
          "myEnum":"FOO",
          "myNestedDataType":{
             "foo":"foo string",
             "bar":"bar string"
          }
       },
       {
          "myInt":23456,
          "myString":"string value 2",
          "myEnum":"BAR",
          "myNestedDataType":{
             "foo":"foo 2 string",
             "bar":"bar 2 string"
          }
       }
    ]

### Direct response for Maps

Responses are defined for the following IDD:

    <operation name="responseOperation" since="1.0.0">
        ...
        <parameters>
            ...
            <simpleResponse type="map(string, MyDataType)">
                ...
            </simpleResponse>
            ...
        </parameters>
        ...
    </operation>

#### XML Map Response

The form of the XML response is:

    <ResponseOperationResponse xmlns="http://www.betfair.com/servicetypes/v1/DemoIDD/">
        <entry key="0">
            <MyDataType>
                <myInt>12345</myInt>
                <myString>string value</myString>
                <myEnum>FOO</myEnum>
                <myNestedDataType>
                    <foo>foo string</foo>
                    <bar>bar string</bar>
                </myNestedDataType>
            </MyDataType >
        </entry>
        <entry key="1">
            <MyDataType>
                <myInt>23456</myInt>
                <myString>string value 2</myString>
                <myEnum>BAR</myEnum>
                <myNestedDataType>
                    <foo>foo 2 string</foo>
                    <bar>bar 2 string</bar>
                </myNestedDataType>
            </MyDataType >
        </entry>
    </ResponseOperationResponse>

#### JSON Map Response

The form of the JSON response is:

    {
       "0":{
          "myInt":12345,
          "myString":"string value",
          "myEnum":"FOO",
          "myNestedDataType":{
             "foo":"foo string",
             "bar":"bar string"
          }
       },
       "1":{
          "myInt":23456,
          "myString":"string value 2",
          "myEnum":"BAR",
          "myNestedDataType":{
             "foo":"foo 2 string",
             "bar":"bar 2 string"
          }
       },
    }

## Faults

Faults may occur in one of the following scenarios:
* The request is badly formed in some way (invalid encoding, mandatory data missing, etc)
* The Cougar framework had an unexpected problem (a java runtime exception).
* The application code returned a defined exception (One of the exceptions listed in the `<exceptions>` tag of the operation).
* The application code had an unexpected problem (a java runtime exception).

When this happens, a fault response is returned. Fault responses are explained the [Cougar Fault Reporting](Cougar_Fault_Reporting.html) document.

## Manipulating the URIs your interface methods are available on

By default Cougar will generate code that is annotated such that your service will be exposed on `/<interfaceName>/<version>/<operationName>`.

### Changing the interface name

You can change the `<interfaceName`> part of the URI by changing the value of the `/interface/extensions/path` element in the RESCRIPT document.  For example:

    <interface name="Example" version="1.0.0">
        ...
        <extensions>
            <path>/eg</path>
        </extensions>
    </interface>

In this case the path to some operation 'echo' would be `/eg/<version>/echo` instead of `/Example/<version>/echo`.

It's legal to leave the `path` empty, so the URI would end up as simply `/<version>/<operationName>`.

### Omitting the interface version

You can choose to omit the `<version>` part of the URL by defining the attribute `/interface/extensions/path` in the RESCRIPT document.  For example:

    <interface name="Example" version="1.0.0">
        ...
        <extensions>
            <path unversioned="true">/eg</path>
        </extensions>
    </interface>

In this case the path to some operation 'doSomething' would be `/eg/echo` instead of `/eg/<version>/echo`.

### Changing the operation name

You can change the `<operationName>` part of the URI by changing the value of the `/interface/operation/extensions/path` element in the RESCRIPT document.  For example:

    <interface name="Example" version="1.0.0">
        ...
        <operation name="echo">
            ...
           <extensions>
               <path>/ekko</path>
           </extensions>
           ...

In this case the path to some operation 'doSomething' would be `/Example/1.0/ekko` instead of `/Example/1.0/echo`
