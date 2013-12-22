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

package com.betfair.testing.utils.cougar.beans;

import com.betfair.testing.utils.JSONHelpers;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum;
import com.betfair.testing.utils.cougar.helpers.SOAPRequestBuilder;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HttpCallBean {

	private String operationName;
	private String serviceName;
	private String serviceExtension;
	private String version;
	private Map<CougarMessageProtocolRequestTypeEnum, Object> postQueryObjects = new HashMap<CougarMessageProtocolRequestTypeEnum,Object>();
	private Map<String, String> pathParams = null;
	private Map<String, String> queryParams = null;
	private List<BatchedRequestBean> batchedRequests = null;
	private Map<String,String> authCredentials = null;
	private String authority = null;
	private String subject = null;
	private Map<String, String> headerParams = null;
	private JSONHelpers jHelpers = new JSONHelpers();
	private XMLHelpers xHelpers = new XMLHelpers();
	
	private Map<CougarMessageProtocolResponseTypeEnum, HttpResponseBean> responses = new HashMap<CougarMessageProtocolResponseTypeEnum, HttpResponseBean>();
		
	private String baseNameSpace = "http://www.betfair.com/servicetypes";
	private String nameSpaceVersion;
	private String nameSpaceServiceName;
	
	private Map<String, String> acceptProtocols = new HashMap<String, String>();
	
	private String ipAddress = "87.248.113.14";
	private String host = "localhost"; // Default value (can be overridden in tests)
	private String port = "8080"; // Default value (can be overridden in tests)
	private String alternativeURL = ""; // Default value to point requests to standard url. Set this parameter to "/www" in the test to use the alternative operation url
	private String path;
	private boolean jsonRPC = false; // Set to true in tests if making a batched JSON query
	
//	private IUtilityLogger logger = UtilityLoggerFactory.getLogger();
	
	private static final int NOTFOUND_STATUS_CODE = 404;
    private String fullPath;

    public String getNameSpace() {
		if (nameSpaceServiceName==null || nameSpaceVersion==null) {
			throw new RuntimeException ("serviceName and version must be set before retrieving nameSpace");
		} else {
			return baseNameSpace + "/" + nameSpaceVersion + "/" + nameSpaceServiceName + "/";
		}
	}
	public void setNameSpace(String nameSpace) {
		this.baseNameSpace = nameSpace;
	}
	public Map<String, String> getQueryParams() {
		return queryParams;
	}
	public void setQueryParams(Map<String, String> queryParam) {
		this.queryParams = queryParam;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
		this.nameSpaceVersion = version.substring(0,2);
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
		this.setPath(operationName);
	}
	// Overload for setting the path to be different to the operation name
	public void setOperationName(String operationName, String path){
		this.operationName = operationName;
		this.setPath(path);
	}
	public Map<String, String> getHeaderParams() {
		return headerParams;
	}
	public void setHeaderParams(Map<String, String> headerParams) {
		this.headerParams = headerParams;
	}
	public Map<String, String> getPathParams() {
		return pathParams;
	}
	public void setPathParams(Map<String, String> pathParams) {
		this.pathParams = pathParams;
	}
	public Map<CougarMessageProtocolRequestTypeEnum,Object> getPostQueryObjects() {
		return postQueryObjects;
	}
	public Object getPostQueryObjectsByEnum(CougarMessageProtocolRequestTypeEnum protocolRequestType) {
		if (postQueryObjects==null) {
			return null;
		} else {
			return postQueryObjects.get(protocolRequestType);
		}
	}

	public void setRestPostQueryObjects(Document document) {
		
		if (document==null) {
			postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.RESTJSON, null);
			postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.RESTXML, null);
		} else {
			Document newDocument = null;
			try {
				/* 
				 * Changes to the existing cougar interface - now all REST requests are wrapped in 
				 * OperationNameRequest tag. This wraps existing test inputs to conform to new standards. 
				 */
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				newDocument = builder.newDocument();	
					
				String builtServiceName = operationName.substring(0,1).toUpperCase(Locale.ENGLISH) +operationName.substring(1) + "Request";

				Element root = (Element)newDocument.createElement(builtServiceName); 
				newDocument.appendChild(root);
				
				/* 
				 * Due to changes in the main cougar engine where it expects the operation names
				 * to start in lowercase, a bit of string handling to convert old tests' XML to 
				 * conform to the new standard. 
				 */
				XMLHelpers helper = new XMLHelpers();
				String tmp = document.getFirstChild().getNodeName();
				helper.renameRootElement(document, tmp.substring(0,1).toLowerCase(Locale.ENGLISH) + tmp.substring(1));
			    root.appendChild( newDocument.importNode(document.getDocumentElement(), true)  );
			    
			}catch (ParserConfigurationException e) {
			//	logger.LogBetfairDebugEntry("Parser Error Setting REST PostQueryObject. " + e.getMessage());
			}	
				
			JSONObject jsonRequest;
			String xmlString;
			
			if(newDocument == null){ // New Document was not created for some reason so use original document
				jsonRequest = jHelpers.convertXMLDocumentToJSONObjectRemoveRootElement(document);
				
				document.getDocumentElement().setAttribute("xmlns", getNameSpace());
				xmlString = xHelpers.getXMLAsString(document);
			}
			else{
				jsonRequest = jHelpers.convertXMLDocumentToJSONObjectRemoveRootElement(newDocument);
				
				newDocument.getDocumentElement().setAttribute("xmlns", getNameSpace());
				xmlString = xHelpers.getXMLAsString(newDocument);
			}
			
			jHelpers.removeJSONObjectHoldingSameTypeList(jsonRequest);
			String jsonString = jsonRequest.toString();
			
			postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.RESTJSON, jsonString);					
			postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.RESTXML, xmlString.split("\\?>")[1]);
		}
	}
	
	/**
	 * Takes xml doc as arg that consists is fragment of SOAPMessasge Body
	 * 
	 * @param document
	 */
	public void setSoapPostQueryObjects(Document document) 
	{
		SOAPRequestBuilder requestBuilder = new SOAPRequestBuilder();
		
		postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.SOAP, requestBuilder.buildSOAPRequest(document, this));
	}
	
	
	/**
	 * Used by concurrency test and take a complete SOAPMessage
	 * 
	 * @param soapMessage
	 */
	public void setSOAPMessage(SOAPMessage soapMessage)
	{
		postQueryObjects.put(CougarMessageProtocolRequestTypeEnum.SOAP, soapMessage);
	}
	
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Map<CougarMessageProtocolResponseTypeEnum, HttpResponseBean> getResponses() {
		return responses;
	}
	
	public HttpResponseBean getResponseObjectsByEnum(CougarMessageProtocolResponseTypeEnum protocolResponseType) {
		//return responses.get(CougarMessageProtocolResponseTypeEnum.valueOf(protocolResponseType.toString()));
		switch (protocolResponseType) {
		case SOAP:
			return responses.get(protocolResponseType);
			
		case RESTJSONJSON:
		case RESTXMLJSON:
			return createRestJSONResponseBean(protocolResponseType);
			
		case RESTJSONXML:
		case RESTXMLXML:
			return createRestXMLResponseBean(protocolResponseType);
			
		case REST:
		default:
			return createRestDefaultResponseBean(protocolResponseType);
		}
	}
	
	private HttpResponseBean createRestJSONResponseBean(CougarMessageProtocolResponseTypeEnum protocolResponseType){
		
		HttpResponseBean jsonResponseBean = responses.get(protocolResponseType);
		String responseString = (String)jsonResponseBean.getResponseObject();
		if (jsonResponseBean.getHttpStatusCode()==NOTFOUND_STATUS_CODE && responseString.contains("html")) { // If not found html page then create as string
			responseString = responseString.replace("\r", ""); // Clean string of escaped characters for assertion
			responseString = responseString.replace("\n", "");
			responseString = responseString.replace("\t", "");
			jsonResponseBean.setResponseObject(responseString);
			return jsonResponseBean;
		} else {
			HttpResponseBean returnJsonResponseBean = new HttpResponseBean();
			JSONObject castedJsonResponseObject;
			if ((responseString==null) || (responseString.equalsIgnoreCase(""))) {
				castedJsonResponseObject = null;
			}
			else {
				try {
					//if the returned result does not contain key-value - e.g. enum
					if(!responseString.substring(0,1).equals("{")){
						responseString = "{response:" + responseString + "}";
					}
					castedJsonResponseObject = jHelpers.parseJSONObjectFromJSONString(responseString);
				} catch (JSONException e) {
					throw new RuntimeException ("Response is an invalid JSON String", e);
				}
			}
			BeanUtils.copyProperties(jsonResponseBean, returnJsonResponseBean);
			returnJsonResponseBean.setResponseObject(castedJsonResponseObject);
			return returnJsonResponseBean;
		}
	}
	
	private HttpResponseBean createRestXMLResponseBean(CougarMessageProtocolResponseTypeEnum protocolResponseType){
		
		HttpResponseBean xmlResponseBean = responses.get(protocolResponseType);
		String responseString = (String)xmlResponseBean.getResponseObject();
		if (xmlResponseBean.getHttpStatusCode()==NOTFOUND_STATUS_CODE && responseString.contains(("html"))) { // If not found html page then create as string			
			responseString = responseString.replace("\r", ""); // Clean string of escaped characters for assertion
			responseString = responseString.replace("\n", "");
			responseString = responseString.replace("\t", "");
			xmlResponseBean.setResponseObject(responseString);
			return xmlResponseBean;
		} else {
			HttpResponseBean returnXmlResponseBean = new HttpResponseBean();
			Document castedXmlResponseObject;
			if ((responseString==null) || (responseString.equalsIgnoreCase(""))) {
				castedXmlResponseObject = null;
			} else{			
				castedXmlResponseObject = xHelpers.getXMLObjectFromString(responseString);
			}
			BeanUtils.copyProperties(xmlResponseBean, returnXmlResponseBean);
			returnXmlResponseBean.setResponseObject(castedXmlResponseObject);
			return returnXmlResponseBean;
		}
	}
	
	private HttpResponseBean createRestDefaultResponseBean(CougarMessageProtocolResponseTypeEnum protocolResponseType){
		HttpResponseBean unknownResponseBean = responses.get(protocolResponseType);
		HttpResponseBean returnUnknownResponseBean = new HttpResponseBean();
		String unknownResponseString = (String)unknownResponseBean.getResponseObject();
		Object castedResponseObject;
		if ((unknownResponseString==null) || (unknownResponseString.equalsIgnoreCase(""))) {
			castedResponseObject=null;
		} else {
			try {
				castedResponseObject = new JSONObject(unknownResponseString);
			} catch (JSONException e) {
				castedResponseObject = xHelpers.getXMLObjectFromString(unknownResponseString);
			}
		}
		BeanUtils.copyProperties(unknownResponseBean, returnUnknownResponseBean);
		returnUnknownResponseBean.setResponseObject(castedResponseObject);
		return returnUnknownResponseBean;
	}
	public void setResponseByEnum(CougarMessageProtocolResponseTypeEnum protocolResponseType, HttpResponseBean response) {
		responses.put(protocolResponseType, response);
	}
	
	/**
	 * Allow request object to be set for given protocol
	 * @param document
	 * @param protocolRequestType
	 */
	public void setPostObjectForRequestType(Document document, Object protocolRequestType)
	{
		postQueryObjects = new HashMap<CougarMessageProtocolRequestTypeEnum,Object>();
		
		CougarMessageProtocolRequestTypeEnum type = CougarMessageProtocolRequestTypeEnum.valueOf(protocolRequestType.toString());
		
		switch(type)
		{
		case RESTXML:
			document.getDocumentElement().setAttribute("xmlns",	getNameSpace());
			String xmlString = xHelpers.getXMLAsString(document);
			postQueryObjects.put(type, xmlString);
			break;
		case RESTJSON:
			JSONObject jsonRequest = jHelpers.convertXMLDocumentToJSONObjectRemoveRootElement(document);
			jHelpers.removeJSONObjectHoldingSameTypeList(jsonRequest);
			String jsonString = jsonRequest.toString();
			postQueryObjects.put(type, jsonString);
			break;
		case SOAP:
			this.setSoapPostQueryObjects(document);
			break;
		default:
			throw new RuntimeException ("I dont know how to handle : " + type);
		}
	}
	public String getServiceName() {
		return serviceName;
	}
	public String getServiceExtension(){
		return serviceExtension;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
		this.serviceExtension = serviceName;
		this.nameSpaceServiceName = Character.toUpperCase(serviceName.charAt(0)) + serviceName.substring(1);
	}
	// Overload for when service extension is different to service name
	public void setServiceName(String serviceName, String serviceExtension){
		this.serviceName = serviceName;
		this.serviceExtension = serviceExtension;
		this.nameSpaceServiceName = Character.toUpperCase(serviceName.charAt(0)) + serviceName.substring(1);
	}
	
	public Map<String, String> getAcceptProtocols() {
		return acceptProtocols;
	}
	public void setAcceptProtocols(Map<String, String> acceptProtocols) {
		this.acceptProtocols = acceptProtocols;
	}
	public void addAcceptProtocol(String protocol, Integer ranking) {
		this.acceptProtocols.put(protocol, "q="+ranking);
	}
	public void setPostQueryObjects(
			Map<Object, Object> postQueryObjects) {
		
		Map<CougarMessageProtocolRequestTypeEnum, Object> castedMap = new HashMap<CougarMessageProtocolRequestTypeEnum, Object>();
		CougarMessageProtocolRequestTypeEnum newKey;
		Object value;
		for (Map.Entry<Object, Object> entry:postQueryObjects.entrySet()) {
			Object key = entry.getKey();
			if (key.getClass()!=CougarMessageProtocolRequestTypeEnum.class) {
				newKey = CougarMessageProtocolRequestTypeEnum.valueOf(key.toString());
				value = entry.getValue();
				castedMap.put(newKey, value);
			}
		}
		
		this.postQueryObjects = castedMap;
	}
	public String getNameSpaceServiceName() {
		return nameSpaceServiceName;
	}
	public void setNameSpaceServiceName(String nameSpaceServiceName) {
		this.nameSpaceServiceName = nameSpaceServiceName;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getHost(){
		return host;
	}
	public void setHost(String host){
		this.host = host;
	}
	public String getPort(){
		return port;
	}
	public void setPort(String port){
		this.port = port;
	}
	public String getPath(){
		return path;
	}
	public void setPath(String path){
		this.path = path;
	}
	public String getAlternativeURL(){
		return alternativeURL;
	}
	public void setAlternativeURL(String altURL){
		this.alternativeURL = altURL;
	}
	public void setAuthCredentials(Map<String,String> authCredentials) {
		this.authCredentials = authCredentials;
	}
	public Map<String,String> getAuthCredentials() {
		return authCredentials;
	}
	public void setJSONRPC(boolean batching){
		jsonRPC = batching;
	}
	public boolean getJSONRPC(){
		return jsonRPC;
	}
	public void setBatchedRequests(Map<String,String>[] requests){
		List<BatchedRequestBean> batch  = new ArrayList<BatchedRequestBean>();
		for(int i = 0; i < requests.length; i++){
			BatchedRequestBean request = new BatchedRequestBean();
			
			request.setMethod(requests[i].get("method"));
			request.setParams(requests[i].get("params"));
			request.setId(requests[i].get("id"));
			request.setVersion(requests[i].containsKey("version")? requests[i].get("version"):"2.0"); //if version not set - default to "2.0"
			request.setService(requests[i].containsKey("service")? requests[i].get("service"):"Baseline"); //if service not set - default to "Baseline"
		
			batch.add(request);
		}
		this.batchedRequests = batch;
	}
	public void setBatchedRequestsDirect(List<BatchedRequestBean> requests){
		this.batchedRequests = requests;
	}
	public List<BatchedRequestBean> getBatchedRequests(){
		return batchedRequests;
	}

    public String getFullPath() {
        return fullPath;
    }

    // allows overriding of the full request path (for aliases etc)
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
