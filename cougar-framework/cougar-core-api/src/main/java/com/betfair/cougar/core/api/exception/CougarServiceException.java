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

import java.util.logging.Level;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.fault.Fault;

public class CougarServiceException extends CougarException {
	private CougarApplicationException dae;

	public CougarServiceException(ServerFaultCode fault, String message) {
		super(Level.FINE, fault, message);
	}

	public CougarServiceException(ServerFaultCode fault, String message, CougarApplicationException dae) {
		super(Level.FINE, fault, message, dae);
		this.dae = dae;
	}

	public CougarServiceException(ServerFaultCode fault, String message, Throwable t) {
		super(Level.FINE, fault, message, t);
	}

	@Override
	public Fault getFault() {
		Fault fault = null;
    	if (dae != null) {
		    fault = new Fault(dae.getResponseCode().getFaultCode(), dae.getExceptionCode(), dae.getClass().getSimpleName(), dae);
    	} else {
    		fault = super.getFault();
    	}
    	return fault;
	}

	@Override
	public ResponseCode getResponseCode() {
    	if (dae != null) {
		    return dae.getResponseCode();
    	} else {
    		return super.getResponseCode();
    	}
	}
}
