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

package com.betfair.cougar.api;

import com.betfair.cougar.api.fault.FaultCode;


public enum ResponseCode implements Result {

	InternalError(FaultCode.Server),
    BusinessException(FaultCode.Server),
	Timeout(FaultCode.Server),
	ServiceUnavailable(FaultCode.Server),
	Unauthorised(FaultCode.Client),
	Forbidden(FaultCode.Client),
	NotFound(FaultCode.Client),
	UnsupportedMediaType(FaultCode.Client),
	MediaTypeNotAcceptable(FaultCode.Client),
	BadRequest(FaultCode.Client),
	BadResponse(FaultCode.Server),
    CantWriteToSocket(FaultCode.Client),
	Ok(null);

	private final FaultCode faultCode;

	ResponseCode(FaultCode faultCode) {
		this.faultCode = faultCode;
	}

	public FaultCode getFaultCode() {
		return faultCode;
	}

	public boolean isSuccess() {
		return faultCode == null;
	}

}
