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

package com.betfair.cougar.client.api;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CougarRemoteFault {

	private LinkedHashMap<String, Object> faultMap;

	public CougarRemoteFault(LinkedHashMap<String, Object> faultMap) {
		this.faultMap = faultMap;
	}

	public String toString() {
		String ec = getErrorCode();
		return "ErrorCode: " + (ec.equals("") ? "N/A" : ec)  +", faultCode=" + getFaultCode() + ", faultString='" + getFaultString()+ "'" ;
	}

	public String getFaultCode() {
		return (String)faultMap.get("faultcode");
	}

	public String getFaultString() {
		return (String)faultMap.get("faultstring");
	}

	@SuppressWarnings("unchecked")
	public String getErrorCode() {

		Map<String,Object> detailMap  = (Map<String, Object>)faultMap.get("detail");
		if( detailMap != null ) {
			Collection<Object> detailValues = detailMap.values();

			if( detailValues.isEmpty() ) {
				return "";
			}

			Map<String, Object> exceptionMap= (Map<String, Object>) detailValues.iterator().next();

			String error = (String) exceptionMap.get("errorCode")+"";
			return error;
		}

		throw new IllegalStateException("Unrecognised fault - " + faultMap.toString());
	}
}
