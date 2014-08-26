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

package com.betfair.cougar.transformations.validators;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.betfair.cougar.codegen.ValidationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UnknownDataTypeValidator extends AbstractValidator{
	private Set<String> dataTypesDefined = new HashSet<String>();

	public UnknownDataTypeValidator() {
		// Add common data types
		dataTypesDefined.add("bool");
		dataTypesDefined.add("byte");
		dataTypesDefined.add("i32");
		dataTypesDefined.add("i64");
		dataTypesDefined.add("float");
		dataTypesDefined.add("double");
		dataTypesDefined.add("string");
		dataTypesDefined.add("dateTime");
        dataTypesDefined.add("void");
	}
	@Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Unknown Data Type Validator";
	}

	@Override
	public String getXPath() {
		return "/interface";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		List<Node> types = getChildrenWithName(getName(), node, "dataType");
		for (Node dt: types) {
			String name = getAttribute(getName(), dt, "name");
			dataTypesDefined.add(name);
		}
		types = getChildrenWithName(getName(), node, "simpleType");
		for (Node dt: types) {
			String name = getAttribute(getName(), dt, "name");
			dataTypesDefined.add(name);
		}
		try {
			final XPathFactory factory = XPathFactory.newInstance();
	        NodeList nodes;
			nodes = (NodeList) factory.newXPath().evaluate("//parameter", node, XPathConstants.NODESET);
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	Node n = nodes.item(i);
	        	String type = getAttribute(getName(), n, "type");
	        	if (!checkComposite(type, n)) {
	        		validateDataType(type, n);
	        	}
	        }
			nodes = (NodeList) factory.newXPath().evaluate("//simpleResponse", node, XPathConstants.NODESET);
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	Node n = nodes.item(i);
	        	String type = getAttribute(getName(), n, "type");
	        	if (!checkComposite(type, n)) {
	        		validateDataType(type, n);
	        	}
	        }
		} catch (XPathExpressionException e) {
			throw new ValidationException("Unable to parse XPath //parameter", node, e);
		}

	}


	private void validateDataType(String type, Node n)  throws ValidationException {
    	if (!dataTypesDefined.contains(type)) {
    		throw new ValidationException("Data type "+type+" is not valid in this IDL", n);
    	}
	}

	private boolean checkComposite(String type, Node n)  throws ValidationException {
		String[] composites = getComposites(type);
			for (String composite: composites) {
				validateDataType(composite, n);
			}
        return composites.length>0;
	}
}
