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

import org.w3c.dom.Document;

public class HttpService {

	//Spring injected
	private static PageManager httptestPageManager = new PageManagerImpl();
	
	/**
	 * Load http page using passed url, page details set in returned bean.
	 * Overload to default whether an exception is thrown on an error response to true
	 * 
	 * @param url	URL of the page to be loaded
	 * @return
	 */
	public static HttptestPageBean loadPage(String url){
		return loadPage(url, true);
	}
	
	/**
	 * Load http page using passed url, page details set in returned bean.
	 * Specify whether or not to throw an exception when an error response is returned
	 * 
	 * @param url	URL of the page to be loaded
	 * @param exceptionOnError whether or not an exception should be thrown when an error response is returned
	 * @return
	 */
	public static HttptestPageBean loadPage(String url, boolean exceptionOnError) {
		HttptestPageBean httptestPageBean = new HttptestPageBean();
		httptestPageBean.setRequestedURL(url);
		httptestPageManager.getPage(httptestPageBean, exceptionOnError);
		return httptestPageBean;
	}
	
	/**
	 * Returns a copy of the domain object model tree associated with the page's response, 
	 * i.e. will return an HTML document if it is a HTML response.
	 * 
	 * @param httptestPageBean Bean populated when loading page in question
	 * @return
	 */
	public static Document getPageDom(HttptestPageBean httptestPageBean) {
		return httptestPageManager.getPageDOM(httptestPageBean);
	}
}
