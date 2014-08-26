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

package com.betfair.cougar.api.fault;

import java.util.List;
import java.util.Set;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.Result;


/**
 * Exception thrown when an error occurs in the business application.
 *
 */
@SuppressWarnings("serial")
public abstract class CougarApplicationException extends Exception implements Result {

	private ResponseCode code = ResponseCode.BadRequest;
    private final String exceptionCode;

    public CougarApplicationException(ResponseCode code, String exceptionCode) {
		super();
		this.code = code;
        this.exceptionCode = exceptionCode;
	}

    public CougarApplicationException(ResponseCode code, String exceptionCode, Throwable cause) {
		super(cause);
		this.code = code;
        this.exceptionCode = exceptionCode;
	}

	public ResponseCode getResponseCode() {
		return code;
	}

    public String getExceptionCode() {
        return exceptionCode;
    }
	public abstract List<String[]> getApplicationFaultMessages();
	public abstract String getApplicationFaultNamespace();
}
