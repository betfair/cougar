/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.transformations.manglers;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BSIDL 3.1 supports defining a response as both a "response" and a "simpleResponse". This mangler
 * simply converts "response" elements to "simpleResponse" elements so that we can continue to
 * work with the same old ftls. It throws an IllegalArgumentException if the idd specifies both - this is
 * allowed by the xsd, but is complete nonsense.
 * <p/>
 * If at some point support for "simpleResponse" is dropped from BSIDL then this mangler can be removed and
 * the ftls updated to check for "response" instead.
 */
public class ResponseToSimpleResponseMangler extends AbstractMangler {

    @Override
    public String getName() {
        return "ResponseToSimpleResponseMangler";
    }

    @Override
    public void mangleDocument(Node doc) {

        final XPathFactory factory = XPathFactory.newInstance();

        try {
            //Get all parameters nodes, and recurse through to find any that have a "response" element defined
            NodeList nodes = (NodeList)factory.newXPath().evaluate("//parameters", doc, XPathConstants.NODESET);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node parametersNode = nodes.item(i);
                    Node toBeReplaced = getNodeToBeReplaced(parametersNode);
                    if (toBeReplaced != null) {
                        //There is no way to simply change the qualifiedName or localName of the element, so we need to
                        //go through all this palarva of making a copy and replacing it
                        String qualifiedName = toBeReplaced.getPrefix() != null ? toBeReplaced.getPrefix() + ".simpleResponse" : "simpleResponse";
                        Element replacementNode = parametersNode.getOwnerDocument().createElementNS(toBeReplaced.getNamespaceURI(), qualifiedName);
                        //Copy all the child nodes (which surprisingly does not include attributes, even though they are nodes)
                        NodeList replacementChildNodes = toBeReplaced.getChildNodes();
                        if (replacementChildNodes != null) {
                            for (int j = 0; j < replacementChildNodes.getLength(); j++) {
                                replacementNode.appendChild(replacementChildNodes.item(j).cloneNode(true));
                            }
                        }
                        //Copy all the attributes
                        NamedNodeMap attributes = toBeReplaced.getAttributes();
                        if (attributes != null) {
                            for (int j = 0; j < attributes.getLength(); j++) {
                                Attr attribute = (Attr)attributes.item(j);
                                //Strangely, it is possible to get a null attribute from this collection... check for it
                                if (attribute != null) {
                                    replacementNode.setAttributeNode((Attr)attribute.cloneNode(true));
                                }
                            }
                        }
                        //Replace the existing node with a new
                        parametersNode.replaceChild(replacementNode, toBeReplaced);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("XPath failed to get parameters", e);
        }
    }

    //Check for a "response" element, which will need to be converted to a "simpleResponse" element
    //Also ensures only one of the two has been defined
    private Node getNodeToBeReplaced(Node parametersNode) {
        Node toBeReplaced = null;
        int responseCount = 0;
        NodeList childNodes = parametersNode.getChildNodes();
        if (childNodes != null) {
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                String name=childNode.getLocalName();
                if ("simpleResponse".equals(name) || "response".equals(name)) {
                    responseCount++;
                    if("response".equals(name)){
                        //This is the node that will need to be replaced with a copy with a localName of "simpleResponse"
                        toBeReplaced=childNode;
                    }
                }
                //If responseCount>1 This implies that both a simpleResponse and response have
                //been defined - this is allowed by the xsd, but makes no sense
                if (responseCount > 1) {
                    throw new IllegalArgumentException("Only one of either simpleResponse or response should define the response type");
                }
            }
        }
        return toBeReplaced;
    }


}
