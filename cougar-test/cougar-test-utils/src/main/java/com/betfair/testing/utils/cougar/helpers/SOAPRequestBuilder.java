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

package com.betfair.testing.utils.cougar.helpers;

import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import org.apache.xerces.dom.AttributeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.util.Map;

public class SOAPRequestBuilder {

	private String nameSpace = null;
	private static final String SECURITY_NAME_SPACE = "http://www.betfair.com/security/";

	/*
	 * Construct the SOAPMessage based on xml doc and CallBean
	 * 
	 * @param document
	 * @param httpCallBean
	 * @return
	 */
	public SOAPMessage buildSOAPRequest(Document document,
			HttpCallBean httpCallBean) {

		nameSpace = httpCallBean.getNameSpace();

		if (nameSpace == null || nameSpace.equals("")) {
			throw new RuntimeException(
					"Cannot create SOAP message using the following name space : "
							+ nameSpace);
		}

		// Construct SOAPMessage
		MessageFactory mf;
		try {
			mf = MessageFactory.newInstance();

			SOAPMessage message = mf.createMessage();
			
			//add headers here
			
			MimeHeaders hd = message.getMimeHeaders();
			hd.addHeader("X-Forwarded-For", httpCallBean.getIpAddress());
			
			//SCR: 103 Trace Me testing
			//hd.addHeader("X-Trace-Me", "true");
			
			//
			if(httpCallBean.getHeaderParams()!=null)
			{
			for (String key: httpCallBean.getHeaderParams().keySet())
			{
				hd.addHeader(key, httpCallBean.getHeaderParams().get(key));
			}
			}

			SOAPPart soapPart = message.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();

			envelope.addNamespaceDeclaration("bas", nameSpace);

			// generate header
			generateSoapHeader(envelope, httpCallBean);

			// generate body
			generateSOAPBody(envelope, document);

			message.saveChanges();

			// TODO write this to the logs
			
			//Uncomment for debug

			/*try
			{
			System.out.println("\n Soap Request:\n");
			message.writeTo(System.out);
			System.out.println();
			}
			catch (IOException e) {
				throw new UtilityException(e);
			}*/
			

			return message;

		} catch (SOAPException e) {
			throw new RuntimeException(e);
		} 
	}

	private void generateSOAPBody(SOAPEnvelope envelope, Document document) throws SOAPException {

		org.w3c.dom.NodeList childNodes = document.getChildNodes();

		// Construct the body
		SOAPBody body = envelope.getBody();

		// for each child node in the message, get name and value
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node aNode = (Node) childNodes.item(i);

			iterate(envelope, aNode, body);

		}

	}

	public void generateSoapHeader(SOAPEnvelope envelope,
			HttpCallBean httpCallBean) throws SOAPException {

		// Remove existing null header
		envelope.getHeader().detachNode();

		// add new soap header
		SOAPHeader soapHeader = envelope.addHeader();

		// header are not set then break
		if (httpCallBean.getAuthority() == null
				&& httpCallBean.getSubject() == null
				&& httpCallBean.getAuthCredentials()== null) {
			return;
		}

		// create element for headers

		if (httpCallBean.getAuthority() != null) {
			String authority = httpCallBean.getAuthority();

			/*soapHeader.addChildElement(
					envelope.createName("Authentication", "", secNameSpace))
					.setTextContent(authority);*/
			
			soapHeader.addChildElement(
					envelope.createName("X-Authentication", "", SECURITY_NAME_SPACE))
					.setValue(authority);

		}

		if (httpCallBean.getAuthCredentials() != null) {
			Map<String, String> credentials = httpCallBean.getAuthCredentials();

			SOAPElement credElement = soapHeader.addChildElement(
					envelope.createName("Credentials", "", SECURITY_NAME_SPACE));
			for(Map.Entry<String,String> entry: credentials.entrySet()){
				credElement.addChildElement(entry.getKey(), "", SECURITY_NAME_SPACE).setValue(entry.getValue());
			}
			
		}

		if (httpCallBean.getSubject() != null) {
			String subject = httpCallBean.getSubject();

			/*soapHeader.addChildElement(
					envelope.createName("Subject", "", secNameSpace))
					.setTextContent(subject);*/
			
			soapHeader.addChildElement(
					envelope.createName("Subject", "", SECURITY_NAME_SPACE))
					.setValue(subject);
		}
	}

	/*
	 * Recursive loop to find children and add to body, if attributes exist then adds those
	 * 
	 * @param envelope
	 * @param node
	 * @param parentElement
	 * @throws SOAPException
	 */
	public void iterate(SOAPEnvelope envelope, Node node,
			SOAPElement parentElement) throws SOAPException {

		// if the node is an element then process it and it's children
		if(node instanceof Element){
			
				Element elemt = (Element) node;
				String localName = elemt.getNodeName();
				SOAPElement newParent = parentElement.addChildElement(localName,"bas", nameSpace);
				
				// If the node has attributes then process them
				if(node.hasAttributes()){
					AttributeMap map = (AttributeMap) node.getAttributes();
					for (int x = 0; x < map.getLength(); x++) {
						String name = map.item(x).getNodeName();
						newParent.setAttribute(name, map.item(x).getNodeValue());
					}
				}
				
				org.w3c.dom.NodeList childNodes = node.getChildNodes();
				// for each of this nodes children recursively call this method
				for (int i = 0; i < childNodes.getLength(); i++) {
					iterate(envelope, childNodes.item(i), newParent);
				}
				
		} else if (node.getNodeType() == Node.TEXT_NODE){ // Node is a text node so add it's value
			String value = node.getNodeValue();
			if (value==null) {
				parentElement.addTextNode("");
			} else {
				parentElement.addTextNode(value);
			}
		}
		// Else is some other kind of node which can be ignored
	}

}
