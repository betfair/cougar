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

package com.betfair.testing.utils.cougar.misc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

/**
 * HttptestPageBean used to model an http page 
 *
 */
public class HttptestPageBean {
	private String requestedURL;
	private String pageText;
	
	private HttpResponse response;
	
	
	public HttpResponse getResponse() {
		return response;
	}
	public void setResponse(HttpResponse response) {
		this.response = response;
	}
	public String getRequestedURL() {
		return requestedURL;
	}
	public void setRequestedURL(String requestedURL) {
		this.requestedURL = requestedURL;
	}
	public String getPageText() {
		return pageText;
	}
	public void setPageText(String pageText) {
		this.pageText = pageText;
	}
	public String getWebResponseHeaderField(String fieldName){
        Header[] headers = response.getHeaders(fieldName);
        if (headers.length > 0) {
            return headers[0].getValue();
        }
        return null;
	}
	
}
