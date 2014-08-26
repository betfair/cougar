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

package com.betfair.cougar.transformations;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.betfair.cougar.codegen.ValidationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractTransformer {

    private static final Pattern COMPOSITE = Pattern.compile("(?:list|set|map)\\((.+)\\)");

    protected Node getAttributeNode(String name, Node node, String attributeName)
            throws ValidationException {
        try {
            Node attribute = node.getAttributes().getNamedItem(attributeName);
            if (attribute == null) {
                return null;
            }
            return attribute;

        } catch (final Exception e) {
            throw new ValidationException(name + " - Failed to extract " + attributeName + " from node", node, e);
        }
    }

    protected String getAndValidateAttribute(String name, Node node, String attributeName) throws ValidationException {
        String out = getAttribute(name, node, attributeName);
        if (out == null || out.length() == 0) {
            throw new ValidationException(name + " - type not defined", node);
        }
        return out;
    }

    protected String getAttribute(String name, Node node, String attributeName)
            throws ValidationException {
        Node attributeNode = getAttributeNode(name, node, attributeName);
        if (attributeNode == null) {
            return null;
        }
        return attributeNode.getTextContent();
    }

    protected Integer getAttributeAsInt(String name, Node node, String attributeName)
            throws ValidationException {
        String idAsString = getAttribute(name, node, attributeName);
        return idAsString == null ? null : Integer.parseInt(idAsString);

    }

    protected Node getFirstChildWithName(Node node, String childName) throws ValidationException {
        NodeList children = node.getChildNodes();
        Node firstParam = null;
        for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && childName.equals(n.getLocalName())) {
                firstParam = n;
                break;
            }
        }
        if (firstParam == null) {
            return null;
        }
        return firstParam;
    }

    protected Element findAncestor(Node node, String elementName) {
        Node parent = node.getParentNode();
        if (parent == null) {
            return null;
        }
        if (parent instanceof Element) {
            if (((Element) parent).getTagName().equals(elementName)) {
                return ((Element) parent);
            }
        }
        return findAncestor(parent, elementName);
    }

    protected List<Node> getChildrenWithName(String name, Node node, String childName) throws ValidationException {
        NodeList children = node.getChildNodes();
        List<Node> result = new ArrayList<Node>();
        for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(childName)) {
                result.add(n);
            }
        }
        return result;
    }

    private static final String[] STRINGABLE_TYPES =
            new String[]{"i64", "i32", "byte", "string", "float", "double", "bool", "dateTime"};

    private boolean isStringable(String type) {
        for (String s : STRINGABLE_TYPES) {
            if (type.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSimpleCollection(String name, Node node, String type) throws ValidationException {
        String[] composites = getComposites(type);
        if (composites.length == 1) {
            // It must be a list or a set, as maps return a length of 2
            checkTypeIsSimple(name, node, composites[0], false);
            return true;
        }
        return false;
    }

    protected void checkTypeIsSimple(String name, Node node, String type, boolean allowSimpleCollections) throws ValidationException {
        // Ensure that the type passed can easily be rendered as a string.
        if (isStringable(type)) {
            return;
        }
        if (isSimpleTypeEnum(name, node, type)) {
            return;
        }
        if (allowSimpleCollections && isSimpleCollection(name, node, type)) {
            return;
        }
        throw new ValidationException(">>>" + name + "<<< - type is not stringable: " + type, node);
    }

    protected boolean isSimpleTypeEnum(String name, Node node, String type) throws ValidationException {
        // To find this out, we need to go to the root of the documnet and find
        // all defined simple types
        Node topLevel = node;
        while (!topLevel.getNodeName().equals("interface")) {
            topLevel = topLevel.getParentNode();
        }

        final XPathFactory factory = XPathFactory.newInstance();

        try {
            Boolean exists = (Boolean)factory.newXPath().evaluate("//simpleType[@name='" + type + "']", topLevel, XPathConstants.BOOLEAN);
            return exists;
        } catch (XPathExpressionException e) {
            throw new ValidationException("Unable to parse XPath //parameter", node, e);
        }
    }

    protected String[] getComposites(String type) throws ValidationException {
        Matcher m = COMPOSITE.matcher(type);
        if (m.matches()) {
            // It is a composite type - get the underlying data types
            String underlying = m.group(1);
            int commaIndex = underlying.indexOf(',');
            if (commaIndex <= 0) {
                return new String[]{underlying.trim()};
            } else {
                return new String[]{
                        underlying.substring(0, commaIndex).trim(),
                        underlying.substring(commaIndex + 1).trim()};
            }
        }
        return new String[0];

    }
}