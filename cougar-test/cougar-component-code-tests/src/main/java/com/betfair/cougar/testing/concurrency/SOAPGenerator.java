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

package com.betfair.cougar.testing.concurrency;

import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.misc.StringHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

final class SOAPGenerator {

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
	private static int complexIdentifier = 0;
	private static final String BAS = "bas";
	private static final String MESSAGE = "message";
	private static final int RAND_STRING_LENGTH = 3;
	
	/**
	 * Generate the shell of the SOAP Message
	 * 
	 * @return
	 */
	
	private SOAPGenerator(){
		
	}
	
	private static SOAPMessage generateSOAPMessageShell(HttpCallBean httpCallBean) {
		SOAPMessage message = null;
		String secNameSpace = "http://www.betfair.com/security/";
		httpCallBean.setServiceName("Baseline");
		String nameSpace = httpCallBean.getNameSpace();
		
		if(nameSpace==null || nameSpace.equals(""))
		{
			throw new RuntimeException("Namespace error invalid :" + nameSpace);
		}
		
		MessageFactory mf;

		try {
			mf = MessageFactory.newInstance();

			message = mf.createMessage();

			SOAPPart soapPart = message.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();

			envelope.addNamespaceDeclaration("soapenv",
					"http://schemas.xmlsoap.org/soap/envelope/");
			envelope.addNamespaceDeclaration(BAS, nameSpace);

			envelope.addNamespaceDeclaration("sec", secNameSpace);

			// header
			envelope.getHeader().detachNode();
			SOAPHeader soapHeader = envelope.addHeader();

			Name header = envelope.createName("Header", "soapenv",
					"http://schemas.xmlsoap.org/soap/envelope/");

			soapHeader.addHeaderElement(header);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return message;
	}

	public static SOAPMessageExchange buildSOAPMessageCOMPLEX(
			HttpCallBean httpCallBean) {
		
		SOAPMessageExchange msgEx = new SOAPMessageExchange();
		
		SOAPMessage message = generateSOAPMessageShell(httpCallBean);

		SOAPEnvelope envelope;
		try {

			envelope = message.getSOAPPart().getEnvelope();

			SOAPBody body;
			body = envelope.getBody();

			SOAPElement root = body.addChildElement(
					"TestComplexMutatorRequest", "bas", httpCallBean
							.getNameSpace());

			SOAPElement theMessage = root.addChildElement(MESSAGE, BAS,
					httpCallBean.getNameSpace());

			theMessage.addChildElement(
					envelope.createName("name", BAS, httpCallBean
							.getNameSpace())).addTextNode("sum");

			theMessage.addChildElement(
					envelope.createName("value1", BAS, httpCallBean
							.getNameSpace())).addTextNode("" + complexIdentifier);

			theMessage.addChildElement(
					envelope.createName("value2", BAS, httpCallBean
							.getNameSpace())).addTextNode("" + complexIdentifier);
			
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
		// Uncomment for debug

		/**
		 try
		 {
			 System.out.println("\n Soap Request:\n");
		 
		 message.writeTo(System.out); System.out.println();
		  
		 }
		 catch (IOException e) { 
			 throw new RuntimeException(e);
		 } catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	**/
	 
		msgEx.setRequest(message);
		msgEx.setResponse(generateExpectedResultShell("sum = " + complexIdentifier * 2 ));
		
		complexIdentifier ++;
		
		return msgEx;

	}
	
	public static SOAPMessageExchange buildSOAPMessagePARAMSTYLES(HttpCallBean httpCallBean)	
	{
		//set up the param fields
		String headerParam;
		String queryParam;

		headerParam = "FOO"; //TODO this could be randomised for additional coverage
		queryParam = StringHelpers.generateRandomString(RAND_STRING_LENGTH, "UPPER");
		
		SOAPMessage message = generateSOAPMessageShell(httpCallBean);

		SOAPMessageExchange msgEx = new SOAPMessageExchange();
		
		SOAPEnvelope envelope;
		try {

			envelope = message.getSOAPPart().getEnvelope();

			SOAPBody body;
			body = envelope.getBody();

			SOAPElement root = body.addChildElement(
					"TestParameterStylesRequest", BAS, httpCallBean
							.getNameSpace());

			root.addChildElement(
					envelope.createName("HeaderParam", BAS, httpCallBean
							.getNameSpace())).addTextNode(headerParam);
			
			root.addChildElement(
					envelope.createName("queryParam", BAS, httpCallBean
							.getNameSpace())).addTextNode(queryParam);
			
			
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
		
		String resp = "headerParam=" + headerParam +",queryParam=" + queryParam;
		msgEx.setRequest(message);
		msgEx.setResponse(generateExpectedResultShell(resp));
		
		return msgEx;
		
	}
	
	public static SOAPMessageExchange buildSOAPMessageSIMPLEGET(HttpCallBean httpCallBean)
	{
		SOAPMessageExchange msgEx = new SOAPMessageExchange();
		SOAPMessage message = generateSOAPMessageShell(httpCallBean);

		SOAPEnvelope envelope;
		
		String content = StringHelpers.generateRandomString(RAND_STRING_LENGTH, "UPPER");
		
		try {

			envelope = message.getSOAPPart().getEnvelope();

			SOAPBody body;
			body = envelope.getBody();

			SOAPElement root = body.addChildElement(
					"TestSimpleGetRequest", BAS, httpCallBean
							.getNameSpace());

			 root.addChildElement(MESSAGE, BAS,
					httpCallBean.getNameSpace()).addTextNode(content);

			
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
		
		msgEx.setRequest(message);
		
		msgEx.setResponse(generateExpectedResultShell(content));
		return msgEx;
		
	}
	
	/**
	 * Generate a simple result message that consists of <response><message>messageString</response></message>
	 * 
	 * @param messageString
	 * @return
	 */
	private static Document generateExpectedResultShell(String messageString) {

		DocumentBuilder documentBuilder;
		Document document = null;

		try {

			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			document = documentBuilder.newDocument();

			Element rootElement = document.createElement("response");

			Element message = document.createElement(MESSAGE);
			message.appendChild(document.createTextNode(messageString));

			rootElement.appendChild(message);

			document.appendChild(rootElement);

		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

		return document;
	}

	public static SOAPMessageExchange buildSOAPMessageAsyncGet(HttpCallBean httpCallBean) {

			SOAPMessageExchange msgEx = new SOAPMessageExchange();
			SOAPMessage message = generateSOAPMessageShell(httpCallBean);

			SOAPEnvelope envelope;
			
			String content = StringHelpers.generateRandomString(RAND_STRING_LENGTH, "UPPER");
			
			try {

				envelope = message.getSOAPPart().getEnvelope();

				SOAPBody body;
				body = envelope.getBody();

				SOAPElement root = body.addChildElement(
						"TestSimpleAsyncGetRequest", BAS, httpCallBean
								.getNameSpace());

				 root.addChildElement(MESSAGE, BAS,
						httpCallBean.getNameSpace()).addTextNode(content);

				
			} catch (SOAPException e) {
				throw new RuntimeException(e);
			}
			
			msgEx.setRequest(message);
			
			msgEx.setResponse(generateExpectedResultShell(content));
			return msgEx;
			
	}
}
