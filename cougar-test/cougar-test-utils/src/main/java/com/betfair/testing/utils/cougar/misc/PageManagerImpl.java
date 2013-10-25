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

import com.betfair.testing.utils.cougar.helpers.HttpHelpers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PageManagerImpl implements PageManager {

    private HttpClient httpClient;

    public PageManagerImpl() {
        httpClient = HttpClientBuilder
                .create()
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .build();
    }

    /* (non-Javadoc)
         * @see com.betfair.jett.utils.httptest.manager.PageManager#getPage(com.betfair.jett.utils.httptest.manager.HttptestPageBean)
         */
	public void getPage(HttptestPageBean bean) {
		getPage(bean, true);

	}

	/* (non-Javadoc)
	 * @see com.betfair.jett.utils.httptest.manager.PageManager#getPage(com.betfair.jett.utils.httptest.manager.HttptestPageBean)
	 */
	public void getPage(HttptestPageBean bean, boolean exceptionOnError) {

        HttpGet req = new HttpGet(bean.getRequestedURL());

		try {
            HttpResponse resp = httpClient.execute(req);
            if (resp.getStatusLine().getStatusCode() >= 400 && exceptionOnError) {
                throw new IllegalStateException("Error received, status line: "+resp.getStatusLine());
            }
			setPageDetails(bean, resp);
		} catch (Exception e) {
			throw new RuntimeException("Page was not loaded", e);
		}

	}

	private void setPageDetails(HttptestPageBean bean, HttpResponse newResp) throws IOException {
		bean.setResponse(newResp);
        HttpEntity entity = newResp.getEntity();
        bean.setPageText(EntityUtils.toString(entity));
	}

	/* (non-Javadoc)
	 * @see com.betfair.jett.utils.httptest.manager.PageManager#getPageDOM(com.betfair.jett.utils.httptest.manager.HttptestPageBean)
	 */
	public Document getPageDOM(HttptestPageBean page) {
        System.out.println(page.getPageText());
        return new HttpHelpers().parseInputStream(new ByteArrayInputStream(page.getPageText().getBytes()));
    }

}
