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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class XMLHelpers {

	public String getXMLAsString(Document document) {
		/*OutputFormat format = new OutputFormat(document);
		StringWriter stringOut = new StringWriter();
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		try {
			serial.serialize(document);
			return stringOut.toString();
		} catch (IOException e) {
			throw new UtilityException(e);
		}*/
		
		
		StringWriter stw = new StringWriter(); 
        Transformer serializer;
        try {
	        serializer = TransformerFactory.newInstance().newTransformer(); 
	        serializer.transform(new DOMSource(document), new StreamResult(stw));
        } catch (TransformerConfigurationException e) {
        	throw new RuntimeException("Error: Converting XML Document to String", e);
		} catch (TransformerFactoryConfigurationError e) {
        	throw new RuntimeException("Error: Converting XML Document to String", e);
		} catch (TransformerException e) {
        	throw new RuntimeException("Error: Converting XML Document to String", e);
		} 
        return stw.toString(); 
		
	}
	
	public Document getXMLObjectFromString(String xmlString) {
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlString)));
			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Invalid XML passed to method", e);
		} catch (SAXException e) {
			throw new RuntimeException("Invalid XML passed to method", e);
		} catch (IOException e) {
			throw new RuntimeException("Invalid XML passed to method", e);
		} finally {}
	}
	
	public void renameRootElement(Document document, String newRootElementName) {
		
		Node rootNode = document.getDocumentElement();
		NodeList childNodes = rootNode.getChildNodes();

		NamedNodeMap attributes = rootNode.getAttributes();
		
		document.removeChild(rootNode);
		
		Node newRootNode = document.createElement(newRootElementName); 
		document.appendChild(newRootNode);
			
		int numberOfAttributes = new Integer(attributes.getLength());
		for (int i = 0; i < numberOfAttributes; i++) {
			Attr attributeNode = (Attr)attributes.item(0);
			Element newRootNodeElement = (Element)newRootNode;
			Element rootNodeElement = (Element)rootNode;
			rootNodeElement.removeAttributeNode(attributeNode);
			newRootNodeElement.setAttributeNode(attributeNode);
		}
		      
		int numberOfChildNodes = new Integer(childNodes.getLength());
		for (int i = 0; i < numberOfChildNodes; i++) {
			Node childNode = childNodes.item(0);
			newRootNode.appendChild(childNode);
		}
	}
	
	public Document createAsDocument(Document document) {
		return document;
	}
	
	/**
	 * Get the text content of the Child Node contained in the passed Node, where Child Node has the
	 * passed nodeName.
	 * 
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public String getTextContentFromChildNode(Node parentNode, String nodeName) {
		
		if (!parentNode.hasChildNodes()) {
			throw new RuntimeException("Passed parent node has no children");
		}
		
		NodeList childNodes = parentNode.getChildNodes();
		int numberOfChildNodes = childNodes.getLength();
		for (int i=0;i<numberOfChildNodes;i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equalsIgnoreCase(nodeName.trim())) {
				return childNode.getTextContent();
			}
		}
		throw new RuntimeException("Unable to find child node: " + nodeName);
	}
	
	/**
	 * Get the Node that contains a child node with the passed node name and test content from
	 * the passed NodeList
	 *  
	 * @param nodeList
	 * @param nodesChildName
	 * @param nodesChildTextContent
	 * @return
	 */
	public Node getNodeFromListContainingSpecifiedChildNode(NodeList nodeList, String nodesChildName, String nodesChildTextContent) {
	
		int listLength = nodeList.getLength();
		for (int i=0;i<listLength;i++) {
			Node node = nodeList.item(i);
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				int childrenListLength = children.getLength();
				for (int j=0;j<childrenListLength;j++) {
					Node child = children.item(j);
					if ((child.getNodeName().equalsIgnoreCase(nodesChildName.trim()) && (child.getTextContent().equalsIgnoreCase(nodesChildTextContent.trim())))) {
						return node;
					}
				}
			}
		}
		throw new RuntimeException("Unable to find child node with child matching: " + nodesChildName + " - " + nodesChildTextContent);
	}
	
	/**
	 * Get the Node that contains all of the passed child name/text content pairs from the passed NodeList
	 *  
	 * @param nodeList
	 * @param nodesChildNames
	 * @param nodesChildTextContents
	 * @return
	 */
	public Node getNodeFromListContainingSpecifiedChildNodes(NodeList nodeList, String nodesChildNames, String nodesChildTextContents) {
        return getNodeFromListContainingSpecifiedChildNodes(nodeList, nodesChildNames.split(","), nodesChildTextContents.split(","));
    }
	public Node getNodeFromListContainingSpecifiedChildNodes(NodeList nodeList, String[] nodesChildNames, String[] nodesChildTextContents) {
		int listLength = nodeList.getLength();
		
		// Init a list of booleans to mark if all the child name/text content pairs have been found
		List<Boolean> foundChild = new ArrayList<Boolean>();
		
		for (int i=0;i<listLength;i++) { // For each given node
			
			// Set entire found list to false as starting searching a new node
			foundChild.clear();
			for(int j = 0; j < nodesChildNames.length; j++){
				foundChild.add(false);
			}
			
			Node node = nodeList.item(i);
			if (node.hasChildNodes()) {
				
				NodeList children = node.getChildNodes();
				int childrenListLength = children.getLength();
				
				for (int j=0;j<childrenListLength;j++) {  // Check each child node for the required child name/content
					
					Node child = children.item(j);					
					for (int k = 0; k < nodesChildNames.length; k++){	
						
						if ((child.getNodeName().equalsIgnoreCase(nodesChildNames[k].trim()) && (child.getTextContent().equalsIgnoreCase(nodesChildTextContents[k].trim())))) {
							foundChild.set(k, true); // found this requested child name/text content pair
						}
					}
				}
				
				if(!foundChild.contains(false)){ // If found all required name/text content pairs then return this node
					return node;
				}
			}
		}
		throw new RuntimeException("Unable to find node with all the given child name/text content pairs");
	}
	
	/**
	 * Get the specified child name (first one the matched) from the passed
	 * parent Node.
	 * 
	 * @param parentNode
	 * @param childNodeName
	 * @return
	 */
	public Node getSpecifiedChildNode(Node parentNode, String childNodeName) {
		if (!parentNode.hasChildNodes()) {
			throw new RuntimeException("Passed parent node has no children");
		}
		
		NodeList childNodes = parentNode.getChildNodes();
		int numberOfChildNodes = childNodes.getLength();
		for (int i=0;i<numberOfChildNodes;i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equalsIgnoreCase(childNodeName.trim())) {
				return childNode;
			}
		}
		
		throw new RuntimeException("Unable to find child node: " + childNodeName);
	}
	
	/**
	 * Get the Node that contains a child node with the passed node name and test content from
	 * the passed parent Node.
	 * 
	 * @param parentNode
	 * @param nodesChildName
	 * @param nodesChildTextContent
	 * @return
	 */
	public Node getNodeContainingSpecifiedChildNodeFromParent(Node parentNode, String nodesChildName, String nodesChildTextContent) {
		
		if (!parentNode.hasChildNodes()) {
			throw new RuntimeException("Passed parent node has no children");
		}
		
		
		NodeList nodeList = parentNode.getChildNodes();
		int listLength = nodeList.getLength();
		for (int i=0;i<listLength;i++) {
			Node node = nodeList.item(i);
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				int childrenListLength = children.getLength();
				for (int j=0;j<childrenListLength;j++) {
					Node child = children.item(j);
					if ((child.getNodeName().equalsIgnoreCase(nodesChildName.trim()) && (child.getTextContent().equalsIgnoreCase(nodesChildTextContent.trim())))) {
						return node;
					}
				}
			}
		}
		throw new RuntimeException("Unable to find child node with child matching: " + nodesChildName + " - " + nodesChildTextContent);
		
	}
	
	/**
	 * Get the Text Content of the first Node in the NodeList that has a matched node name
	 *  
	 * @param nodeList
	 * @param nodeName
	 * 
	 * @return
	 */
	public String getTextContentOfNodeFromList(NodeList nodeList, String nodeName) {
	
		int listLength = nodeList.getLength();
		for (int i=0;i<listLength;i++) {
			Node node = nodeList.item(i);
			if (node.hasChildNodes()) {
				NodeList children = node.getChildNodes();
				int childrenListLength = children.getLength();
				for (int j=0;j<childrenListLength;j++) {
					Node child = children.item(j);
					if (child.getNodeName().equalsIgnoreCase(nodeName.trim())) {
						return child.getTextContent();
					}
				}
			}
		}
		throw new RuntimeException("Unable to find node in list: " + nodeName);
	}
	
}
