/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.testing.utils.cougar.helpers;

import com.betfair.testing.utils.cougar.beans.BatchedRequestBean;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.daos.CougarDefaultDAO;
import com.betfair.testing.utils.cougar.daos.ICougarDAO;
import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.enums.CougarMessageProtocolRequestTypeEnum;
import com.betfair.testing.utils.cougar.misc.IReflect;
import com.betfair.testing.utils.cougar.misc.Reflect;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.xerces.dom.DocumentImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CougarHelpers {

    private static final Logger logger = LoggerFactory.getLogger(CougarHelpers.class);
	private ICougarDAO cougarDAO = new CougarDefaultDAO();

	private IReflect reflect = new Reflect();

  	private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

  	private static final String SOAP_CALL_TEXT = "Make Cougar SOAP Call : ";
  	private static final String JMX_SETTING_ERROR = "Problem setting JMX attribute: ";
  	private static final String JMX_RETRIEVAL_ERROR = "Problem retrieving JMX attribute value: ";
  	private static final String JMX_INVOKE_ERROR = "Problem invoking JMX operation: ";
  	private static final String JSON_CONTENT = "application/json";
  	private static final String XML_CONTENT = "application/xml";

  	private static final String UTF8 = "utf-8";

  	// Map of operation names to paths (where different)
  	// TODO Would be better to generate this automatically
  	private static final Map<String, String> OPERATION_PATHS = new HashMap<String, String>(){
		{
			put("testSimpleMapGet","simpleMapGet");
			put("testSimpleListGet","simpleListGet");
			put("testSimpleSetGet","simpleSetGet");
			put("testSimpleGet","simple");
			put("testSimpleCacheGet","cache");
			put("testLargeGet","largeGet");
			put("testLargeMapGet","map");
			put("testMapsNameClash","map1");
			put("testGetTimeout","simple/timeout");
			put("testParameterStyles","styles");
			put("testDateRetrieval","dates");
			put("testDoubleHandling","doubles");
			put("testListRetrieval","primitiveLists");
			put("testComplexMutator","complex");
			put("testLargePost","largePost");
			put("testException","exception");
			put("testSecureService","secure");
			put("testSimpleTypeReplacement","simpletypes");
			put("testStringableLists","simpleLists");
			put("testBodyParams","multibody");
			put("testNoParams","noparams");
			put("testDirectMapReturn","direct/map");
			put("testDirectListReturn","direct/list");
			put("boolSimpleTypeEcho","boolEcho");
			put("byteSimpleTypeEcho","byteEcho");
			put("i32SimpleTypeEcho","i32Echo");
			put("i64SimpleTypeEcho","i64Echo");
			put("floatSimpleTypeEcho","floatEcho");
			put("doubleSimpleTypeEcho","doubleEcho");
			put("stringSimpleTypeEcho","stringEcho");
			put("dateTimeSimpleTypeEcho","dateTimeEcho");
			put("i32ListSimpleTypeEcho","i32ListEcho");
			put("i32SetSimpleTypeEcho","i32SetEcho");
			put("i32MapSimpleTypeEcho","i32MapEcho");
			put("testIdentityChain","identityChain");
			put("getDetailedHealthStatus","detailed");
			put("isHealthy","summary");
		}
  	};

	private JMXConnector jmxc = null;

	/*
	 * Send a request to a locally running Cougar container via SOAP as per the
	 * passed parameters.
	 *
	 * @param message
	 * @param serviceName
	 * @param version
	 * @param httpBean
	 * @return
	 */
	public HttpResponseBean makeCougarSOAPCall(SOAPMessage message,
			String serviceName, String version, HttpCallBean httpBean) {
		try {

			//Debugging code
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			message.writeTo(outStream);
			SOAPConnectionFactory connectionFactory = SOAPConnectionFactory
					.newInstance();
			SOAPConnection connection = connectionFactory.createConnection();

			// this can either be a SOAPException or SOAPMessage
			HttpResponseBean responseBean = new HttpResponseBean();
			Object response;

			String host = httpBean.getHost();
			String port = httpBean.getPort();

			String endPoint = "http://" + host + ":" + port + "/" + serviceName + "Service/" + version;

			try {

				response = connection.call(message, endPoint);

			} catch (SOAPException e) {
				response = e;
			} finally {
				connection.close();
			}

			responseBean.setResponseObject(handleResponse(response, responseBean));

			return responseBean;

		} catch (SOAPException | IOException | ParserConfigurationException | TransformerException e) {
			throw new RuntimeException(SOAP_CALL_TEXT + e, e);
		}

    }

	/*
	 * Handle the response of the SOAP call
	 *
	 * @param response
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private Object handleResponse(Object response, HttpResponseBean responseBean) throws SOAPException,
			IOException, ParserConfigurationException, TransformerException {

		Class<?> clazz = response.getClass();

		if (clazz
				.getName()
				.equalsIgnoreCase(
						"com.sun.xml.internal.messaging.saaj.soap.ver1_1.Message1_1Impl")
				|| clazz
						.getName()
						.equalsIgnoreCase(
								"com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl")) {

			return handleSoapResponse((SOAPMessage) response, responseBean);
		}
		else{ // else assume that an exception has been thrown
			return response;
		}

	}

	/*
	 * ???
	 *
	 * @param response
	 * @param responseBean
	 * @throws DOMException
	 * @throws SOAPException
	 */
	private void extractHeaderDataSOAP(SOAPMessage response, HttpResponseBean responseBean) throws SOAPException
	{
		//extract MimeHeaders
		MimeHeaders mime = response.getMimeHeaders();
		Iterator<MimeHeader> iter = mime.getAllHeaders();

		while(iter.hasNext())
		{
			MimeHeader mimeH = iter.next();
			responseBean.addEntryToResponseHeaders(mimeH.getName(),mimeH.getValue());

		}

		//extract SOAPHeaders from the envelope and a them to the mimeHeaders
		if(response.getSOAPHeader()!=null)
		{
			javax.xml.soap.SOAPHeader header = response.getSOAPHeader();

			NodeList nodes = header.getChildNodes();


			for(int x=0; x<nodes.getLength();x++)
			{
				//if the header entry contains child nodes - write them with the node names
				if(nodes.item(x).hasChildNodes()){
					NodeList childnodes = nodes.item(x).getChildNodes();
					for(int y = 0; y<childnodes.getLength();y++){
                        responseBean.addEntryToResponseHeaders(nodes.item(x).getLocalName(),childnodes.item(y).getLocalName()+":"+childnodes.item(y).getTextContent());
                    }
				}
				else{
					responseBean.addEntryToResponseHeaders(nodes.item(x).getLocalName(), nodes.item(x).getTextContent());
				}
			}
		}
	}

	/*
	 * ???
	 *
	 * @param response
	 * @param responseBean
	 * @return
	 * @throws TransformerException
	 * @throws SOAPException
	 * @throws ParserConfigurationException
	 */
	private Document handleSoapResponse(SOAPMessage response, HttpResponseBean responseBean)
			throws TransformerException, SOAPException,
			ParserConfigurationException {

		Node responseNode = null;

		if (response!=null) {
			responseNode = extractResponseNode(response);
			extractHeaderDataSOAP(response, responseBean);
		}

		// build new xml document for assertion
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document newDocument = builder.newDocument();

		Node adoptedBlob = newDocument.importNode(responseNode, true);
		newDocument.appendChild(adoptedBlob);

		// Output as String if required
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		transformer.transform(new DOMSource(newDocument), new StreamResult(out));

        if (logger.isDebugEnabled()) {
            logger.debug("\n Return Doc \n");
            logger.debug(new String(out.toByteArray()));
        }

		return newDocument;
	}

	public Node extractResponseNode(SOAPMessage response) throws SOAPException{

		Node responseNode;

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			response.writeTo(outStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		logger.LogBetfairDebugEntry("SOAP Response: " + outStream.toString());

		if (response.getSOAPBody().hasFault()) {
			responseNode = response.getSOAPBody().getFault();
		}
		else if(response.getSOAPBody().getFirstChild() == null){ // Response type is void
			responseNode = response.getSOAPBody();
		}
		else {
			// extract the body
			SOAPBody respBody = response.getSOAPBody();
			// First child should be the service name object
			Node serviceResponseNode = respBody.getFirstChild();

			// second child
			responseNode = serviceResponseNode.getFirstChild();
		}

		return responseNode;
	}

	/*
	 * Create and return a HttpMethodBase for a Rest request based on the passed
	 * HttpCallBean and CougarMessageProtocolRequestTypeEnum.
	 *
	 * @param httpCallBean
	 * @param protocolRequestType
	 * @return
	 */
	public HttpUriRequest getRestMethod(HttpCallBean httpCallBean, CougarMessageProtocolRequestTypeEnum protocolRequestType) {

		Object postQueryObject = httpCallBean.getPostQueryObjectsByEnum(protocolRequestType);
		String postQuery;

		if (postQueryObject == null) {
			postQuery = null;
		} else {
			postQuery = (String)postQueryObject;
		}

		String serviceExtension = httpCallBean.getServiceExtension();
		String version = httpCallBean.getVersion();
		Map<String, String> queryParams = httpCallBean.getQueryParams();

		String host = httpCallBean.getHost();
		String port = httpCallBean.getPort();
		String path = httpCallBean.getPath();
		String altURL = httpCallBean.getAlternativeURL(); // Will be "" for standard URL, or "/www" for alternative URL
		boolean batchedQuery = httpCallBean.getJSONRPC();
        String fullPath = httpCallBean.getFullPath();

		String methodURL = "";

		if(batchedQuery){ // If request is a batched JSON request set the URL to the appropriate baseline URL
			postQuery = createBatchedPostString(httpCallBean.getBatchedRequests()); // Build the post string out of the list of requests to batch
			methodURL = "http://" + host + ":" + port + "/json-rpc";
		}
		else{ // else build the request URL

			String queryParamString = "";
			if (queryParams != null) {
				int counter = 0;
				StringBuilder queryBuff = new StringBuilder();
				for (Map.Entry<String,String> entry : queryParams.entrySet()) {
					String value = entry.getValue();
					String key = entry.getKey();
					if (counter == 0) {
						String firstQuery = "?" + key + "=" + value;
						queryBuff.append(firstQuery);
					} else {
						String nextQuery = "&" + key + "=" + value;
						queryBuff.append(nextQuery);
					}
					counter++;
				}
				queryParamString = queryBuff.toString();
			}

            if (fullPath != null) {
                methodURL = "http://" + host + ":" + port + fullPath + queryParamString;
            }
            else {
                methodURL = "http://" + host + ":" + port + altURL + "/" + serviceExtension + "/" + version + "/"
                            + path + queryParamString;
            }
		}

//        if (logger.isDebugEnabled()) {
//        }

		if ((postQuery != null) && (!postQuery.equalsIgnoreCase(""))) {
			HttpPost method = new HttpPost(methodURL);
			try {
				method.setEntity(new StringEntity(postQuery,
                        null, UTF8));
				return method;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new HttpGet(methodURL);
		}
	}

	private String createBatchedPostString(List<BatchedRequestBean> requests){

		StringBuilder postQuery = new StringBuilder();
		postQuery.append("[");

		for(BatchedRequestBean entry : requests){ // build each request String and add to post string
			//if version not set - default to "2.0", if service not set - default to "Baseline"
			String version = entry.getVersion();
			String service = entry.getService();
			String method = entry.getMethod();
			String params = entry.getParams();
			String id = entry.getId();
			postQuery.append("{ \"jsonrpc\": \"").append(version).append("\", \"method\": \"").append(service).append("/v")
                    .append(version).append("/").append(method).append("\", \"params\": ").append(params).append(", \"id\": ").append(id).append("}");
			postQuery.append(",");
		}

		postQuery.deleteCharAt(postQuery.length()-1); // Remove last comma
		postQuery.append("]");

		return postQuery.toString();
	}

	/**
	 * Send a request to a locally running Cougar container via REST as per the
	 * passed parameters.
	 */
	public HttpResponseBean makeRestCougarHTTPCall(HttpCallBean httpCallBean, HttpUriRequest method, CougarMessageProtocolRequestTypeEnum protocolRequestType, CougarMessageContentTypeEnum responseContentTypeEnum, CougarMessageContentTypeEnum requestContentTypeEnum) {

		Map<String, String> headerParams = httpCallBean.getHeaderParams();
		String authority = httpCallBean.getAuthority();
		Map <String, String> authCredentials = httpCallBean.getAuthCredentials();
		Map<String, String> acceptProtocols = httpCallBean.getAcceptProtocols();
		String ipAddress = httpCallBean.getIpAddress();
		String altUrl = httpCallBean.getAlternativeURL();

		Object postQueryObject = httpCallBean.getPostQueryObjectsByEnum(protocolRequestType);
		String postQuery;
		if (postQueryObject == null) {
			postQuery = null;
		} else {
			postQuery = (String)postQueryObject;
		}

        InputStream inputStream = null;
		try {
			completeRestMethodBuild(method, responseContentTypeEnum, requestContentTypeEnum, postQuery, headerParams, authority, authCredentials,altUrl, acceptProtocols, ipAddress);

            if (logger.isDebugEnabled()) {
                logger.debug("Request");
                logger.debug("=======");
                logger.debug("URI: '" + method.getURI() + "'");
                Header[] headers = method.getAllHeaders();
                for (Header h : headers) {
                    logger.debug("Header: '"+h.getName()+" = "+h.getValue()+"'");
                }
                logger.debug("Body:    '" + postQuery + "'");
            }

			Date requestTime = new Date();
            final HttpResponse httpResponse = cougarDAO.executeHttpMethodBaseCall(method);
            inputStream = httpResponse.getEntity().getContent();

            String response = buildResponseString(inputStream);

            if (logger.isDebugEnabled()) {
                logger.debug("Response");
                logger.debug("========");
                logger.debug(String.valueOf(httpResponse.getStatusLine()));
                Header[] headers = httpResponse.getAllHeaders();
                for (Header h : headers) {
                    logger.debug("Header: '"+h.getName()+" = "+h.getValue()+"'");
                }
                logger.debug("Body:    '" + response + "'");
            }

			Date responseTime = new Date();

			return buildHttpResponseBean(httpResponse, response, requestTime, responseTime);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) { /*ignore*/}
            }
        }
	}

	private String buildResponseString(InputStream is) throws IOException{

		 List<Integer> bytes = new ArrayList<Integer>();
         int read;
         while((read = is.read()) != -1){
         	bytes.add(read);
         }

         byte[] buffer = new byte[bytes.size()];
         for(int i = 0; i < bytes.size(); i++){
         	buffer[i] =  bytes.get(i).byteValue();
         }

         return new String(buffer, "UTF-8");
	}

	public void setCougarDAO(ICougarDAO cougarDAO) {
		this.cougarDAO = cougarDAO;
	}

    public static void main(String[] args) {
        CougarHelpers ch = new CougarHelpers();
        ch.setJMXConnectionFactory();
    }

	/*
	 * Find the VM instance running Cougar based on COUGARVMNAME1 and COUGARVMNAME2
	 * fields, and attach, setting the JmxConnector to be used by other methods.
	 *
	 */
	private void setJMXConnectionFactory() {

		String id = null;
		List<VirtualMachineDescriptor> vms = VirtualMachine.list();
		Boolean foundVM = false;

		for (VirtualMachineDescriptor vmd : vms) {

			String vmName = cleanCommandLineArgs(vmd.displayName());

            id = vmd.id();
            JMXConnector jmxConnector = null;
            try{
                jmxConnector = createJMXConnector(id);
            }
            catch(Exception e){//Do Nothing move onto next vm
                continue;
            }
            try{
                if (!makeServerConnection(jmxConnector)) {
                    continue;
                }
            }
            catch(Exception e){//Do Nothing move onto next vm
                continue;
            }

            //No exceptions thrown so we have our cougar vm
            jmxc = jmxConnector;
            foundVM = true;
            break;
		}
		if (!foundVM) {
			throw new RuntimeException("Unable to find cougar VM");
		}
	}


	/*
	 * Clean any command line args from a vm display name before comparing it with expected values
	 */
	private String cleanCommandLineArgs(String vmName){

		vmName = vmName.replaceAll("\"", ""); // Strip any quotes from the name (needed for running on Jenkins server)

		StringBuffer buff = new StringBuffer(vmName);
		int argIndex = -1;

		while((argIndex = buff.indexOf("-D")) != -1){ // While there's a command line argument in the string

			int argEnd = -1;

			if((argEnd = buff.indexOf(" ", argIndex)) == -1){ // If this argument is at the end of the display name then clean to the end rather than to next white space
				argEnd = buff.length();
			}

			buff.replace(argIndex-1, argEnd, ""); // remove contents of buffer between space before arg starts and the next white space/end of buffer
		}

		return buff.toString();
	}

	public JMXConnector createJMXConnector(String id) throws IOException,AgentLoadException,
									AgentInitializationException, AttachNotSupportedException {

		// attach to the target application
		VirtualMachine vm = VirtualMachine.attach(id);

		// get the connector address
		String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

		// no connector address, so we start the JMX agent
		if (connectorAddress == null) {
			String agent = vm.getSystemProperties()
					.getProperty("java.home")
					+ File.separator
					+ "lib"
					+ File.separator
					+ "management-agent.jar";
			vm.loadAgent(agent);

			// agent is started, get the connector address
			connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
		}

		// establish connection to connector server
		JMXServiceURL url = new JMXServiceURL(connectorAddress);
		return JMXConnectorFactory.connect(url);
	}

	public boolean makeServerConnection(JMXConnector jmxConnector) throws IOException, MBeanException,
		AttributeNotFoundException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException{

		MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
        Set<ObjectName> mbeans = mBeanServerConnection.queryNames(new ObjectName("CoUGAR:name=healthChecker,*"), null);
        if (!mbeans.isEmpty()) {
            mBeanServerConnection.getAttribute(mbeans.iterator().next(), "SystemInService");
            return true;
        }
        return false;
	}



	/*
	 * Produce an alphabetically ordered array of the method names of a given class
	 */
	private String[] getOrderedMethodNamesFromClass(String classPath) throws ClassNotFoundException{

		Class baseline = Class.forName(classPath);
		Method[] methods = baseline.getDeclaredMethods();

		String[] methodNames = new String[methods.length];
		for(int i = 0; i < methods.length; i++){
			methodNames[i] = methods[i].getName();
		}
		Arrays.sort(methodNames);

		return methodNames;
	}

	/*
	 * Get the path of a given operation (either from the map of paths or itself)
	 */
	private String getPath(String name){

		if(OPERATION_PATHS.containsKey(name)){ // If operation path is different to name then get it
			return OPERATION_PATHS.get(name);
		}
		return name; // Else return original name
	}

	private MBeanServerConnection getJMXConnection() {

		try {
			if (jmxc == null) {
				setJMXConnectionFactory();
			}
			return jmxc.getMBeanServerConnection();
		} catch (IOException e) {
			throw new RuntimeException("Problem connecting to cougar JMX", e);
		}
	}

	private void setJMXMBeanAttribute(String mBeanName, String attributeName,
			Object newMbeanValue) {

		try {
			MBeanServerConnection mBeanServerConnection = getJMXConnection();
			ObjectName mbeanName = new ObjectName(mBeanName);
			Object currentAttributeValue = mBeanServerConnection.getAttribute(mbeanName, attributeName);
			Object reflectedValue = reflect.getRealProperty(currentAttributeValue.getClass(), newMbeanValue);
			Attribute attribute = new Attribute(attributeName, reflectedValue);
			mBeanServerConnection.setAttribute(mbeanName, attribute);
		} catch (Exception e) {
			throw new RuntimeException(JMX_SETTING_ERROR + mBeanName + ": " + attributeName, e);
		}
	}

	public void setJMXFaultControllerAttribute(String attributeName,
			String value) {
		String mBeanName = "CoUGAR:name=faultController";
		setJMXMBeanAttribute(mBeanName, attributeName, value);
	}

	private HttpResponseBean buildHttpResponseBean(HttpResponse httpResponse, String response, Date requestTime, Date responseTime) {

		HttpResponseBean httpResponseBean = new HttpResponseBean();

		Header[] headersArray = httpResponse.getAllHeaders();
		Map<String, String[]> headersMap = new HashMap<>();
		for (Header header: headersArray) {
			String[] headerAttributes = header.toString().split(": ");
			headersMap.put(headerAttributes[0], new String[] { headerAttributes[1].replace("\r\n", "")});
		}
		httpResponseBean.setResponseHeaders(headersMap);

        httpResponseBean.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
        httpResponseBean.setHttpStatusText(httpResponse.getStatusLine().getReasonPhrase());

		httpResponseBean.setRequestTime(new Timestamp(requestTime.getTime()));
		httpResponseBean.setResponseTime(new Timestamp(responseTime.getTime()));

		if ((response != null) && (!response.equalsIgnoreCase(""))) {
			httpResponseBean.setResponseObject(response);
		} else {
			httpResponseBean.setResponseObject(null);
		}

		return httpResponseBean;
	}

	private void completeRestMethodBuild(HttpUriRequest method,
			CougarMessageContentTypeEnum responseContentTypeEnum,
			CougarMessageContentTypeEnum requestContentTypeEnum,
			String postQuery, Map<String, String> headerParams,
			String authority, Map <String, String> authCredentials, String altUrl,
			Map<String, String> acceptProtocols, String ipAddress) {


		String contentType = selectContent(requestContentTypeEnum);
		if(!"".equals(contentType)){
			method.setHeader("Content-Type", contentType);
		}

		method.setHeader("User-Agent", "java/socket");

		String accept = selectAccept(responseContentTypeEnum, acceptProtocols);
		method.setHeader("Accept", accept);

		// No need to set this header any more as it is set by the new http client version
		/*if ((postQuery != null) && (!postQuery.equalsIgnoreCase(""))) {
			Integer contentLength = postQuery.length();
			method.setHeader("Content-Length", contentLength.toString());
		}*/

		if (headerParams != null) {
			for (Map.Entry<String, String>entry : headerParams.entrySet()) {
				String headerParamValue = entry.getValue();
				String key = entry.getKey();
				method.setHeader(key, headerParamValue);
		//		logger.LogBetfairDebugEntry("Rest request header param added: '"
		//						+ key + ":" + headerParamValue + "'");
			}
		}

		if (authority != null) {
			method.setHeader("X-Authentication", authority);
		}
		if (authCredentials != null) {
			if("".equals(altUrl)){
				method.setHeader("X-Token-Username", authCredentials.get("Username"));
				method.setHeader("X-Token-Password", authCredentials.get("Password"));
			}
			else{
				method.setHeader("X-AltToken-Username", authCredentials.get("Username"));
				method.setHeader("X-AltToken-Password", authCredentials.get("Password"));
			}

		}

		if (ipAddress==null) {
			method.setHeader("X-Forwarded-For", null);
		} else if (!ipAddress.trim().equalsIgnoreCase("DO NOT INCLUDE")) {
			method.setHeader("X-Forwarded-For", ipAddress);
		}


		//logger.LogBetfairDebugEntry("Rest request postString: '"
		//		+ postQuery + "'");
	}

	private String selectContent(CougarMessageContentTypeEnum requestContentTypeEnum){
		switch (requestContentTypeEnum) {
		case JSON:
	//		logger.LogBetfairDebugEntry("Rest request Content-Type: "+ JSON_CONTENT);
			return JSON_CONTENT;
		case XML:
	//		logger.LogBetfairDebugEntry("Rest request Content-Type: "+ XML_CONTENT);
			return XML_CONTENT;
		case OTHER:
			//DO NOTHING
			return "";
		default:
			throw new RuntimeException(
					"Unsupported request message content type supplied: "
							+ requestContentTypeEnum.toString());
		}
	}

	private String selectAccept(CougarMessageContentTypeEnum responseContentTypeEnum,Map<String,String> acceptProtocols){
		if (responseContentTypeEnum == null) {
			int loopCounter = 0;
			StringBuffer acceptBuff = new StringBuffer();
			for (Map.Entry<String,String> entry : acceptProtocols.entrySet()) {
				String ranking = entry.getValue();
				String protocol = entry.getKey();
				if (loopCounter == 0) {
					if ((ranking != null) && (!ranking.equalsIgnoreCase(""))) {
						String firstProtocolWithRanking = protocol + ";" + ranking;
						acceptBuff.append(firstProtocolWithRanking);
					} else {
						acceptBuff.append(protocol);
					}

				} else {
					String nextProtocolWithRanking = "," + protocol + ";" + ranking;
					acceptBuff.append(nextProtocolWithRanking);
		//			logger.LogBetfairDebugEntry("Rest request Accept protocol: "+acceptBuff.toString());
				}
				loopCounter++;
			}
			return acceptBuff.toString();
		} else {
			switch (responseContentTypeEnum) {
			case JSON:
	//			logger.LogBetfairDebugEntry("Rest request Accept protocol: "
	//							+ JSON_CONTENT);
				return JSON_CONTENT;
			case XML:
	//			logger.LogBetfairDebugEntry("Rest request Accept protocol: "
	//							+ XML_CONTENT);
				return XML_CONTENT;
			default:
				throw new RuntimeException(
						"Unsupported response message content type supplied: "
								+ responseContentTypeEnum.toString());
			}
		}
	}

	public void setJMXMBeanAttributeValue(String mBeanName, String attributeName, Object value) {

		try {
			MBeanServerConnection mBeanServerConnection = getJMXConnection();
			ObjectName mbeanName = new ObjectName(mBeanName);
            Attribute attr = new Attribute(attributeName, value);
			mBeanServerConnection.setAttribute(mbeanName, attr);

		} catch (Exception e) {
			throw new RuntimeException(JMX_RETRIEVAL_ERROR + mBeanName + ": " + attributeName, e);
		}
	}

    public void setSOAPSchemaValidationEnabled(boolean validationEnabled) {
        setJMXMBeanAttributeValue("com.betfair.cougar.transport:type=soapCommandProcessor", "SchemaValidationEnabled", validationEnabled);
    }

	private Object getJMXMBeanAttributeValue(String mBeanName, String attributeName) {

		try {
			MBeanServerConnection mBeanServerConnection = getJMXConnection();
			ObjectName mbeanName = new ObjectName(mBeanName);
			return mBeanServerConnection.getAttribute(mbeanName, attributeName);

		} catch (Exception e) {
			throw new RuntimeException(JMX_RETRIEVAL_ERROR + mBeanName + ": " + attributeName, e);
		}
	}

	private Object invokeJMXMBeanOperation(String mBeanName, String operationName, Object[] params,
                                           String[] signature) {

		try {
			MBeanServerConnection mBeanServerConnection = getJMXConnection();
			return mBeanServerConnection.invoke(new ObjectName(mBeanName), operationName, params, signature);

		} catch (Exception e) {
			throw new RuntimeException(JMX_INVOKE_ERROR + mBeanName + ": " + operationName +
                    " with arguments : " + Arrays.toString(params), e);
		}
	}

	public String getJMXLoggingManagerAttributeValue(String attributeName) {
		String mBeanName = "CoUGAR:name=Logging";
		return getJMXMBeanAttributeValue(mBeanName, attributeName).toString();
	}

	public String getJMXApplicationPropertyValue(String key) {
		String mBeanName = "CoUGAR:name=ApplicationProperties";
        return (String) invokeJMXMBeanOperation(mBeanName, "getProperty",
                new Object[]{key},
                new String[]{"java.lang.String"});
	}

    public String getJMXSystemPropertyValue(String propertyName)
    {
        TabularData sysProps = (TabularData) getJMXMBeanAttributeValue("java.lang:type=Runtime", "SystemProperties");
        return (String) sysProps.get(new String[]{"user.dir"}).get("value");

    }

	public String getRuntimeAttributeValue(String attributeName){
		String mBeanName = "java.lang:type=Runtime";
		return getJMXMBeanAttributeValue(mBeanName, attributeName).toString();
	}

	public String getJMXAttributeValue(String mBeanName, String attributeName){
		return getJMXMBeanAttributeValue(mBeanName, attributeName).toString();
	}
	/**
	 * Returns the system Java version string in the format usable for User-Agent field
	 * in cougar log
	 * @return String
	 */
	public String getJavaVersion() {
		String s = "";
		Pattern myPattern = Pattern.compile("(.*?)\\-");
		Matcher m = myPattern.matcher(System.getProperty("java.runtime.version"));
		while (m.find()) {
		   s = m.group(1);
		}
		return "\"Java/" + s + "\"";

	}

	public Map<String,String> convertFaultObjectToMap(HttpResponseBean soapResp){

		DocumentImpl doc = (DocumentImpl) soapResp.getResponseObject();

		String actualFaultCode = doc.getElementsByTagName("faultcode").item(0).getFirstChild().getNodeValue();
		String actualFaultString = doc.getElementsByTagName("faultstring").item(0).getFirstChild().getNodeValue();
		String actualMessage = doc.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();
        Node actualTraceNode = doc.getElementsByTagName("trace").item(0).getFirstChild();
        String actualTrace = "";
        if (actualTraceNode != null) {
            actualTrace = actualTraceNode.getNodeValue();

            actualTrace = actualTrace.replace("\r", ""); // Clean string of escaped characters (to avoid errors on Linux CI build)
            actualTrace = actualTrace.trim();
        }

		Map<String,String> responseMap = new HashMap<String, String>();
		responseMap.put("faultCode", actualFaultCode);
		responseMap.put("faultString", actualFaultString);
		responseMap.put("faultMessage", actualMessage);
		responseMap.put("faultTrace", actualTrace);

		return responseMap;
	}

	// Method to convert batched JSON response to a map for comparison (as the order of the array of responses cannot be relied upon)
	public Map<String,Object> convertBatchedResponseToMap(HttpResponseBean batchedResponse) throws JSONException{

		JSONObject json = (JSONObject) batchedResponse.getResponseObject();
		JSONArray results = null;
        try {
            results = (JSONArray) json.get("response");
        }
        catch (JSONException je) {
            logger.debug(String.valueOf(json),je);
            throw je;
        }

		Map<String, Object> responseMap = new HashMap<String, Object >();

		for(int i = 0; i < results.length(); i++){
            JSONObject jsonObject = results.getJSONObject(i);
            responseMap.put("response"+ jsonObject.get("id"), jsonObject.toString());
		}

		responseMap.put("httpStatusCode",batchedResponse.getHttpStatusCode()+"");
		responseMap.put("httpStatusText",batchedResponse.getHttpStatusText());
		responseMap.put("requestTime", batchedResponse.getRequestTime());
		responseMap.put("responseTime", batchedResponse.getResponseTime());

		return responseMap;
	}
        public Date convertToSystemTimeZone(String datetoconvert)
{
	  TimeZone tz = TimeZone.getDefault();
	  Date date = new Date();
	 try{
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		 String s = datetoconvert;
		String[] tmp = s.split(".0Z");
		String s1= tmp[0];
         logger.debug("given string is" +s1);
		 date = dateFormat.parse(s1);
		logger.debug("OffsetValue is " +tz.getRawOffset());
          if(tz.getRawOffset()!= 0)

          date = new Date(date.getTime() + tz.getRawOffset());
          logger.debug("After adding offset" +date);
		if ( tz.inDaylightTime( date ))
	   {
	      Date dstDate = new Date( date.getTime() + tz.getDSTSavings() );
	     logger.debug("Day Light Saving is  "+ tz.getDSTSavings());
	       logger.debug("Dst is   "+ dstDate);

	      if ( tz.inDaylightTime( dstDate ))
	      {
	         date = dstDate;
//	         logger.debug("dst date  "+ dstDate);
	      }
       }
          logger.debug("After the day light saving" +date);


	  	}
		catch(Exception e)
		{
			logger.debug("System exception caught" +e);
		}
return date;
	}

    public String dateInUTC(Date date) {
        if (date == null) {
            return "null";
        }
        return date.toGMTString().replace("GMT","UTC");
    }
}
