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

package com.betfair.testing.utils.cougar.callmaker;


import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import org.apache.http.client.methods.HttpUriRequest;

public class RestGenericCallMaker extends AbstractCallMaker {

	private CougarHelpers cougarHelpers = new CougarHelpers();
	
	public CougarHelpers getCougarHelpers() {
		return cougarHelpers;
	}

	public void setCougarHelpers(CougarHelpers cougarHelpers) {
		this.cougarHelpers = cougarHelpers;
	}

	/* (non-Javadoc)
	 * @see com.betfair.testing.utils.cougar.callmaker.AbstractCallMaker#makeCall(com.betfair.testing.utils.cougar.beans.HttpCallBean, com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum)
	 */
	@Override
	public HttpResponseBean makeCall(HttpCallBean httpCallBean, CougarMessageContentTypeEnum responseContentTypeEnum) {

		CougarMessageProtocolRequestTypeEnum protocolRequestType = CougarMessageProtocolRequestTypeEnum.REST;
		CougarMessageContentTypeEnum requestContentTypeEnum = CougarMessageContentTypeEnum.OTHER;
				
		HttpUriRequest method = cougarHelpers.getRestMethod(httpCallBean, protocolRequestType);
		HttpResponseBean responseBean = cougarHelpers.makeRestCougarHTTPCall(httpCallBean, method, protocolRequestType, responseContentTypeEnum, requestContentTypeEnum);
		
		return responseBean;

	}

}
