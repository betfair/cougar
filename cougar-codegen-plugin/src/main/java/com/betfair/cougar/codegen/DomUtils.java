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

package com.betfair.cougar.codegen;

import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DomUtils {
    public static String getNameBasedXPath(Node n, boolean includeCurrent) {
		Node parent = null;
		Stack<Node> hierarchy = new Stack<Node>();

		if (includeCurrent) {
			hierarchy.push(n);
		}
		parent = n.getParentNode();
		while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
			hierarchy.push(parent);
			parent = parent.getParentNode();
		}

		// construct xpath
		StringBuilder sb = new StringBuilder();
		Node node = null;
		while (!hierarchy.isEmpty() && null != (node = hierarchy.pop())) {
			// only consider elements
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;

				// Bung in a slash and the element name
				sb.append("/");
				sb.append(node.getLocalName());

				if (node.hasAttributes()) {
					// see if the element has a name attribute
					if (e.hasAttribute("name")) {
						// name attribute found - use that
						sb.append("[@name='").append(e.getAttribute("name")).append("']");
					}
				}
			}
		}

		// return buffer
		return sb.toString();
	}
}
