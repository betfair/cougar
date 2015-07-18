/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.core.api.exception;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.fault.CougarFault;

public class CougarServiceExceptionTest{

	@After
	public void tearDown(){

	}

	@Test
	public void testGetFault() {
		CougarServiceException dse = new CougarServiceException(ServerFaultCode.ResponseContentTypeNotValid,"message");
		CougarFault fault = dse.getFault();
		assertEquals(ServerFaultCode.ResponseContentTypeNotValid.getResponseCode().getFaultCode(),fault.getFaultCode());
	}

	@Test
	public void testCougarApplicationException() {
		//CougarServiceException dse = new CougarServiceException(ServerFaultCode.FrameworkError,"messge", null);
		CougarServiceException dse = new CougarServiceException(ServerFaultCode.FrameworkError,"messge", new MockException());
		CougarFault fault = dse.getFault();
		assertEquals(ResponseCode.InternalError.getFaultCode(), fault.getFaultCode());
		assertEquals(ResponseCode.InternalError, dse.getResponseCode());
	}

    @Test(expected = IllegalArgumentException.class)
    public void nullCheckedException() {
        new CougarServiceException(ServerFaultCode.ServiceCheckedException, "wibble", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingCheckedException() {
        new CougarServiceException(ServerFaultCode.ServiceCheckedException, "wibble");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwableInsteadOfCheckedException() {
        new CougarServiceException(ServerFaultCode.ServiceCheckedException, "wibble", new RuntimeException());
    }

    @Test
    public void nullRuntimeException() {
        new CougarServiceException(ServerFaultCode.ServiceRuntimeException, "wibble", null);
    }

    @Test
    public void missingRuntimeException() {
        new CougarServiceException(ServerFaultCode.ServiceRuntimeException, "wibble");
    }

    @Test
    public void throwableInsteadOfRuntimeException() {
        new CougarServiceException(ServerFaultCode.ServiceRuntimeException, "wibble", new RuntimeException());
    }

	private static class MockException extends CougarApplicationException {

		public MockException() {
			super(ResponseCode.InternalError, "DUMMY");
		}

		@Override
		public List<String[]> getApplicationFaultMessages() {
			return null;
		}

		@Override
		public String getApplicationFaultNamespace() {
			return null;
		}

	}
}
