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

package com.betfair.application.services;

import com.betfair.application.performance.BaselinePerformanceTester;
import com.betfair.application.util.BaselineClientConstants;
import com.betfair.application.util.HttpBodyBuilder;
import com.betfair.application.util.HttpCallLogEntry;
import com.betfair.application.util.HttpCallable;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Ignore
public class BaselineAppServiceTest extends XMLTestCase implements BaselineClientConstants {

	private static final String HOST;
	private static final int PORT;
	private static final String SOAP_ENDPOINT;
	private static final String REST_BASE;
    private static final String REST_BASE_ALT;
    private static final String RPC_ENDPOINT;
    private static final String WSDL_ENDPOINT;
	private static String CHARSET = "utf-8";

    static {
        // Read in the properties.
        try {
            Properties props = new Properties();
            InputStream is = BaselinePerformanceTester.class
                    .getResourceAsStream("/perftester/perftest.properties");
            props.load(is);
            is.close();
            HOST = props.getProperty("HOST");
            PORT = Integer.parseInt(props.getProperty("PORT"));
            SOAP_ENDPOINT = "http://" + HOST + ":" + PORT + "/BaselineService/v2.0";
            REST_BASE = "http://" + HOST + ":" + PORT + "/cougarBaseline/v2.0/";
            REST_BASE_ALT = "http://" + HOST + ":" + PORT + "/www/cougarBaseline/v2.0/";
            RPC_ENDPOINT = "http://" + HOST + ":" + PORT + "/json-rpc";
            WSDL_ENDPOINT = "http://" + HOST + ":" + PORT + "/wsdl";
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise tester", e);
        }
    }


    private HttpCallable identityChainCall = new HttpCallable("IDENTITY_CHAIN", REST_BASE
			+ IDENT_CHAIN_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(IDENT_CHAIN_SOAP));
    private HttpCallable identityChainCallFail = new HttpCallable("IDENTITY_CHAIN_FAIL", REST_BASE
			+ IDENT_CHAIN_PATH, SOAP_ENDPOINT, HttpStatus.SC_FORBIDDEN,
			new HttpBodyBuilder(IDENT_CHAIN_SOAP_FAIL));
    private HttpCallable identityChainCallAlt = new HttpCallable("IDENTITY_CHAIN", REST_BASE_ALT
            + IDENT_CHAIN_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
            new HttpBodyBuilder(IDENT_CHAIN_SOAP));
    private HttpCallable identityChainCallCredentialsNotFirst = new HttpCallable("CREDENTIALS_NOT_FIRST","",
            SOAP_ENDPOINT, HttpStatus.SC_OK, new HttpBodyBuilder(CREDENTIALS_NOT_FIRST_SOAP));
    private HttpCallable simpleGetCall = new HttpCallable("SIMPLE_GET", REST_BASE
			+ SIMPLE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(SIMPLE_GET_SOAP));
    private HttpCallable simpleGetCallAltURL = new HttpCallable("SIMPLE_GET", REST_BASE_ALT
            + SIMPLE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
            new HttpBodyBuilder(SIMPLE_GET_SOAP));
    private HttpCallable simpleGetCallReacharound = new HttpCallable("SIMPLE_GET_REACHAROUND", REST_BASE_ALT
            + SIMPLE_GET_PATH_REACHAROUND, SOAP_ENDPOINT, HttpStatus.SC_OK,
            new HttpBodyBuilder(SIMPLE_GET_SOAP_REACHAROUND));
	private	HttpCallable simpleCacheCall = new HttpCallable("SIMPLE_CACHE_GET", REST_BASE
			+ SIMPLE_CACHE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(SIMPLE_GET_SOAP));
	private HttpCallable largeGetCall = new HttpCallable("LARGE_GET", REST_BASE
			+ LARGE_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(LARGE_GET_SOAP));
	private HttpCallable largeMapGetCall = new HttpCallable("LARGE_MAP_GET", REST_BASE
			+ LARGE_MAP_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(LARGE_GET_SOAP));
	private HttpCallable listCall = new HttpCallable("LIST_GET", REST_BASE
			+ LIST_GET_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(LIST_GET_SOAP));
	private HttpCallable noParamsCall = new HttpCallable("NO_PARAMS", REST_BASE
			+ NO_PARAMS_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(NO_PARAMS_SOAP));
	private HttpCallable stylesCall = new HttpCallable("STYLES", REST_BASE
			+ STYLES_PATH, SOAP_ENDPOINT, HttpStatus.SC_OK,
			new HttpBodyBuilder(STYLES_SOAP));
	private HttpCallable dateCall = new HttpCallable("DATES", REST_BASE
			+ DATES_PATH, SOAP_ENDPOINT, new HttpBodyBuilder(DATES_BODY_JSON),
			new HttpBodyBuilder(DATES_BODY_XML), new HttpBodyBuilder(
					DATES_SOAP));
    private HttpCallable dateEchoCall = new HttpCallable("DATE_ECHO", REST_BASE
            + DATE_ECHO_PATH, SOAP_ENDPOINT, new HttpBodyBuilder(DATE_ECHO_BODY_JSON),
            new HttpBodyBuilder(DATE_ECHO_BODY_XML), new HttpBodyBuilder(
                    DATE_ECHO_SOAP));
	private HttpCallable doubleCall = new HttpCallable("DOUBLES", REST_BASE
			+ DOUBLE_PATH, SOAP_ENDPOINT, new HttpBodyBuilder(DOUBLE_BODY_JSON),
			new HttpBodyBuilder(DOUBLE_BODY_XML), new HttpBodyBuilder(
					DOUBLE_SOAP));
	private HttpCallable timeoutCall = new HttpCallable("TIMEOUT", REST_BASE
			+ SIMPLE_TIMEOUT_PATH, SOAP_ENDPOINT,
			HttpStatus.SC_GATEWAY_TIMEOUT,
			new HttpBodyBuilder(TIMEOUT_SOAP));
	private HttpCallable exceptionCall = new HttpCallable("EXCEPTION", REST_BASE
			+ EXCEPTION_PATH_UNAUTHORISED, SOAP_ENDPOINT,
			HttpStatus.SC_UNAUTHORIZED, new HttpBodyBuilder(EXC_GET_SOAP));
	private HttpCallable largePostCall = new HttpCallable("LARGE_POST", REST_BASE + LARGE_POST_PATH,
			SOAP_ENDPOINT, new HttpBodyBuilder(
					LARGE_POST_BODY_JSON_START,
					LARGE_POST_BODY_JSON_REPEAT,
					LARGE_POST_BODY_JSON_SEPARATOR,
					LARGE_POST_BODY_JSON_END), new HttpBodyBuilder(
					LARGE_POST_BODY_XML_START,
					LARGE_POST_BODY_XML_REPEAT, "",
					LARGE_POST_BODY_XML_END), new HttpBodyBuilder(
					LARGE_POST_SOAP_START, LARGE_POST_SOAP_REPEAT,
					"", LARGE_POST_SOAP_END));
	private HttpCallable mapCall = new HttpCallable("MAPS",
			REST_BASE + MAP_PATH, SOAP_ENDPOINT, new HttpBodyBuilder(
					MAPS_BODY_JSON), new HttpBodyBuilder(MAPS_BODY_XML),
			new HttpBodyBuilder(MAPS_SOAP));
	private HttpCallable complexMutatorCall = new HttpCallable("COMPLEX_MUTATOR",
			REST_BASE + "complex", SOAP_ENDPOINT, new HttpBodyBuilder(
					COMPLEX_MUTATOR_BODY_JSON), new HttpBodyBuilder(
					COMPLEX_MUTATOR_BODY_XML), new HttpBodyBuilder(
					COMPLEX_MUTATOR_SOAP));

    private HttpCallable voidReturnCall = new HttpCallable("VOID",
            REST_BASE + "voidResponseOperation?message=%1$d", SOAP_ENDPOINT,
			HttpStatus.SC_OK,new HttpBodyBuilder(VOID_PARAMS_SOAP));

    private HttpCallable enumResponseCall = new HttpCallable("EnumWrapper",
            REST_BASE + "callWithEnumResponse", SOAP_ENDPOINT,
			HttpStatus.SC_OK,new HttpBodyBuilder(ENUM_RESPONSE_REQUEST_SOAP));

     private HttpCallable listEventCall = new HttpCallable("ListEvent", REST_BASE
            + LIST_EVENT_PATH, SOAP_ENDPOINT, new HttpBodyBuilder(LIST_EVENT_BODY_JSON),
            new HttpBodyBuilder(LIST_EVENT_BODY_XML), new HttpBodyBuilder(
                    LIST_EVENT_SOAP));

    @Test
    public void testEnumResponse_JSON() throws Exception {
        makeRequestAndAssert(enumResponseCall, new Object[0], APPLICATION_JSON, ENUM_RESPONSE_JSON);
        makeRequestAndAssert(enumResponseCall, new Object[0], TEXT_JSON, ENUM_RESPONSE_JSON);
    }

    @Test
    public void testEnumResponse_XML() throws Exception {
        makeRequestAndAssert(enumResponseCall, new Object[0], APPLICATION_XML, ENUM_RESPONSE_XML);
        makeRequestAndAssert(enumResponseCall, new Object[0], TEXT_XML, ENUM_RESPONSE_XML);
    }

    @Test
    public void testEnumResponse_SOAP() throws Exception {
        makeRequestAndAssert(enumResponseCall, new Object[0], SOAP, ENUM_RESPONSE_SOAP);
    }

    @Test
    //Test added where credentials aren't first item in header to validate workaround to Axiom isEquals bug (DE5417)
    public void testCredentialsNotFirstInHeader() throws Exception {
        makeRequestAndAssert(identityChainCallCredentialsNotFirst, new Object[0], SOAP, CREDENTIALS_NOT_FIRST_RESPONSE_SOAP);
    }

    @Test
    public void testVoidResponse_JSON() throws Exception {
        makeRequest(voidReturnCall, APPLICATION_JSON);
        makeRequest(voidReturnCall, TEXT_JSON);
    }

	@Test
	public void testSimpleGet_JSON() throws Exception {
		makeRequestAndAssert(simpleGetCall, APPLICATION_JSON, SIMPLE_GET_RESPONSE_JSON);
		makeRequestAndAssert(simpleGetCall, TEXT_JSON, SIMPLE_GET_RESPONSE_JSON);
	}

	@Test
	public void testSimpleGet_XML() throws Exception {
		makeRequestAndAssert(simpleGetCall, APPLICATION_XML, SIMPLE_GET_RESPONSE_XML);
		makeRequestAndAssert(simpleGetCall, TEXT_XML, SIMPLE_GET_RESPONSE_XML);
	}

	@Test
	public void testSimpleGet_SOAP() throws Exception {
		makeRequestAndAssert(simpleGetCall, SOAP, SIMPLE_GET_RESPONSE_SOAP);
	}

    @Test
    public void testSimpleGetReacharound_JSON() throws Exception {
        makeRequestAndAssert(simpleGetCallReacharound, APPLICATION_JSON, SIMPLE_GET_REACHAROUND_RESPONSE_JSON);
        makeRequestAndAssert(simpleGetCallReacharound, TEXT_JSON, SIMPLE_GET_REACHAROUND_RESPONSE_JSON);
    }

    @Test
    public void testSimpleGetReacharound_XML() throws Exception {
        makeRequestAndAssert(simpleGetCallReacharound, APPLICATION_XML, SIMPLE_GET_REACHAROUND_RESPONSE_XML);
        makeRequestAndAssert(simpleGetCallReacharound, TEXT_XML, SIMPLE_GET_REACHAROUND_RESPONSE_XML);
    }

    @Test
    public void testSimpleGetReacharound_SOAP() throws Exception {
        makeRequestAndAssert(simpleGetCallReacharound, SOAP, SIMPLE_GET_REACHAROUND_RESPONSE_SOAP);
    }

	@Test
	public void testSimpleCacheGet_JSON() throws Exception {
		String result = makeRequest(simpleCacheCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(simpleCacheCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testSimpleCacheGet_XML() throws Exception {
		String result = makeRequest(simpleCacheCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(simpleCacheCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeGet_JSON() throws Exception {
		String result = makeRequest(largeGetCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(largeGetCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeGet_XML() throws Exception {
		String result = makeRequest(largeGetCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(largeGetCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeGet_SOAP() throws Exception {
		String result = makeRequest(largeGetCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeMapGet_JSON() throws Exception {
		String result = makeRequest(largeMapGetCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(largeMapGetCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeMapGet_XML() throws Exception {
		String result = makeRequest(largeMapGetCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(largeMapGetCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargeMapGet_SOAP() throws Exception {
		String result = makeRequest(largeMapGetCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testMapsNameClash_JSON() throws Exception {
		String result = makeRequest(mapCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(mapCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testMapsNameClash_XML() throws Exception {
		String result = makeRequest(mapCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(mapCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testMapsNameClash_SOAP() throws Exception {
		String result = makeRequest(mapCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testParameterStyles_JSON() throws Exception {
        Object[] args = new Object[] { 10, "2010-11-10T13:37:00.000" };
		makeRequestAndAssert(stylesCall, args, APPLICATION_JSON, STYLES_RESPONSE_JSON);
		makeRequestAndAssert(stylesCall, args, TEXT_JSON, STYLES_RESPONSE_JSON);
	}

	@Test
	public void testParameterStyles_XML() throws Exception {
        Object[] args = new Object[] { 10, "2010-11-10T13:37:00.000" };
		makeRequestAndAssert(stylesCall, args, APPLICATION_XML, STYLES_RESPONSE_XML);
		makeRequestAndAssert(stylesCall, args, TEXT_XML, STYLES_RESPONSE_XML);
	}

	@Test
	public void testParameterStyles_SOAP() throws Exception {
		makeRequestAndAssert(stylesCall, SOAP, STYLES_RESPONSE_SOAP);
	}

	@Test
	public void testDateRetrieval_JSON() throws Exception {
		makeRequestAndAssert(dateCall, APPLICATION_JSON, DATE_RESPONSE_JSON);
		makeRequestAndAssert(dateCall, TEXT_JSON, DATE_RESPONSE_JSON);
	}

	@Test
	public void testDateRetrieval_XML() throws Exception {
		makeRequestAndAssert(dateCall, APPLICATION_XML, DATE_RESPONSE_XML);
		makeRequestAndAssert(dateCall, TEXT_XML, DATE_RESPONSE_XML);
	}

	@Test
	public void testDateRetrieval_SOAP() throws Exception {
		makeRequestAndAssert(dateCall, SOAP, DATE_RESPONSE_SOAP);
	}

    @Test
    public void testDateEcho_XML() throws Exception {
        makeRequestAndAssert(dateEchoCall, APPLICATION_XML, DATE_ECHO_RESPONSE_XML);
        makeRequestAndAssert(dateEchoCall, TEXT_XML, DATE_ECHO_RESPONSE_XML);
    }

    @Test
    public void testDateEcho_JSON() throws Exception {
        makeRequestAndAssert(dateEchoCall, APPLICATION_JSON, DATE_ECHO_RESPONSE_JSON);
        makeRequestAndAssert(dateEchoCall, TEXT_JSON, DATE_ECHO_RESPONSE_JSON);
    }


	@Test
	public void testDoubleHandling_JSON() throws Exception {
		String result = makeRequest(doubleCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(doubleCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testDoubleHandling_XML() throws Exception {
		makeRequestAndAssert(doubleCall, APPLICATION_XML, DOUBLE_RESPONSE_XML);
		makeRequestAndAssert(doubleCall, TEXT_XML, DOUBLE_RESPONSE_XML);
	}

	@Test
	public void testDoubleHandling_SOAP() throws Exception {
		String result = makeRequest(doubleCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testListRetrieval_JSON() throws Exception {
		String result = makeRequest(listCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(listCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testListRetrieval_XML() throws Exception {
		String result = makeRequest(listCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(listCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testListRetrieval_SOAP() throws Exception {
		String result = makeRequest(listCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

    @Test
	public void XXtestGetTimeout() throws Exception {
		//This will need to have the service timeout set to something less than 5 seconds to work.
		makeRequest(timeoutCall, APPLICATION_JSON);
	}

	@Test
	public void testComplexMutator_JSON() throws Exception {
		String result = makeRequest(complexMutatorCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(complexMutatorCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testComplexMutator_XML() throws Exception {
		String result = makeRequest(complexMutatorCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(complexMutatorCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testComplexMutator_SOAP() throws Exception {
		String result = makeRequest(complexMutatorCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargePost_JSON() throws Exception {
		String result = makeRequest(
				largePostCall, APPLICATION_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(
				largePostCall, TEXT_JSON);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargePost_XML() throws Exception {
		String result = makeRequest(
				largePostCall, APPLICATION_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
		result = makeRequest(
				largePostCall, TEXT_XML);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

	@Test
	public void testLargePost_SOAP() throws Exception {
		String result = makeRequest(
				largePostCall, SOAP);
		assertNotNull(result);
		assertTrue("Empty buffer returned", result.length() > 0);
		System.out.println(result);
	}

    @Test
    public void testWsdlCall() throws Exception {
        HttpCallable c = new HttpCallable("wsdl",WSDL_ENDPOINT + "/BaselineService.wsdl", null, HttpStatus.SC_OK, null);
        String result = makeRequest(c, null, 0, APPLICATION_XML, null, null);

        // check result is OK
        assertTrue("WSDL not OK: "+result, result.contains("<wsdl:operation name=\"testSimpleGet\">"));
    }
	@Test
	public void testException_JSON() throws Exception {
		makeRequestAndAssert(exceptionCall, APPLICATION_JSON, EXCEPTION_RESPONSE_JSON);
		makeRequestAndAssert(exceptionCall, TEXT_JSON, EXCEPTION_RESPONSE_JSON);
	}

	@Test
	public void testException_XML() throws Exception {
		makeRequestAndAssert(exceptionCall, APPLICATION_XML, EXCEPTION_RESPONSE_XML);
		makeRequestAndAssert(exceptionCall, TEXT_XML, EXCEPTION_RESPONSE_XML);
	}

	@Test
	public void testException_SOAP() throws Exception {
		makeRequestAndAssert(exceptionCall, SOAP, EXCEPTION_RESPONSE_SOAP);
	}

	public void testSecureService() throws Exception {
	}

	@Test
	public void testNoParams_JSON() throws Exception {
		makeRequestAndAssert(noParamsCall, APPLICATION_JSON, NO_PARAMS_RESPONSE_JSON);
		makeRequestAndAssert(noParamsCall, TEXT_JSON, NO_PARAMS_RESPONSE_JSON);
	}

	@Test
	public void testNoParams_XML() throws Exception {
		makeRequestAndAssert(noParamsCall, APPLICATION_XML, NO_PARAMS_RESPONSE_XML);
		makeRequestAndAssert(noParamsCall, TEXT_XML, NO_PARAMS_RESPONSE_XML);
	}

	@Test
	public void testNoParams_SOAP() throws Exception {
		makeRequestAndAssert(noParamsCall, SOAP, NO_PARAMS_RESPONSE_SOAP);
	}

    @Test
    public void testMixed_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc mixed", RPC_ENDPOINT, MIXED_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check time is returned ok
        assertTrue("Time not returned: "+result, !result.isEmpty());

        assertTrue("FOO not returned", result.contains("{\"message\":\"FOO\"}"));
        assertTrue("BAR not returned", result.contains("{\"message\":\"BAR\"}"));
    }

    @Test
    public void testInvalidCredentials_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc mixed", RPC_ENDPOINT, MIXED_JSONRPC, HttpStatus.SC_FORBIDDEN);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Token-Username", "INVALID");
        String result = makeRequest(c, null, 0, "RPC", headers, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32099"));
        assertTrue("Error returned: "+result, result.contains("DSC-0015"));
    }

    @Test
    public void testBad_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, BAD_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32601"));
        assertTrue("Error returned: "+result, result.contains("DSC-0021"));
    }

    @Test
    public void testMissingParam_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, MISSING_MANDATORY_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32602"));
        assertTrue("Error returned: "+result, result.contains("DSC-0018"));
    }

    @Test
    public void testMandMissing_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, MAND_MISSING_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32602"));
        assertTrue("Error returned: "+result, result.contains("DSC-0018"));
    }

    @Test
    public void testMalformed_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, MALFORMED_JSONRPC, HttpStatus.SC_BAD_REQUEST);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32700"));
        assertTrue("Error returned: "+result, result.contains("DSC-0044"));
    }

    @Test
    public void testEmpty_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, EMPTY_JSONRPC, HttpStatus.SC_BAD_REQUEST);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertTrue("Error returned: "+result, result.contains("error"));
        assertTrue("Error returned: "+result, result.contains("-32600"));
        assertTrue("Error returned: "+result, result.contains("DSC-0031"));
    }

    @Test
    public void testNonMand_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, NONMAND_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertFalse("Error returned: "+result, result.contains("error"));
        assertTrue("FOO not returned", result.contains("{\"bodyParameter1\":\"foo\"}"));
        assertTrue("BAR not returned", result.contains("\"id\":\"WeirdStuff\""));
    }

    @Test
    public void testNonMandMissing_JSONRPC() throws Exception {
        HttpCallable c = new HttpCallable("json-rpc bad", RPC_ENDPOINT, NONMAND_JSONRPC, HttpStatus.SC_OK);
        String result = makeRequest(c, null, 0, "RPC", null, null);

        // check result is OK
        assertFalse("Error returned: "+result, result.contains("error"));
        assertTrue("FOO not returned", result.contains("{\"bodyParameter1\":\"foo\"}"));
        assertTrue("BAR not returned", result.contains("\"id\":\"WeirdStuff\""));
    }

    // Security header tests
    @Test
    public void testSecurity_Rescript() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Token-Username", "foo");
        headers.put("X-Token-Password", "bar");
        headers.put("X-Token-Nothing", "qux");

        Map<String, String> expectedHeaders = new HashMap<String, String>();
        expectedHeaders.put("X-Token-Username", "foo");
        expectedHeaders.put("X-Token-Password", "bar");
        // Do not expect unrecognised headers to be returned
        makeRequestAndAssertWithHeaders(identityChainCall, TEXT_JSON, IDENT_CHAIN_RESPONSE_JSON, headers, expectedHeaders);
    }

    // Security header tests
    @Test
    public void testSecurity_RescriptAlt() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-AltToken-Username", "foo");
        headers.put("X-AltToken-Password", "bar");
        headers.put("X-Token-Username", "NotFoo");
        headers.put("X-Token-Username", "NotBar");

        Map<String, String> expectedHeaders = new HashMap<String, String>();
        // rewrite not supported for alt binding
        expectedHeaders.put("X-AltToken-Username", null);
        expectedHeaders.put("X-AltToken-Password", null);
        expectedHeaders.put("X-Token-Username", null);
        // Do not expect unrecognised headers to be returned
        makeRequestAndAssertWithHeaders(identityChainCallAlt, TEXT_JSON, IDENT_CHAIN_RESPONSE_JSON, headers, expectedHeaders);
    }

    // Security header tests
    @Test
    public void testSecurity_RescriptFail() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Token-Username", "INVALID");
        // Check the invalid security headers cause a 403
        makeRequestAndAssertWithHeaders(identityChainCallFail, TEXT_JSON, IDENT_CHAIN_RESPONSE_JSON_FAIL, headers, null);
    }

    @Test
    public void testSecurity_SOAPFail() throws Exception {
        makeRequestAndAssert(identityChainCallFail, SOAP, IDENT_CHAIN_RESPONSE_SOAP_FAIL);
    }

    @Test
    public void testSecurity_SOAP() throws Exception {
        makeRequestAndAssert(identityChainCall, SOAP, IDENT_CHAIN_RESPONSE_SOAP);
    }

    @Test
    public void testListEventPublication() throws Exception {
        makeRequest(listEventCall, APPLICATION_JSON);
        makeRequest(listEventCall, TEXT_JSON);
    }

	private void makeRequestAndAssert(HttpCallable call, String contentType, String expectedValue) throws Exception {
		Object[] args = new Object[] { 10 };
        makeRequestAndAssert(call, args, contentType, expectedValue);
	}

    private void makeRequestAndAssertWithHeaders(HttpCallable call, String contentType, String expectedValue, Map<String, String> headersToAdd, Map<String, String> expectedReturnedHeaders) throws Exception {
        Object[] args = new Object[] { 10 };
        makeRequestAndAssert(call, args, contentType, expectedValue, headersToAdd, expectedReturnedHeaders);
    }

    private void makeRequestAndAssert(HttpCallable call, Object[] args, String contentType, String expectedValue) throws Exception {
        makeRequestAndAssert(call, args, contentType, expectedValue, null, null);
    }

    private void makeRequestAndAssert(HttpCallable call, Object[] args, String contentType, String expectedValue, Map<String, String> headersToAdd, Map<String, String> expectedReturnedHeaders) throws Exception {
        String result = makeRequest(call, args, args.length, contentType, headersToAdd, expectedReturnedHeaders);
        assertNotNull(result);
        assertTrue("Empty buffer returned", result.length() > 0);
        System.out.println(result);

        if (contentType.equals(APPLICATION_XML) || contentType.equals(TEXT_XML) || contentType.equals(SOAP)) {
            assertXMLEqual("Comparing test xml to control xml", String.format(expectedValue, args), result);
        } else {
            assertEquals(String.format(expectedValue, args), result);
        }
    }


	private String makeRequest(HttpCallable call, String contentType)throws Exception {
		return makeRequest(call, new Object[] { 10 }, 1, contentType, null, null);
	}

	private String makeRequest(HttpCallable call, Object[] paramValues, String contentType) throws Exception {
		return makeRequest(call, paramValues, 1, contentType, null, null);
	}

	private String makeRequest(HttpCallable call, int size, String contentType) throws Exception {
		return makeRequest(call, new Object[] { 10 }, size, contentType, null, null);
	}

	private String makeRequest(HttpCallable call, Object[] paramValues, int size, String contentType, Map<String, String> headersToAdd, Map<String, String> expectedReturnedHeaders)
			throws Exception {
		String result = null;
		HttpClient httpc = new DefaultHttpClient();
		HttpCallLogEntry cle = new HttpCallLogEntry();
		HttpUriRequest method = call.getMethod(contentType, paramValues, size, cle);

        if (headersToAdd != null) {
            for (Map.Entry<String, String> entry: headersToAdd.entrySet()) {
                method.addHeader(entry.getKey(), entry.getValue());
            }
        }

        InputStream inputStream = null;

		try {
            final HttpResponse httpResponse = httpc.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

			int expectedHTTPCode = call.expectedResult();
			if (contentType.equals("SOAP") && expectedHTTPCode != HttpStatus.SC_OK) {
				// All SOAP errors are 500.
				expectedHTTPCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			}
			assertEquals(expectedHTTPCode, statusCode);

            if (expectedReturnedHeaders != null) {
                for (Map.Entry<String, String> entry: expectedReturnedHeaders.entrySet()) {
                    Header h = httpResponse.getFirstHeader(entry.getKey());
                    if (h == null) {
                        assertNull(entry.getValue());
                    } else {
                        String headerValue = h.getValue();
                        assertEquals(entry.getValue(), headerValue);
                    }
                }
            }

			// Read the response body.
			InputStream is = httpResponse.getEntity().getContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b;
			while ((b = is.read()) != -1) {
				baos.write(b);
			}
			is.close();
			result = new String(baos.toByteArray(), CHARSET);

		} finally {
			// Release the connection.
			if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) { /* ignore */ }
            }
		}
		return result;
	}

}
