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

package com.betfair.application.util;


import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;

public class HttpCallable {

        private String name;
        private String restURL;
        private String soapEndpoint;
        private String rpcEndpoint;
        private int expectedHTTP;
        
        private String jsonRPCCall;

        private HttpBodyBuilder bodyJSON;
        private HttpBodyBuilder bodyXML;
        private HttpBodyBuilder bodySOAP;
        
        public HttpCallable(String name, String restURL, String soapEndpoint, int expectedHTTP, HttpBodyBuilder bodySOAP) {
            this.name = name;
            this.restURL = restURL;
            this.soapEndpoint = soapEndpoint;
            this.expectedHTTP = expectedHTTP;
            this.bodySOAP = bodySOAP;
        }
        
        public HttpCallable(String name, String url, String soapEndpoint, HttpBodyBuilder bodyJSON, HttpBodyBuilder bodyXML, HttpBodyBuilder bodySOAP){
            this(name, url, soapEndpoint, HttpStatus.SC_OK, bodySOAP);
            this.bodyJSON = bodyJSON;
            this.bodyXML = bodyXML;
        }
        
        public HttpCallable(String name, String url, String jsonRPCCall, int expectedHTTP){
            this(name, null, null, HttpStatus.SC_OK, null);
            this.rpcEndpoint = url;
            this.jsonRPCCall = jsonRPCCall;
            this.expectedHTTP = expectedHTTP;
        }

        public HttpUriRequest getMethod(String contentType, Object[] paramValues, int size, HttpCallLogEntry cle) {
            cle.setMethod(name);
            cle.setProtocol(contentType);
            if (contentType.equals("RPC")) {
                HttpPost pm = new HttpPost(rpcEndpoint);

                final ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonRPCCall.getBytes());
                final InputStreamEntity is = new InputStreamEntity(inputStream,inputStream.available());
                pm.addHeader("Content-Type", "application/json");
                pm.addHeader("Accept", "application/json");
                pm.addHeader("X-ExpectedReturnCode", String.valueOf(expectedHTTP));
                pm.setEntity(is);
                return pm;
            } else if (contentType.equals("SOAP")) {
                HttpPost pm = new HttpPost(soapEndpoint);

                final ByteArrayInputStream inputStream = bodySOAP.buildBodyToBytes(size);
                final InputStreamEntity is = new InputStreamEntity(inputStream,inputStream.available());
                pm.addHeader("Content-Type", "application/soap+xml");
                pm.addHeader("X-ExpectedReturnCode", String.valueOf(expectedHTTP));
                pm.setEntity(is);
                return pm;
            } else {
                if (bodyJSON != null) {
                    HttpPost pm = new HttpPost(String.format(restURL, paramValues));
                    InputStreamEntity is;
                    if (contentType.endsWith("json")) {
                        final ByteArrayInputStream inputStream = bodyJSON.buildBody(size);
                        is = new InputStreamEntity(inputStream,inputStream.available());
                    } else {
                        final ByteArrayInputStream inputStream = bodyXML.buildBody(size);
                        is = new InputStreamEntity(inputStream,inputStream.available());
                    }
                    pm.addHeader("Content-Type", contentType);
                    pm.addHeader("Accept", contentType);
                    pm.setEntity(is);
                    return pm;
                } else {
                    HttpGet gm = new HttpGet(String.format(restURL, paramValues));
                    gm.addHeader("Accept", contentType);
                    return gm;
                }
            }
        }

        public int expectedResult() {
           return expectedHTTP;
        }
        
        public String getName() {
        	return name;
        }
}
