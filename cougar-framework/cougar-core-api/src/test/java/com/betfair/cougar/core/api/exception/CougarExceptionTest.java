/*
 * Copyright 2014, The Sporting Exchange Limited
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import com.betfair.cougar.api.fault.FaultCode;
import org.junit.After;
import org.junit.Test;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;


public class CougarExceptionTest {


	@After
	public void tearDown(){
	}

	@Test
	public void testServiceNoThrowableConstructor() {
		CougarServiceException dse = new CougarServiceException(ServerFaultCode.FrameworkError, "MESSAGE");
		checkFault(dse, FaultCode.Server, "DSC-0002", ResponseCode.InternalError, "MESSAGE", null);

		dse = new CougarServiceException(ServerFaultCode.ResponseContentTypeNotValid, "Invalid");
		checkFault(dse, FaultCode.Server, "DSC-0014", ResponseCode.InternalError, "ILLEGAL-ARGUMENT", null);
	}

	@Test
	public void testServiceApplicationExceptionConstructor() {
		CougarApplicationException dae = new CougarApplicationException(ResponseCode.NotFound, "APP-0001") {

			@Override
			public List<String[]> getApplicationFaultMessages() {
				return null;
			}

			@Override
			public String getApplicationFaultNamespace() {
				return null;
			}};
		CougarServiceException dse = new CougarServiceException(ServerFaultCode.ServiceCheckedException, "MESSAGE", dae);
		checkFault(dse, FaultCode.Client, "APP-0001", ResponseCode.NotFound, "", dae);
	}

	@Test
	public void testServiceOtherExceptionConstructor() {
		IllegalArgumentException iae = new IllegalArgumentException("MESSAGE");
		CougarServiceException dse = new CougarServiceException(ServerFaultCode.ServiceRuntimeException, "MESSAGE", iae);
		checkFault(dse, FaultCode.Server, "DSC-0005", ResponseCode.InternalError, "MESSAGE", null);
	}

	@Test
	public void testPanicInTheCougar() {
		PanicInTheCougar pitd = new PanicInTheCougar(new IllegalArgumentException("ILLEGAL-ARGUMENT"));
		checkFault(pitd, FaultCode.Server, "DSC-0001", ResponseCode.InternalError, "ILLEGAL-ARGUMENT", null);

		pitd = new PanicInTheCougar("CAUSE");
		checkFault(pitd, FaultCode.Server, "DSC-0001", ResponseCode.InternalError, "CAUSE", null);

		pitd = new PanicInTheCougar("CAUSE", new IllegalArgumentException("ILLEGAL-ARGUMENT"));
		checkFault(pitd, FaultCode.Server, "DSC-0001", ResponseCode.InternalError, "ILLEGAL-ARGUMENT", null);
	}

	@Test
	public void testFrameworkException() {
		CougarFrameworkException dfe = new CougarFrameworkException("CAUSE");
		checkFault(dfe, FaultCode.Server, "DSC-0002", ResponseCode.InternalError, "CAUSE", null);

		dfe = new CougarFrameworkException("CAUSE", new IllegalArgumentException("ILLEGAL-ARGUMENT"));
		checkFault(dfe, FaultCode.Server, "DSC-0002", ResponseCode.InternalError, "ILLEGAL-ARGUMENT", null);
			}

	@Test
	public void testValidationException() {
		CougarValidationException dfe = new CougarValidationException(ServerFaultCode.AcceptTypeNotValid, "CAUSE");
		checkFault(dfe, FaultCode.Client, "DSC-0013", ResponseCode.MediaTypeNotAcceptable, "CAUSE", null);

		dfe = new CougarValidationException(ServerFaultCode.InvocationResultIncorrect);
		checkFault(dfe, FaultCode.Server, "DSC-0003", ResponseCode.InternalError, "", null);
	}

    @Test
    public void testIdentityExceptions() {
        CougarServiceException exception = new CougarServiceException(ServerFaultCode.UnidentifiedCaller, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0033", ResponseCode.BadRequest, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.UnknownCaller, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0034", ResponseCode.BadRequest, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.UnrecognisedCredentials, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0035", ResponseCode.BadRequest, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.InvalidCredentials, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0036", ResponseCode.BadRequest, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.SubscriptionRequired, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0037", ResponseCode.Forbidden, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.OperationForbidden, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0038", ResponseCode.Forbidden, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.NoLocationSupplied, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0039", ResponseCode.BadRequest, "CAUSE", null);

        exception = new CougarServiceException(ServerFaultCode.BannedLocation, "CAUSE");
        checkFault(exception, FaultCode.Client, "DSC-0040", ResponseCode.Forbidden, "CAUSE", null);
    }

	private void checkFault(CougarException ex, FaultCode fault, String errror, ResponseCode resp,
		String detail, CougarApplicationException dae) {
		assertEquals(fault, ex.getFault().getFaultCode());
		assertEquals(errror, ex.getFault().getErrorCode());
		assertEquals(resp, ex.getResponseCode());
		if (dae != null) {
			final Writer strackTrace = new StringWriter();
			final PrintWriter pw = new PrintWriter(strackTrace);
			dae.printStackTrace(pw);
			assertEquals(strackTrace.toString(), ex.getFault().getDetail().getStackTrace());
		}
	}

}
