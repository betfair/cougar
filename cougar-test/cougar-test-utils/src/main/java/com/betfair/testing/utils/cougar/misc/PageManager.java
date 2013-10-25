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

public interface PageManager {

	/**
	 * Load a page based on passed bean (default exception on error response to true)
	 */
	public abstract void getPage(HttptestPageBean bean);

	/**
	 * Load a page based on passed bean, specifying whether or not to throw an exception when an error response is returned
	 */
	public abstract void getPage(HttptestPageBean bean, boolean exceptionOnError);

	/**
	 * Returns a copy of the domain object model tree associated with the page's response,
	 * i.e. will return an HTML document if it is a HTML response.
	 */
	public abstract Document getPageDOM(HttptestPageBean page);

}