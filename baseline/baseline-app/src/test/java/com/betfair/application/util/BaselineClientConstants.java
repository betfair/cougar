/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.application.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface BaselineClientConstants {
    String SIMPLE_GET_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestSimpleGetRequest><bas:message>%1$d</bas:message></bas:TestSimpleGetRequest></soapenv:Body></soapenv:Envelope>";
    String SIMPLE_GET_SOAP_REACHAROUND = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestSimpleGetRequest><bas:message>FORWARD:%1$d</bas:message></bas:TestSimpleGetRequest></soapenv:Body></soapenv:Envelope>";
    String SIMPLE_CACHE_GET_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestSimpleCacheGetRequest><bas:message>%1$d</bas:message></bas:TestSimpleCacheGetRequest></soapenv:Body></soapenv:Envelope>";
    String LARGE_GET_SOAP  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestLargeGetRequest><bas:size>%1$d</bas:size></bas:TestLargeGetRequest></soapenv:Body></soapenv:Envelope>";
    String LARGE_MAP_GET_SOAP  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestLargeMapGetRequest><bas:size>%1$d</bas:size></bas:TestLargeMapGetRequest></soapenv:Body></soapenv:Envelope>";
    String LIST_GET_SOAP  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestListRetrievalRequest><bas:seed>%1$d</bas:seed></bas:TestListRetrievalRequest></soapenv:Body></soapenv:Envelope>";
    String NO_PARAMS_SOAP  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestNoParamsRequest></bas:TestNoParamsRequest></soapenv:Body></soapenv:Envelope>";
    String EXC_GET_SOAP    = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestExceptionRequest><bas:message>%1$d</bas:message><bas:responseCode>Unauthorised</bas:responseCode></bas:TestExceptionRequest></soapenv:Body></soapenv:Envelope>";
    String TIMEOUT_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestGetTimeoutRequest><bas:message>%1$d</bas:message></bas:TestGetTimeoutRequest></soapenv:Body></soapenv:Envelope>";
    String STYLES_SOAP     = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\">" +
            "<soapenv:Header></soapenv:Header><soapenv:Body>" +
                "<bas:TestParameterStylesRequest>" +
                  "<bas:pathParam>10</bas:pathParam>" +
                  "<bas:secondPathParam>20</bas:secondPathParam>" +
                  "<bas:HeaderParam>Foo</bas:HeaderParam>" +
                  "<bas:queryParam>40</bas:queryParam>" +
                  "<bas:dateQueryParam>2000-07-09T16:39:20Z</bas:dateQueryParam>"+
                "</bas:TestParameterStylesRequest>" +
            "</soapenv:Body>" +
          "</soapenv:Envelope>";
    String VOID_PARAMS_SOAP ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestVoidResponseRequest><bas:message>%1$d</bas:message></bas:TestVoidResponseRequest></soapenv:Body></soapenv:Envelope>";
    String ENUM_RESPONSE_REQUEST_SOAP="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:CallWithEnumResponseRequest></bas:CallWithEnumResponseRequest></soapenv:Body></soapenv:Envelope>";

    String DATES_SOAP    = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestDateRetrievalRequest><bas:inputDates><bas:name>foo</bas:name><bas:first>2001-01-01T00:00:00.0000Z</bas:first><bas:last>2000-07-09T16:39:20.685+01:00</bas:last></bas:inputDates></bas:TestDateRetrievalRequest></soapenv:Body></soapenv:Envelope>";
    String DATES_BODY_XML = "<TestDateRetrievalRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><inputDates><name>Foo</name><first>2009-01-01T00:00:00.0000Z</first><last>2009-01-01T00:00:00.123+02:30</last></inputDates></TestDateRetrievalRequest>";
    String DATES_BODY_JSON = "{\"inputDates\":{\"name\":\"Foo\",\"first\": \"2009-01-01T00:00:00.000Z\",\"last\": \"1999-12-31T23:59:59.999+01:24\"}}";

    String IDENT_CHAIN_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://www.betfair.com/security/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header><sec:Credentials><sec:Username>foo</sec:Username><sec:Password>bar</sec:Password><sec:Nothing>password</sec:Nothing></sec:Credentials></soapenv:Header><soapenv:Body><bas:TestIdentityChainRequest/></soapenv:Body></soapenv:Envelope>";
    String IDENT_CHAIN_SOAP_FAIL = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://www.betfair.com/security/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header><sec:Credentials><sec:Username>foo</sec:Username><sec:Password>INVALID</sec:Password><sec:Nothing>password</sec:Nothing></sec:Credentials></soapenv:Header><soapenv:Body><bas:TestIdentityChainRequest/></soapenv:Body></soapenv:Envelope>";

    String CREDENTIALS_NOT_FIRST_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://www.betfair.com/security/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\" xmlns:a=\"http://www.w3.org/2005/08/addressing\"><soapenv:Header><a:Action>retrieveAccount</a:Action><sec:Credentials><sec:X-Application>1001</sec:X-Application><sec:X-Authentication>vpIt3Zu38ZWppNBKYnbX7Uhno9zOwAydXvIwknGEdTc=</sec:X-Authentication></sec:Credentials></soapenv:Header><soapenv:Body><bas:TestIdentityChainRequest/></soapenv:Body></soapenv:Envelope>";

    String DATE_ECHO_SOAP    = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:DateTimeSimpleTypeEchoRequest><bas:msg>2001-01-01T00:00:00.123Z</bas:msg></bas:DateTimeSimpleTypeEchoRequest></soapenv:Body></soapenv:Envelope>";
    String DATE_ECHO_BODY_XML = "<DateTimeSimpleTypeEchoRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><msg><Date>2009-01-01T00:00:00.456+02:30</Date></msg></DateTimeSimpleTypeEchoRequest>";
    String DATE_ECHO_BODY_JSON = "{\"msg\":\"2009-01-01T00:00:00.333Z\"}";

    String DOUBLE_SOAP    = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestDoubleHandlingRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><bas:doubleContainer><bas:map><bas:entry key=\"foo\"><bas:Double>1.0</bas:Double></bas:entry><bas:entry key=\"bar\"><bas:Double>2.0</bas:Double></bas:entry></bas:map><bas:val>3.0</bas:val></bas:doubleContainer><bas:doubleVal>4.0</bas:doubleVal></bas:TestDoubleHandlingRequest></soapenv:Body></soapenv:Envelope>";
    String DOUBLE_BODY_XML = "<TestDoubleHandlingRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><doubleContainer><val>3.0</val><map><entry key=\"foo\"><Double>1.0</Double></entry><entry key=\"bar\"><Double>2.0</Double></entry></map></doubleContainer><doubleVal>4.0</doubleVal></TestDoubleHandlingRequest>";
    String DOUBLE_BODY_JSON = "{\"doubleContainer\":{\"map\": {\"foo\": 1.0,\"bar\": 2.0},\"val\":3.0},\"doubleVal\":4.0}";
    
    String COMPLEX_MUTATOR_SOAP = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestComplexMutatorRequest><bas:message><bas:name>Sum</bas:name><bas:value1>%1$d</bas:value1><bas:value2>%1$d</bas:value2></bas:message></bas:TestComplexMutatorRequest></soapenv:Body></soapenv:Envelope>";
    String COMPLEX_MUTATOR_BODY_XML = "<TestComplexMutatorRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><message><name>sum</name><value1>7</value1><value2>75</value2></message></TestComplexMutatorRequest>";
    String COMPLEX_MUTATOR_BODY_JSON = "{\"message\":{\"name\":\"sum\",\"value1\":7,\"value2\":5}}";

    String LARGE_POST_SOAP_START = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestLargePostRequest><bas:largeRequest><bas:size>%1$d</bas:size><bas:objects>";
    String LARGE_POST_SOAP_REPEAT = "<bas:ComplexObject><bas:name>asdf</bas:name><bas:value1>3</bas:value1><bas:value2>4</bas:value2></bas:ComplexObject>";
    String LARGE_POST_SOAP_END = "</bas:objects><bas:oddOrEven>ODD</bas:oddOrEven></bas:largeRequest></bas:TestLargePostRequest></soapenv:Body></soapenv:Envelope>";

    String LARGE_POST_BODY_XML_START = "<TestLargePostRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><largeRequest><size>1</size><objects><ComplexObject><name>asdf</name><value1>3</value1><value2>4</value2></ComplexObject>";
    String LARGE_POST_BODY_XML_REPEAT = "<ComplexObject><name>ssasdf</name><value1>23</value1><value2>42</value2></ComplexObject>";
    String LARGE_POST_BODY_XML_END = "</objects><oddOrEven>ODD</oddOrEven></largeRequest></TestLargePostRequest>";
    
    String LARGE_POST_BODY_JSON_START = "{\"largeRequest\":{\"size\":3,\"oddOrEven\":\"EVEN\",\"objects\":[";
    String LARGE_POST_BODY_JSON_REPEAT = "{\"name\":\"foo\",\"value1\":1,\"value2\":2}";
    String LARGE_POST_BODY_JSON_SEPARATOR = ",";
    String LARGE_POST_BODY_JSON_END = "]}}";
    
    String MAPS_SOAP       = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:TestMapsNameClashRequest><bas:mapParam><bas:cache><bas:entry key=\"foo\"><bas:String>bar</bas:String></bas:entry><bas:entry key=\"bar\"><bas:String>bar</bas:String></bas:entry><bas:entry key=\"bar\"><bas:String>foo</bas:String></bas:entry></bas:cache></bas:mapParam></bas:TestMapsNameClashRequest></soapenv:Body></soapenv:Envelope>";
    String MAPS_BODY_XML = "<TestMapsNameClashRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><mapParam><cache><entry key=\"foo\"><String>bar</String></entry><entry key=\"bar\"><String>bar</String></entry><entry key=\"bar\"><String>foo</String></entry></cache></mapParam></TestMapsNameClashRequest>";
    String MAPS_BODY_JSON = "{\"mapParam\":{\"cache\": {\"foo\": \"bar\",\"bar\": \"bar\",\"bar\": \"foo\"}}}";


    
    String STYLES_PATH = "styles?queryParam=%1$d&dateQueryParam=%2$s";
	String EXCEPTION_PATH_UNAUTHORISED = "exception?message=%1$d&responseCode=Unauthorised";
	String SIMPLE_TIMEOUT_PATH = "simple/timeout?message=%1$d";
	String LARGE_GET_PATH = "largeGet?size=%1$d";
	String LARGE_POST_PATH = "largePost";
	String SIMPLE_EVENT_POST_PATH = "simpleEventPublication";
    String LARGE_MAP_GET_PATH = "map?size=%1$d";
	String LIST_GET_PATH = "primitiveLists?seed=%1$d";
	String MAP_PATH = "map1";
	String SIMPLE_GET_PATH = "simple?message=%1$d";
    String SIMPLE_GET_PATH_REACHAROUND = "simple?message=FORWARD:%1$d";
	String SIMPLE_CACHE_GET_PATH = "cache?id=%1$d";
	String NO_PARAMS_PATH = "noparams";
	String DOUBLE_PATH = "doubles";
	String DATES_PATH = "dates";
    String DATE_ECHO_PATH = "dateTimeEcho?msg=2009-01-01T00:00:00.333Z";
    String IDENT_CHAIN_PATH = "identityChain";
    String LIST_EVENT_PATH = "emitListEvent";
	
	String SOAP = "SOAP";
	String TEXT_JSON = "text/json";
	String APPLICATION_JSON = "application/json";
	String TEXT_XML = "text/xml";
	String APPLICATION_XML = "application/xml";
	
    String IDENT_CHAIN_RESPONSE_JSON="{\"identities\":[{\"principal\":\"PRINCIPAL: Username\",\"credentialName\":\"CREDENTIAL: Username\",\"credentialValue\":\"foo\"},{\"principal\":\"PRINCIPAL: Password\",\"credentialName\":\"CREDENTIAL: Password\",\"credentialValue\":\"bar\"}]}";

    String IDENT_CHAIN_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header><sec:Credentials xmlns:sec=\"http://www.betfair.com/security/\"><sec:Username>foo</sec:Username><sec:Password>bar</sec:Password></sec:Credentials></soapenv:Header><soapenv:Body><TestIdentityChainResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><identities><Ident><principal>PRINCIPAL: Username</principal><credentialName>CREDENTIAL: Username</credentialName><credentialValue>foo</credentialValue></Ident><Ident><principal>PRINCIPAL: Password</principal><credentialName>CREDENTIAL: Password</credentialName><credentialValue>bar</credentialValue></Ident></identities></response></TestIdentityChainResponse></soapenv:Body></soapenv:Envelope>";
    String CREDENTIALS_NOT_FIRST_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body/></soapenv:Envelope>";

    String IDENT_CHAIN_RESPONSE_JSON_FAIL="{\"detail\":{},\"faultcode\":\"Client\",\"faultstring\":\"DSC-0015\"}";
    String IDENT_CHAIN_RESPONSE_SOAP_FAIL="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><soapenv:Fault><faultcode>soapenv:Client</faultcode><faultstring>DSC-0015</faultstring><detail/></soapenv:Fault></soapenv:Body></soapenv:Envelope>";

	String SIMPLE_GET_RESPONSE_JSON="{\"message\":\"%1$d\"}";
    String SIMPLE_GET_RESPONSE_OLD_VERSION_JSON="{\"message\":\"%1$d emitted by version 1.0.0 of Baseline\"}";
	String SIMPLE_GET_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestSimpleGetResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><SimpleResponse><message>%1$d</message></SimpleResponse></TestSimpleGetResponse>";
	String SIMPLE_GET_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestSimpleGetResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><message>1</message></response></TestSimpleGetResponse></soapenv:Body></soapenv:Envelope>";

    String SIMPLE_GET_RESPONSE_OLD_VERSION_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestSimpleGetResponse xmlns=\"http://www.betfair.com/servicetypes/v1/Baseline/\"><response><message>1 emitted by version 1.0.0 of Baseline</message></response></TestSimpleGetResponse></soapenv:Body></soapenv:Envelope>";

    String SIMPLE_GET_REACHAROUND_RESPONSE_JSON="{\"message\":\"FORWARDED:%1$d\"}";
    String SIMPLE_GET_REACHAROUND_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestSimpleGetResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><SimpleResponse><message>FORWARDED:%1$d</message></SimpleResponse></TestSimpleGetResponse>";
    String SIMPLE_GET_REACHAROUND_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestSimpleGetResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><message>FORWARDED:1</message></response></TestSimpleGetResponse></soapenv:Body></soapenv:Envelope>";

	String EXCEPTION_RESPONSE_JSON="{\"detail\":{\"SimpleException\":{\"reason\":\"%1$d\",\"errorCode\":\"NULL\"},\"exceptionname\":\"SimpleException\"},\"faultcode\":\"Client\",\"faultstring\":\"SEX-0002\"}";
	String EXCEPTION_RESPONSE_XML="<?xml version='1.0' encoding='utf-8'?><fault><faultcode>Client</faultcode><faultstring>SEX-0002</faultstring><detail><SimpleException><errorCode>NULL</errorCode><reason>%1$d</reason></SimpleException><exceptionname>SimpleException</exceptionname></detail></fault>";
	String EXCEPTION_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><soapenv:Fault><faultcode>soapenv:Client</faultcode><faultstring>SEX-0002</faultstring><detail><bas:SimpleException xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><bas:errorCode>NULL</bas:errorCode><bas:reason>1</bas:reason></bas:SimpleException></detail></soapenv:Fault></soapenv:Body></soapenv:Envelope>";
	String NO_PARAMS_RESPONSE_JSON="{\"version\":\"1.0.0\",\"status\":\"hello\"}";
	String NO_PARAMS_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestNoParamsResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><NoParamsResponse><status>hello</status><version>1.0.0</version></NoParamsResponse></TestNoParamsResponse>";
	String NO_PARAMS_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestNoParamsResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><version>1.0.0</version><status>hello</status></response></TestNoParamsResponse></soapenv:Body></soapenv:Envelope>";
	String DOUBLE_RESPONSE_JSON="{\"val\":3.0,\"topLevelVal\":4.0,\"map\":{\"foo\":1.0,\"bar\":2.0}}";
	String DOUBLE_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestDoubleHandlingResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><DoubleResponse><map><entry key=\"foo\"><Double>1.0</Double></entry><entry key=\"bar\"><Double>2.0</Double></entry></map><topLevelVal>4.0</topLevelVal><val>3.0</val></DoubleResponse></TestDoubleHandlingResponse>";
	String DATE_RESPONSE_JSON="{\"name\":\"First Passed Date: Thu Jan 01 00:00:00 GMT 2009, Second Passed Date: Fri Dec 31 22:35:59 GMT 1999\",\"first\":\"2009-01-01T00:00:00.000Z\",\"last\":\"1999-12-31T22:35:59.999Z\"}";
	String DATE_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestDateRetrievalResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><DateContainer><allDates xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/><first>2009-01-01T00:00:00.000Z</first><last>2008-12-31T21:30:00.123Z</last><name>First Passed Date: Thu Jan 01 00:00:00 GMT 2009, Second Passed Date: Wed Dec 31 21:30:00 GMT 2008</name></DateContainer></TestDateRetrievalResponse>";
	String DATE_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestDateRetrievalResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><name>First Passed Date: Mon Jan 01 00:00:00 GMT 2001, Second Passed Date: Sun Jul 09 16:39:20 BST 2000</name><first>2001-01-01T00:00:00.000Z</first><last>2000-07-09T15:39:20.685Z</last><allDates xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></response></TestDateRetrievalResponse></soapenv:Body></soapenv:Envelope>";

    String DATE_ECHO_RESPONSE_JSON="\"2009-01-01T00:00:00.333Z\"";
    String DATE_ECHO_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><DateTimeSimpleTypeEchoResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><Date>2009-01-01T00:00:00.333Z</Date></DateTimeSimpleTypeEchoResponse>";
    String DATE_ECHO_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><DateTimeSimpleTypeEchoResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response>2009-01-01T00:00:00.345Z</response></DateTimeSimpleTypeEchoResponse></soapenv:Body></soapenv:Envelope>";

	String STYLES_RESPONSE_JSON="[\"secondHeaderParam=null\",\"queryParam=%1$d\",\"headerParam=null\",\"dateQueryParam=Wed Nov 10 13:37:00 GMT 2010\",\"ok=null\"]";
	String STYLES_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><TestParameterStylesResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><String>secondHeaderParam=null</String><String>queryParam=%1$d</String><String>headerParam=null</String><String>dateQueryParam=Wed Nov 10 13:37:00 GMT 2010</String><String>ok=null</String></TestParameterStylesResponse>";
	String STYLES_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><TestParameterStylesResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response><String>secondHeaderParam=null</String><String>queryParam=40</String><String>headerParam=Foo</String><String>dateQueryParam=Sun Jul 09 17:39:20 BST 2000</String><String>ok=null</String></response></TestParameterStylesResponse></soapenv:Body></soapenv:Envelope>";

    String ENUM_RESPONSE_JSON="\"WEASEL\"";
    String ENUM_RESPONSE_XML="<?xml version='1.0' encoding='UTF-8'?><CallWithEnumResponseResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><SimpleValidValue>WEASEL</SimpleValidValue></CallWithEnumResponseResponse>";
    String ENUM_RESPONSE_SOAP="<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><CallWithEnumResponseResponse xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><response>WEASEL</response></CallWithEnumResponseResponse></soapenv:Body></soapenv:Envelope>";

    String MIXED_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2.0/testSimpleGet\", \"params\": [\"FOO\"], \"id\": 1},{ \"jsonrpc\": \"2.0\", \"method\": \"bASELINE/v2.0/TESTsIMPLEgET\", \"params\": [\"BAR\"], \"id\": 2},{ \"jsonrpc\": \"2.0\", \"method\": \"environment/v1.0/getTime\", \"params\": [], \"id\": 3}]";
    String BAD_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2.0/XXXXXXXX\", \"params\": [\"FOO\"], \"id\": 1}]";
    String NONMAND_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2.0/nonMandatoryParamsOperation\", \"params\": {\"message\": {\"bodyParameter1\":\"foo\"}}, \"id\": \"WeirdStuff\"}]";
    String MAND_MISSING_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2.0/nonMandatoryParamsOperation\", \"params\": {\"foo\": \"bar\"}, \"id\": \"WeirdStuff\"}]";
    String MULTI_VERSION_TEST_SIMPLE_GET_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2/testSimpleGet\", \"params\": [\"Version 2 echo\"], \"id\": \"v2\"},{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v1.3/testSimpleGet\", \"params\": [\"FOO\"], \"id\": \"v1\"}]";

    String EMPTY_JSONRPC="[]";
    String MISSING_MANDATORY_JSONRPC="[{ \"jsonrpc\": \"2.0\", \"method\": \"Baseline/v2.0/testSimpleGet\", \"params\": [], \"id\": 1}]";
    String MALFORMED_JSONRPC="COCKTROUSERS";

    String LIST_EVENT_SOAP      = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bas=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><soapenv:Header></soapenv:Header><soapenv:Body><bas:EmitListEventRequest><bas:messageList><bas:String>a</bas:String><bas:String>b</bas:String></bas:messageList></bas:EmitListEventRequest></soapenv:Body></soapenv:Envelope>";
    String LIST_EVENT_BODY_XML  = "<EmitListEventRequest xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><messageList><String>a</String><String>b</String></messageList></EmitListEventRequest>";
    String LIST_EVENT_BODY_JSON = "{\"messageList\":[\"a\",\"b\"]}";


}

