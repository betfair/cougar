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

package com.betfair.testing.utils.cougar.manager;

import com.betfair.testing.utils.cougar.daos.ICougarDAO;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CougarTestDAO implements ICougarDAO {
	
	public List<HttpUriRequest> methods = new ArrayList<HttpUriRequest>();

	public org.apache.http.HttpResponse executeHttpMethodBaseCall(HttpUriRequest method) {
		// TODO Auto-generated method stub
		methods.add(method);
        final BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, ""));
        try {
            response.setEntity(new StringEntity("test"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return response;
	}

	public int executeHttpMethodCallSpoofIPAddress(HttpUriRequest method, String ipAddress) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}
