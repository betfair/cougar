/*
 * Copyright 2014, Simon Matić Langford
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

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.fault.Fault;

import java.util.logging.Level;

public class CougarClientException extends CougarException {

	private CougarApplicationException dae;
    private boolean confirmedAsCougar;

	public CougarClientException(CougarException ce) {
		this(ce.getServerFaultCode(), ce.getMessage(), ce.getCause());
	}

	public CougarClientException(ServerFaultCode fault, String message) {
		this(fault, message, true);
	}

	public CougarClientException(ServerFaultCode fault, String message, boolean confirmedAsCougar) {
		super(Level.FINE, fault, message);
        this.confirmedAsCougar = confirmedAsCougar;
	}

	public CougarClientException(ServerFaultCode fault, String message, CougarApplicationException dae) {
		super(Level.FINE, fault, message, dae);
		this.dae = dae;
        this.confirmedAsCougar = true;
	}

	public CougarClientException(ServerFaultCode fault, String message, Throwable t) {
		this(fault, message, t, true);
	}

	public CougarClientException(ServerFaultCode fault, String message, Throwable t, boolean confirmedAsCougar) {
		super(Level.FINE, fault, message, t);
        if (t instanceof CougarApplicationException) {
            this.dae = (CougarApplicationException) t;
        }
        this.confirmedAsCougar = confirmedAsCougar;
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

    @Override
    protected String additionalInfo() {
        return confirmedAsCougar ? null : "Server not confirmed to be a Cougar";
    }

    public boolean isConfirmedAsCougar() {
        return confirmedAsCougar;
    }
}
