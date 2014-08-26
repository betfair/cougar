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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.betfair.cougar.codegen.ValidationException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class RequestParameterValidator extends AbstractValidator {
    private Set<String> reservedNames = new HashSet<String>();
    public RequestParameterValidator() {
        reservedNames.add("alt");
    }
	// Not thread safe, but there again nor's the validation threaded through maven...
	@Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Request Parameter Validator";
	}

	@Override
	public String getXPath() {
		return "//parameters/request";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		Set<String> namesUsed = new HashSet<String>();

		List<Node> values = getChildrenWithName(getName(), node, "parameter");
		for (Node val: values) {
			String name = getAttribute(getName(), val, "name");
			if (namesUsed.contains(name)) {
				throw new ValidationException(getName() + " - duplicate name: " +name, val);
			}
            if (reservedNames.contains(name)) {
                throw new ValidationException(getName() + " - Reserved parameter name: " +name, val);
            }
			namesUsed.add(name);


			validateExtensions(val);

		}
	}

	private void validateExtensions(Node node) throws ValidationException {
		getAndValidateAttribute(getName(), node, "name");
		String type = getAndValidateAttribute(getName(), node, "type");

        // don't require extensions for connected operations
        Element operationElement = findAncestor(node, "operation");
        boolean connected = "true".equals(operationElement.getAttribute("connected"));
        if (connected) {
            return;
        }

        Node extensions = getFirstChildWithName(node, "extensions");
        if (extensions == null) {
            throw new ValidationException(getName() + " - Node does not have any extensions children defined", node);
        }
        Node styleNode = getFirstChildWithName(extensions, "style");
        if (styleNode == null) {
            throw new ValidationException(getName() + " - style not defined", node);
        }
		String style = styleNode.getTextContent();
		if (style == null || style.length() == 0) {
			throw new ValidationException(getName() + " - style not defined", node);
		}

		if (style.equals("body")) {
            // body style params must be in a POST method
            try {
                final XPathFactory factory = XPathFactory.newInstance();
                String method = (String)
                    factory.newXPath().evaluate("../../../extensions/method", node, XPathConstants.STRING);
                if (method == null || ! method.equals("POST")) {
                    throw new ValidationException(getName() + " - body style cannot be used in a non-POST method", node);
                }
                return;
            }
            catch (XPathExpressionException e) {
                throw new ValidationException("Unable to parse XPath ../../../extensions/method", node, e);
            }
		}

        if ((style.equals("header")) || (style.equals("query"))) {
			checkTypeIsSimple(getName(), node, type, true);
            return;
		}

		throw new ValidationException(getName() + " - unsupported style: "+style, node);
	}
}
