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

import javax.xml.soap.SOAPMessage;
import java.sql.Timestamp;
import java.util.Date;

public class SoapCallMaker extends AbstractCallMaker {

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
	public HttpResponseBean makeCall(HttpCallBean httpCallBean, CougarMessageContentTypeEnum requestContentTypeEnum) {

		CougarMessageProtocolRequestTypeEnum protocolRequestType = CougarMessageProtocolRequestTypeEnum.SOAP;
		
		SOAPMessage request = (SOAPMessage) httpCallBean.getPostQueryObjectsByEnum(protocolRequestType);
		
		Date requestTime = new Date();
		HttpResponseBean httpResponseBean = cougarHelpers.makeCougarSOAPCall(request, httpCallBean.getServiceName(), httpCallBean.getVersion(), httpCallBean);
		Date responseTime = new Date();

		httpResponseBean.setRequestTime(new Timestamp(requestTime.getTime()));
		httpResponseBean.setResponseTime(new Timestamp(responseTime.getTime()));

		return httpResponseBean;

	}
}
