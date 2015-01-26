/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.betfair.cougar.core.api.exception.CougarClientException;
import org.testng.annotations.Test;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.baseline.v2.enumerations.TestParameterStylesHeaderParamEnum;
import com.betfair.baseline.v2.enumerations.TestParameterStylesQAHeaderParamEnum;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.exception.WotsitException;
import com.betfair.baseline.v2.to.ComplexObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsOperationResponseObject;
import com.betfair.baseline.v2.to.NonMandatoryParamsRequest;
import com.betfair.baseline.v2.to.SimpleResponse;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.util.RequestUUIDImpl;

public abstract class TestSuite {


	/**
	 * This implementation is built from an IDD that differs from the baseline service in a way that simulates an upgrade
	 * to service definition, such as adding/removing parameters, adding/removing fields from dataTypes, adding/removing ValidValues
	 */
	private BaselineSyncClient baselineClient;
	private ExecutionContext ec;

	public TestSuite() {
		this.ec = new ExecutionContext() {
			public IdentityChain getIdentity() {return null;}
			public GeoLocationDetails getLocation() {
				return new GeoLocationDetails(){
					public String getCountry() {return "GBR";}
					public String getLocation() {return "127.0.0.1";}
					public String getRemoteAddr() {return "127.0.0.1";}
					public List<String> getResolvedAddresses() {return Collections.singletonList("127.0.0.1");}
                                        public String getInferredCountry() {return "GBR";}
					public boolean isLowConfidenceGeoLocation() {return false;}};
			}
			public Date getReceivedTime() {return new Date();}
			public Date getRequestTime() {return new Date();}
			public RequestUUID getRequestUUID() {return new RequestUUIDImpl();}
			public boolean traceLoggingEnabled() {return false;}

            @Override
            public int getTransportSecurityStrengthFactor() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isTransportSecure() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

	}

	void setBaselineClient(BaselineSyncClient client) {
		this.baselineClient = client;
	}

    protected void initSystemProperties() {
        // set the relevant system props prior to starting cougar..
        System.setProperty("cougar.client.rescript.remoteaddress","http://127.0.0.1:8080/");
        System.setProperty("baseline.server.binaryProtocol.address","127.0.0.1:9003");
        System.setProperty("cougar.log.CONSOLE.level","INFO");
        System.setProperty("cougar.geoip.useDefault","true");
        System.setProperty("cougar.fault.detailed","true");
        System.setProperty("cougar.client.socket.session.workerTimeout","2");
        System.setProperty("cougar.app.name","cougar-iddversion-tests");
        System.setProperty("cougar.client.socket.ssl.supportsTls","false");
        System.setProperty("cougar.client.socket.ssl.requiresTls","false");
    }


	/**
	 * simulate
	 * <li> the server adding non mandatory parameters to an operation
	 * <li> the server adding non mandatory fields to a dataType
	 * <li> the server removing fields from the response dataType
	 * <p>
	 * In this case the client's IDD has removed 'headerParameter' operation,  bodyParameter2 from NonMandatoryParamsRequest and 'queryParameter'
	 * from the NonMandatoryParamsOperationResponse
	 * dataType.
	 * @throws SimpleException
	 */
	@Test
	public void testOptionalParameterAddedToService() throws SimpleException {
		NonMandatoryParamsRequest foo = new NonMandatoryParamsRequest();
		foo.setBodyParameter1("body1");
		NonMandatoryParamsOperationResponseObject response = baselineClient.nonMandatoryParamsOperation(ec,"query", foo);
		assertEquals("body1", response.getBodyParameter1());
		assertNull(response.getBodyParameter2());
		assertNull(response.getHeaderParameter());
	}

	/**
	 * simulate the server adding a mandatory parameter to an operation
	 * @throws SimpleException
	 */
	@Test
	public void testMandatoryParameterAddedToService() throws SimpleException {
		try {
			SimpleResponse response = baselineClient.testSimpleGet(ec);
			fail("expected cougar service exception");
		}
		catch (CougarClientException e) {
			assertEquals("DSC-0018", e.getFault().getErrorCode());
		}
	}

	/**
	 * Simulate the server adding a mandatory field to the params dataType, in this case value1
	 * @throws SimpleException
	 */
	@Test
	public void testMandatoryFieldAddedToParameter() throws SimpleException {
		ComplexObject co = new ComplexObject();
		co.setName("complexObjectName");
		co.setOk(true);
		co.setValue2(2);
		try {
			SimpleResponse response = baselineClient.testComplexMutator(ec, co);
			fail("expected cougar service exception");
		}
		catch (CougarClientException e) {
			assertEquals("DSC-0018", e.getFault().getErrorCode());
		}
	}

	/**
	 * Simulate the server adding an additional valid value to a parameter. In this case foobar
	 * @throws SimpleException
	 */
	@Test
	public void testValidValueAdded() throws SimpleException {
		List<String> results = baselineClient.testParameterStyles(ec, TestParameterStylesHeaderParamEnum.Bar, "foo", "bar", new Date(0), 1.0f);
		assertEquals("secondHeaderParam=foo", results.get(0));
		assertEquals("queryParam=bar", results.get(1));
		assertEquals("headerParam=Bar", results.get(2));
		assertTrue(results.get(3).startsWith("dateQueryParam=1 Jan 1970"));
		assertEquals("ok=1.0", results.get(4));
	}

	/**
	 * Simulate the server removing a ValidValue from the parameters by adding 'Baz' to the local IDD
	 * @throws SimpleException
	 */
	@Test
	public void testValidValueRemoved() throws SimpleException {
		try {
			baselineClient.testParameterStylesQA(ec, TestParameterStylesQAHeaderParamEnum.Baz, "foo", new Date(0));
			fail("expected cougar service exception");
		}
		catch (CougarClientException e) {
			assertEquals(getExpectedValidValueRemovedErrorCode(), e.getFault().getErrorCode());
		}
	}

    protected abstract String getExpectedValidValueRemovedErrorCode();

    @Test(expectedExceptions=IllegalArgumentException.class)
	public void testServerAddedException() throws SimpleException, WotsitException {
		baselineClient.testException(ec, "BadRequest", "CLOSED");
	}




}
