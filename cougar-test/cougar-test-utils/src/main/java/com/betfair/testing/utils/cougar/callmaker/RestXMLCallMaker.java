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
import com.betfair.testing.utils.cougar.beans.*;
import com.betfair.testing.utils.cougar.enums.*;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.apache.http.client.methods.HttpUriRequest;

public class RestXMLCallMaker extends AbstractCallMaker{
	
	private XMLHelpers xmlHelpers = new XMLHelpers();
	private CougarHelpers cougarHelpers = new CougarHelpers();

	/*
	 * Method will make a REST XML call to the specified baseline-app service, running
	 * locally, and returns the response body as a Document.
	 * 
	 * (non-Javadoc)
	 * @see com.betfair.testing.utils.cougar.callmaker.AbstractCallMaker#makeCall(com.betfair.testing.utils.cougar.beans.HttpCallBean)
	 */
	public HttpResponseBean makeCall(HttpCallBean httpCallBean, CougarMessageContentTypeEnum responseContentTypeEnum) {
	
		CougarMessageProtocolRequestTypeEnum protocolRequestType = CougarMessageProtocolRequestTypeEnum.RESTXML;
		CougarMessageContentTypeEnum requestContentTypeEnum = CougarMessageContentTypeEnum.XML;
				
		HttpUriRequest method = cougarHelpers.getRestMethod(httpCallBean, protocolRequestType);
		HttpResponseBean responseBean = cougarHelpers.makeRestCougarHTTPCall(httpCallBean, method, protocolRequestType, responseContentTypeEnum, requestContentTypeEnum);
		
		return responseBean;
	}

	public CougarHelpers getCougarHelpers() {
		return cougarHelpers;
	}

	public void setCougarHelpers(CougarHelpers cougarHelpers) {
		this.cougarHelpers = cougarHelpers;
	}
	
	public XMLHelpers getXmlHelpers() {
		return xmlHelpers;
	}

	public void setXmlHelpers(XMLHelpers xmlHelpers) {
		this.xmlHelpers = xmlHelpers;
	}
	
}
