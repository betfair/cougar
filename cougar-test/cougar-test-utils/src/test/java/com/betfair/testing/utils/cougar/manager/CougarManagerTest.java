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

import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.callmaker.AbstractCallMaker;
import com.betfair.testing.utils.cougar.callmaker.*;
import com.betfair.testing.utils.cougar.enums.*;
import com.betfair.testing.utils.cougar.helpers.CougarHelpers;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class CougarManagerTest {

	
	private CougarManager cougarManager = CougarManager.getInstance();
	private CougarHelpers cougarHelpers = new CougarHelpers();
	private CougarTestDAO cougarTestDAO = new CougarTestDAO();

	public CougarManagerTest() {
		
		cougarHelpers.setCougarDAO(cougarTestDAO);
		cougarManager.setCougarHelpers(cougarHelpers);
		
		RestJSONCallMaker restJSONCallMaker = new RestJSONCallMaker();
		restJSONCallMaker.setCougarHelpers(cougarHelpers);
		RestXMLCallMaker restXMLCallMaker = new RestXMLCallMaker();
		restXMLCallMaker.setCougarHelpers(cougarHelpers);
		SoapCallMaker soapXMLCallMaker = new SoapCallMaker();
				
		Map<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker> requestBuilderMap = new HashMap<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker>();
		
		requestBuilderMap.put(CougarMessageProtocolRequestTypeEnum.RESTJSON, restJSONCallMaker);
		requestBuilderMap.put(CougarMessageProtocolRequestTypeEnum.RESTXML, restXMLCallMaker);
		requestBuilderMap.put(CougarMessageProtocolRequestTypeEnum.SOAP, soapXMLCallMaker);
		
		CallMakerFactory.setRequestBuilderMap(requestBuilderMap);
		
		
	}

	@Test
	public void sendPostRestRequest_Test() throws ParserConfigurationException, SAXException, IOException {
		
		
		String POSTQUERY = "<ComplexObject><name>sum</name><value1>7</value1><value2>75</value2></ComplexObject>";
		
		//String expRestXMLRequestBody = "<ComplexObject xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><name>sum</name><value1>7</value1><value2>75</value2></ComplexObject>";
		String operationName = "someOperation";
		String requestWrapper = "SomeOperationRequest";
		
		String expRestXMLRequestBody = "<" + requestWrapper + " xmlns=\"http://www.betfair.com/servicetypes/v2/Baseline/\"><complexObject><name>sum</name><value1>7</value1><value2>75</value2></complexObject></" + requestWrapper + ">";
		
		//String expRestJSONRequestBody = "{\"name\":\"sum\",\"value1\":7,\"value2\":75}" ;
		String expRestJSONRequestBody = "{\"complexObject\":{\"name\":\"sum\",\"value1\":7,\"value2\":75}}" ;
			
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(POSTQUERY)));
			
		HttpCallBean httpCallBean = new HttpCallBean();
		
		
		httpCallBean.setOperationName(operationName);
		httpCallBean.setServiceName("Baseline");
		httpCallBean.setVersion("v2");
		httpCallBean.setQueryParams(null);
		httpCallBean.setHeaderParams(null);
		
		httpCallBean.setRestPostQueryObjects(document);
		
		cougarManager.makeRestCougarHTTPCalls(httpCallBean);
		
		List<HttpUriRequest> methodsSent = cougarTestDAO.methods;
		
		HttpPost methodSent;
		
		methodSent = (HttpPost)methodsSent.get(0);
		
		assertNull( methodSent.getURI().getQuery());
		
		assertEquals("/Baseline/v2/" + operationName, methodSent.getURI().getPath());
		
		Header[] headers = methodSent.getAllHeaders();
		assertEquals(4, headers.length);

		assertEquals("Content-Type: application/json", String.valueOf(headers[0]));
		assertEquals("User-Agent: java/socket", String.valueOf(headers[1]));
		assertEquals("Accept: application/json", String.valueOf(headers[2]));
		//Changed this from 37...
		//assertEquals("Content-Length: 55", String.valueOf(headers[3]));
		assertEquals("X-Forwarded-For: 87.248.113.14", String.valueOf(headers[3]));
		
		StringEntity stringRequestEntity = (StringEntity)methodSent.getEntity();
        InputStream inputStream = stringRequestEntity.getContent();
        byte[] buffer = new byte[inputStream.available()];
        int offset=0;
        int read;
        while ((read=inputStream.read(buffer,offset,inputStream.available()))!=-1) {
            offset+=read;
        }
        assertEquals(expRestJSONRequestBody, new String(buffer,"UTF-8"));
		
		methodSent = (HttpPost)methodsSent.get(2);
		
		assertNull( methodSent.getURI().getQuery());
		
		assertEquals("/Baseline/v2/" + operationName, methodSent.getURI().getPath());
		
		headers = methodSent.getAllHeaders();
		assertEquals(4, headers.length);
		
		stringRequestEntity = (StringEntity)methodSent.getEntity();
        inputStream = stringRequestEntity.getContent();
        buffer = new byte[inputStream.available()];
        offset=0;
        while ((read=inputStream.read(buffer,offset,inputStream.available()))!=-1) {
            offset+=read;
        }
		assertEquals(expRestXMLRequestBody, new String(buffer,"UTF-8"));

		assertEquals("Content-Type: application/xml", String.valueOf(headers[0]));
		assertEquals("User-Agent: java/socket", String.valueOf(headers[1]));
		assertEquals("Accept: application/xml", String.valueOf(headers[2]));
		
		//assertEquals("Content-Length: 141", String.valueOf(headers[3]));
		//assertEquals("Content-Length: 186", String.valueOf(headers[3]));
		
		assertEquals("X-Forwarded-For: 87.248.113.14", String.valueOf(headers[3]));
		

	}
	
	
	
	@Test
	public void sendGetRestRequest_Test() throws ParserConfigurationException, SAXException, IOException {
		
		Document document = null;
		
		HttpCallBean httpCallBean = new HttpCallBean();
		
		httpCallBean.setOperationName("complex");
		httpCallBean.setServiceName("rest");
		httpCallBean.setVersion("v2");
		
		Map<String, String> queryParams = new HashMap<String, String>(); 
		queryParams.put("queryParam", "qp1");
		httpCallBean.setQueryParams(queryParams);
		
		HashMap<String, String> headerParams = new HashMap<String, String>();
		headerParams.put("HeaderParam", "hp1");
		httpCallBean.setHeaderParams(headerParams);
		
		httpCallBean.setRestPostQueryObjects(document);
		
		cougarManager.makeRestCougarHTTPCalls(httpCallBean);
		
		List<HttpUriRequest> methodsSent = cougarTestDAO.methods;
		
		HttpGet methodSent;
		
		methodSent = (HttpGet)methodsSent.get(0);
		
		assertEquals("queryParam=qp1", methodSent.getURI().getQuery());
		
		assertEquals("/rest/v2/complex", methodSent.getURI().getPath());
		
		Header[] headers = methodSent.getAllHeaders();
		assertEquals(5, headers.length);

		assertEquals("Content-Type: application/json", String.valueOf(headers[0]));
		assertEquals("User-Agent: java/socket", String.valueOf(headers[1]));
		assertEquals("Accept: application/json", String.valueOf(headers[2]));
		assertEquals("HeaderParam: hp1", String.valueOf(headers[3]));
		assertEquals("X-Forwarded-For: 87.248.113.14", String.valueOf(headers[4]));
		
		methodSent = (HttpGet)methodsSent.get(2);
		
		assertEquals("queryParam=qp1", methodSent.getURI().getQuery());
		
		assertEquals("/rest/v2/complex", methodSent.getURI().getPath());
		
		headers = methodSent.getAllHeaders();
		assertEquals(5, headers.length);

		assertEquals("Content-Type: application/xml", String.valueOf(headers[0]));
		assertEquals("User-Agent: java/socket", String.valueOf(headers[1]));
		assertEquals("Accept: application/xml", String.valueOf(headers[2]));
		assertEquals("HeaderParam: hp1", String.valueOf(headers[3]));
		assertEquals("X-Forwarded-For: 87.248.113.14", String.valueOf(headers[4]));
	}
	
	@Test
	public void sortMapArrayByServiceVersion_Test(){
		
		Map<String,Object>[] mapArray = new HashMap[5];
		
		int[] insertOrder = new int[]{3,1,4,0,2};
		for(int i = 0; i < mapArray.length; i++){
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ServiceVersion", Double.valueOf(i));
			map.put("Operation", "testSimpleGet"+i);
			
			mapArray[insertOrder[i]] = map;
		}
		
		mapArray = cougarManager.sortRequestLogEntriesByServiceVersion(mapArray);
		
		for(int i = 0; i < mapArray.length; i++){
			assertEquals("testSimpleGet"+i, mapArray[i].get("Operation"));
			assertEquals(Double.valueOf(i), mapArray[i].get("ServiceVersion"));
		}
		
	}
}
