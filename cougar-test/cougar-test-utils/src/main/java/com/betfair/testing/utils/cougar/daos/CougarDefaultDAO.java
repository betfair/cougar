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

package com.betfair.testing.utils.cougar.daos;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class CougarDefaultDAO implements ICougarDAO {
	
	private ThreadLocal<HttpClient> clients = new ThreadLocal<HttpClient>();
	
	public ThreadLocal<HttpClient> getClients() {
		return clients;
	}

	public void setClients(ThreadLocal<HttpClient> clients) {
		this.clients = clients;
	}

	/* (non-Javadoc)
	 * @see com.betfair.testing.utils.cougar.daos.ICougarDAO#executeHttpMethodBaseCall(org.apache.commons.httpclient.HttpMethodBase)
	 */
	public HttpResponse executeHttpMethodBaseCall(HttpUriRequest method) throws IOException {
        if (clients.get() == null) {
            clients.set(new DefaultHttpClient());
        }
        return clients.get().execute(method);
	}
		
}
