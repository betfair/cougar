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

package com.betfair.platform;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.enumerations.BodyParamEnumObjectBodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationHeaderParamEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationQueryParamEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectBodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectHeaderParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectQueryParameterEnum;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.exception.WotsitException;
import com.betfair.baseline.v2.to.BodyParamEnumObject;
import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.EnumOperationResponseObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsOperationResponseObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsRequest;
import com.betfair.baseline.v2.to.SimpleResponse;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.client.socket.ExecutionVenueNioClient;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.impl.CougarSpringCtxFactoryImpl;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exercise the baseline app using the binary protocol
 */
public class BinaryProtocolTest
{


	private static BaselineSyncClient client;

	private static DefaultHttpClient httpClient;
	private static ClassPathXmlApplicationContext springContext;

	private static ExecutionContext ec;

	@BeforeClass
	public static void beforeClass() throws ClientProtocolException, IOException, InterruptedException {
        // set the relevant system props prior to starting cougar..
        System.setProperty("cougar.client.rescript.remoteaddress","http://127.0.0.1:8080/");
        System.setProperty("baseline.server.binaryProtocol.address","127.0.0.1:9003");
        System.setProperty("cougar.log.CONSOLE.level","INFO");
        System.setProperty("cougar.geoip.useDefault","true");
        System.setProperty("cougar.client.socket.ssl.supportsTls","false");
        System.setProperty("cougar.client.socket.ssl.requiresTls","false");
        System.setProperty("cougar.client.socket.session.workerTimeout","2");
        System.setProperty("cougar.client.socket.reconnectInterval","500");
        System.setProperty("cougar.app.name","cougar-binaryprotocol-tests");

		httpClient = new DefaultHttpClient();
		setServerHealth(OK);
		CougarLoggingUtils.setTraceLogger(null); //because trace log is static and multiple spring contexts will try to set it
		CougarSpringCtxFactoryImpl context = new CougarSpringCtxFactoryImpl();
		springContext = context.create();
		client = (BaselineSyncClient) springContext.getBean("baselineClient");
		ec = ExecutionContextHelper.createContext("abc", "127.0.0.1");

	}



	@AfterClass
	public void afterClass() {
		((ExecutionVenueNioClient)springContext.getBean("socketTransport")).stop();
		springContext.getBeanFactory().destroySingletons();
		springContext.stop();
	}


	@Test(description="Test that making the server unhealthy causes the client to disconnect, and reconnect when the server becomes healthy again")
	public void testSocketHealth() throws ClientProtocolException, IOException, InterruptedException {
		setServerHealth(OK);
		try {
			Boolean result = client.boolSimpleTypeEcho(ec, true );
			Assert.assertEquals(Boolean.TRUE, result);
		} catch (SimpleException e) {
			Assert.fail("should be connected");
		}

		setServerHealth(FAIL);
		try {
			Boolean result = client.boolSimpleTypeEcho(ec, true);
			Assert.fail("shouldn't be connected");
		}
		catch (SimpleException e) {
			Assert.fail();
		}
		catch (CougarClientException e) {
			//not connected
		}

		setServerHealth(WARN);
		// It can take upto 50s for connection to re-establish
        try{Thread.sleep(50000);}catch (Exception ex){}
		try {
			Boolean result = client.boolSimpleTypeEcho(ec, true );
			Assert.assertEquals(Boolean.TRUE, result);
		} catch (SimpleException e) {
			Assert.fail("should be connected",e);
		}
	}

    @Test (description = "test application client not acting as proxy or intermediary for another customer or client")
    public void testNullCustomerIPAddress() {
        try {
            ExecutionContext ec = ExecutionContextHelper.createContext("abc",null);
            SimpleResponse r = client.testSimpleGet(ec,"simples");
            Assert.assertEquals(r.getMessage(), "simples");
        }
        catch (SimpleException e) {
            Assert.fail("unexpected exception",e);
        }
    }

	@Test(description="test basic invocation")
	public void testSimpleGet() {
		try {
			SimpleResponse r = client.testSimpleGet(ec, "simples");
			Assert.assertEquals(r.getMessage(), "simples");
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test that the binary protocol handles list collections")
	public void testLists() {
		try {
			List<String> request = Arrays.asList("abc","def","ghi");
			List<String> result = client.testSimpleListGet(ec, request);
			Assert.assertEquals(result, request);
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test that the binary protocol handles sets")
	public void testSets() {
		Set<String> request = new HashSet<String>(Arrays.asList("abc","def","ghi"));
		Set<String> response;
		try {
			response = client.testSimpleSetGet(ec, request);
			Assert.assertEquals(response, request);
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test the binary protocol handles maps")
	public void testMaps() {
		Map<String, String> request = new HashMap<String, String>();
		request.put("a", "a");
		request.put("b", "b");
		try {
			Map<String,String> response = client.testSimpleMapGet(ec, request);
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test binary protocol handles void responses")
	public void testVoidResponse() {
		try {
			client.voidResponseOperation(ec, "void");
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test no param operations")
	public void testNoParamOperations() {
		try {
			client.testNoParams(ec);
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test binary protocol handles null parameters")
	public void testNonMandatoryParams() {
		try {
			NonMandatoryParamsRequest request = new NonMandatoryParamsRequest();
			request.setBodyParameter1(null);
			request.setBodyParameter2(null);
			NonMandatoryParamsOperationResponseObject response = client.nonMandatoryParamsOperation(ec, null, null, request);
			Assert.assertNull(response.getHeaderParameter());
			Assert.assertNull(response.getQueryParameter());
			Assert.assertNull(response.getBodyParameter1());
			Assert.assertNull(response.getBodyParameter2());
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test binary protocol handles null parameters")
	public void testMandatoryParameterNotSupplied() {
		try {
			client.voidResponseOperation(ec, null);
			Assert.fail("expected an exception as parameter is mandatory");
		}
		catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
		catch (CougarValidationException e) {
			//all is good
		}
	}


	@Test(description="test binary protocol handles complex objects")
	public void testComplexObjects() {
		ComplexObject co = new ComplexObject();
		co.setName("sum");
		co.setValue1(1);
		co.setValue2(2);
		try {
			SimpleResponse response = client.testComplexMutator(ec, co);
			Assert.assertEquals(response.getMessage(), "sum = 3");
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test complex objects that have optional elements")
	public void testComplexObjectWithOptionalElement() {
		ComplexObject co = new ComplexObject();
		co.setName("sum");
		co.setValue1(1);
		co.setValue2(null);
		try {
			SimpleResponse response = client.testComplexMutator(ec, co);
			Assert.assertEquals(response.getMessage(), "sum = 1");
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test binary protocol handles methods that throw exceptions")
	public void testExceptions() {
		try {
			client.testException(ec, "BadRequest", "aMessage");
		} catch (SimpleException e) {
			Assert.assertEquals(e.getReason(), "aMessage");
			Assert.assertEquals(e.getExceptionCode(), "SEX-0002");
		} catch (WotsitException e) {
			Assert.fail("unexpected exception",e);
		}
	}

	@Test(description="test binary protocol handles methods that throw runtime exceptions")
	public void testRuntimeExceptions() {
		try {
			client.testException(ec, "BadRequest", "throwRuntime");
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		} catch (WotsitException e) {
			Assert.fail("unexpected exception",e);
		} catch (CougarClientException e) {
			//all good
		}
	}


	@Test(description="test binary protocol handles enumerations")
	public void testEnumeration() {
		BodyParamEnumObject obj = new BodyParamEnumObject();
		obj.setBodyParameter(BodyParamEnumObjectBodyParameterEnum.BarBody);
		try {
			EnumOperationResponseObject response = client.enumOperation(ec,  EnumOperationHeaderParamEnum.FooHeader, EnumOperationQueryParamEnum.BarQuery, obj );
			Assert.assertEquals(response.getHeaderParameter(), EnumOperationResponseObjectHeaderParameterEnum.FooHeader);
			Assert.assertEquals(response.getQueryParameter(), EnumOperationResponseObjectQueryParameterEnum.BarQuery);
			Assert.assertEquals(response.getBodyParameter(), EnumOperationResponseObjectBodyParameterEnum.BarBody);
		} catch (SimpleException e) {
			Assert.fail("unexpected exception",e);
		}
	}


	private static void setServerHealth(String health) throws ClientProtocolException, IOException, InterruptedException {
		HttpPost post = new HttpPost("http://localhost:8080/www/cougarBaseline/v2.0/setHealthStatusInfo");
		post.setEntity(new StringEntity(health));
		post.setHeader("Content-type", "application/json");
		readFully(httpClient.execute(post));

		for (int i=0; i<10; i++) {
			HttpGet get = new HttpGet("http://localhost:8080/www/healthcheck/v2.0/summary?alt=xml");
			List<String> lines = readFully(httpClient.execute(get));
			String expectedStatus = "OK";
			if (health == FAIL ) {
				expectedStatus = "FAIL";
			}

			for (String line : lines) {
				if (line.contains(expectedStatus)) {
					Thread.sleep(1000);
					return;
				}
			}
			System.err.println("server health not reporting expected status, will try again");
			Thread.sleep(1000);

		}
		System.err.println("server health not set, expect failing tests to follow ");

	}

	private static List<String> readFully(HttpResponse response) throws IllegalStateException, IOException {
		response.getStatusLine();
		InputStream is = response.getEntity().getContent();
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		List<String> lines = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}


	private static String OK = "{ \"message\" : " +
	"								{ \"initialiseHealthStatusObject\" : \"true\", " +
	"								  \"DBConnectionStatusDetail\" : \"OK\", " +
	"								  \"cacheAccessStatusDetail\" : \"OK\", " +
	"								  \"serviceStatusDetail\" : \"OK\" } }";

	private static String FAIL = "{ \"message\" : " +
	"								{ \"initialiseHealthStatusObject\" : \"true\", " +
	"								  \"DBConnectionStatusDetail\" : \"FAIL\", " +
	"								  \"cacheAccessStatusDetail\" : \"FAIL\", " +
	"								  \"serviceStatusDetail\" : \"FAIL\" } }";

	private static String WARN = "{ \"message\" : " +
	"								{ \"initialiseHealthStatusObject\" : \"true\", " +
	"								  \"DBConnectionStatusDetail\" : \"WARN\", " +
	"								  \"cacheAccessStatusDetail\" : \"WARN\", " +
	"								  \"serviceStatusDetail\" : \"WARN\" } }";

}
