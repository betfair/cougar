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

package com.betfair.cougar.transport.api.protocol.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.fault.CougarFault;

public class ResponseCodeMapper {
    private static final String COUGAR_FAULT_PREFIX = "DSC-";
	private static final Map<ResponseCode, Integer> RESPONSE_CODES = new HashMap<ResponseCode, Integer>();
	static {
		RESPONSE_CODES.put(ResponseCode.InternalError, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        RESPONSE_CODES.put(ResponseCode.BusinessException, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		RESPONSE_CODES.put(ResponseCode.Timeout, HttpServletResponse.SC_GATEWAY_TIMEOUT);
		RESPONSE_CODES.put(ResponseCode.ServiceUnavailable, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		RESPONSE_CODES.put(ResponseCode.Unauthorised, HttpServletResponse.SC_UNAUTHORIZED);
		RESPONSE_CODES.put(ResponseCode.Forbidden, HttpServletResponse.SC_FORBIDDEN);
		RESPONSE_CODES.put(ResponseCode.NotFound, HttpServletResponse.SC_NOT_FOUND);
		RESPONSE_CODES.put(ResponseCode.UnsupportedMediaType, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		RESPONSE_CODES.put(ResponseCode.MediaTypeNotAcceptable, HttpServletResponse.SC_NOT_ACCEPTABLE);
		RESPONSE_CODES.put(ResponseCode.BadRequest, HttpServletResponse.SC_BAD_REQUEST);
		RESPONSE_CODES.put(ResponseCode.BadResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // should never occur on server
		RESPONSE_CODES.put(ResponseCode.Ok, HttpServletResponse.SC_OK);
        RESPONSE_CODES.put(ResponseCode.CantWriteToSocket, HttpServletResponse.SC_OK); // We can't write it anyway...

		if (RESPONSE_CODES.size() != ResponseCode.values().length) {
			throw new IllegalStateException("Incorrect number of response codes mapped in http");
		}
	}

	public static void setResponseStatus(HttpServletResponse response, ResponseCode code) {
		response.setStatus(RESPONSE_CODES.get(code));
	}

	public static int getHttpResponseCode(ResponseCode code) {
		return RESPONSE_CODES.get(code);
	}

    public static ResponseCode getResponseCodeFromHttpCode(int httpResponseStatus, CougarFault fault) {
        if(httpResponseStatus == HttpServletResponse.SC_INTERNAL_SERVER_ERROR){
            if(fault.getErrorCode().startsWith(COUGAR_FAULT_PREFIX)){
                return ResponseCode.InternalError;
            }
            else{
                return ResponseCode.BusinessException;
            }
        }

        for (Map.Entry<ResponseCode, Integer> entry : RESPONSE_CODES.entrySet()) {
            if (entry.getValue() == httpResponseStatus) {
                return entry.getKey();
            }
        }
        return ResponseCode.InternalError;
    }
}
