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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.betfair.cougar.codegen.ValidationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * remove all simple types defined (except for valid values ones) and
 * replace all references to them with their result.
 */
public class SimpleTypeMangler extends AbstractMangler {

	@Override
	public String getName() {
		return "Simple Type Mangler";
	}

	@Override
	public void mangleDocument(Node doc) {
		try {
			// First thing to is to get all simple types defined in the IDL
			List<Node> simpleTypes = getChildrenWithName(getName(), doc, "simpleType");
			// a container for checking if a simple type is already defined
			Set<String> definedSimpleTypes = new HashSet<String>();
			for (Node st: simpleTypes) {
				List<Node> children = getChildrenWithName(getName(), st, "validValues");
				if (children.isEmpty()) {
					String name = getAttribute(getName(), st, "name");
					String type = getAttribute(getName(), st, "type");

					// check for simpleType duplicates
					if(definedSimpleTypes.contains(name)){
						throw new ValidationException("The data type " + name + " is already defined", st);
					}else{
						definedSimpleTypes.add(name);
					}

					replaceSimpleType(doc, name, type);
					st.getParentNode().removeChild(st);
				}
			}
		} catch (ValidationException e) {
			throw new IllegalArgumentException("Unable to mangle document", e);
		}

	}

	private void replaceSimpleType(Node doc, String simpleTypeName, String simpleTypePrimitive) throws ValidationException {
		replaceViaXPath(doc, simpleTypeName, simpleTypePrimitive, "//parameter");
		replaceViaXPath(doc, simpleTypeName, simpleTypePrimitive, "//simpleResponse");
		replaceViaXPath(doc, simpleTypeName, simpleTypePrimitive, "//response");
	}

	private void replaceViaXPath(Node doc, String simpleTypeName, String simpleTypePrimitive, String xPath)  throws ValidationException {
		// Need to zap through the entire document and replace anything that has a type
		// of the "name" parameter, and replace it's value with the "type" parameter.
		final XPathFactory factory = XPathFactory.newInstance();
        NodeList nodes;
        try {
    		nodes = (NodeList) factory.newXPath().evaluate(xPath, doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ValidationException("Unable to parse XPath "+xPath, doc, e);
		}
        for (int i = 0; i < nodes.getLength(); i++) {
        	Node n = nodes.item(i);
        	Node attrNode = getAttributeNode(getName(), n, "type");
        	String content = attrNode.getTextContent();
        	if (content.equals(simpleTypeName)) {
        		attrNode.setTextContent(simpleTypePrimitive);

        	} else {
        		// Need to check if it's a composite type and replace there.
        		String[] composites = getComposites(content);
        		boolean contentChanged = false;
        		if (composites != null) {
        			// It is a composite type - check the underlying data types
        			for (String composite: composites) {
        	        	if (composite.equals(simpleTypeName)) {
        	        		content = content.replace(simpleTypeName, simpleTypePrimitive);
        	        		contentChanged = true;
        	        	}
        			}
        			if (contentChanged) {
        				attrNode.setTextContent(content);
        			}
        		}
        	}
        }

	}
}
