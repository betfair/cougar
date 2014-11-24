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

package com.betfair.testing.utils.cougar.assertions;

import com.betfair.testing.utils.cougar.misc.AggregatedStepExpectedOutputMetaData;
import com.betfair.testing.utils.cougar.misc.NameValuePair;
import com.betfair.testing.utils.cougar.misc.ObjectUtil;
import com.betfair.testing.utils.cougar.misc.StepMetaData;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DocumentAssertion implements IAssertion {

	List<Node> expNodes = new ArrayList<Node>();
	List<Node> actNodes = new ArrayList<Node>();

	@Override
	public Document preProcess(Object actualObject, AggregatedStepExpectedOutputMetaData expectedObjectMetaData)throws AssertionError {

		Class<?> expectedClass = expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0).getClass();

		if (ObjectUtil.isDocument(expectedClass)) {

			return (Document) expectedObjectMetaData.getMetaDataAtIndex(0).getValueAtIndex(0);

		}else{

			return processAsString(expectedObjectMetaData);
		}
	}

	private Document processAsString(AggregatedStepExpectedOutputMetaData expectedObjectMetaData) throws AssertionError
	{
		Document document = null;
		String xmlString = "";
		AggregatedStepExpectedOutputMetaData metaData = expectedObjectMetaData;

		for (StepMetaData stepMetaData : metaData.getValues()) {
			for (NameValuePair nameValuePair : stepMetaData
					.getNameValuePairs()) {
				Object value = nameValuePair.getValue();
				Object key = nameValuePair.getName();
				if ((key != null) && (value != null)) {
					xmlString = xmlString + String.valueOf(value).trim();
				}
			}
		}

		// Validate XML String and then create document from XML string
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(
					new StringReader(xmlString)));
			return document;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void execute(String message, Object passedExpDocument, Object passedActDocument, AggregatedStepExpectedOutputMetaData outputMetaData) throws AssertionError {

		expNodes = new ArrayList<Node>();
		actNodes = new ArrayList<Node>();

		Document expDocument;
		Document actDocument;

		try {
			expDocument = (Document) passedExpDocument;
		} catch (ClassCastException e) {
			AssertionUtils.actionFail("Expected object is not an XML Document, which the Actual object is.");
			return;
		}

		try {
			actDocument= (Document) passedActDocument;
		} catch (ClassCastException e) {
            AssertionUtils.actionFail("Actual object is not an XML Document, which the Expected object is.");
			return;
		}

		NodeList expNodeList = expDocument.getChildNodes();
		for (int i = 0; i < expNodeList.getLength(); i++) {
			iterate(expNodeList.item(i), expNodes);
		}

		NodeList actNodeList = actDocument.getChildNodes();
		for (int i = 0; i < actNodeList.getLength(); i++) {
			iterate(actNodeList.item(i), actNodes);
		}

        String messagePrefix = "\nExpected:\n" + new XMLHelpers().getXMLAsString(expDocument) + "\n\nactual:\n"+new XMLHelpers().getXMLAsString(actDocument)+"\n\n";

//        TODO: Consider XMLUnit to compare XML?
//        XMLUnit.setIgnoreAttributeOrder(true);
//        assertXMLEqual(messagePrefix, expDocument, actDocument);

		Collections.reverse(expNodes);
		Collections.reverse(actNodes);

		//Debug purposes
		//TODO change to be logged this does not work for some of the parsed docs
	/*	System.out.print(new XMLHelpers().getXMLAsString(expDocument));
		System.out.print("");
		System.out.print(new XMLHelpers().getXMLAsString(actDocument));*/

        AssertionUtils.jettAssertEquals(messagePrefix+": Check number of keys in document: ", expNodes.size(), actNodes.size());

		Node previousExpNode = null;
		int counter = 0;
		for (Node expectedNode : expNodes) {

			if (actNodes.size() > counter) {

				Node actualNode = actNodes.get(counter);

				if (expectedNode.getNodeType() == 1) {

                    AssertionUtils.jettAssertEquals(messagePrefix+": Node <" + expectedNode.getNodeName()
							+ "> node type check: ", expectedNode.getNodeType(),
							actualNode.getNodeType());

                    AssertionUtils.jettAssertEquals(messagePrefix+": Node <"
							+ expectedNode.getNodeName() + "> node name check: ",
							expectedNode.getNodeName(), actualNode.getNodeName());

					NamedNodeMap expectedNodeAttributes = expectedNode
							.getAttributes();
					NamedNodeMap actualNodeAttributes = actualNode.getAttributes();
					if (expectedNodeAttributes != null) {
						for (int i = 0; i < expectedNodeAttributes.getLength(); i++) {
							String expAttribute = expectedNodeAttributes.item(i)
									.getNodeValue();

							if (actualNodeAttributes.item(i)==null) {
                                AssertionUtils.actionFail(messagePrefix+": Node <"
										+ expectedNode.getNodeName()
										+ "> : Expected attribute <" + expAttribute + "> but was NULL ");
							} else {
								String actAttribute = actualNodeAttributes.item(i)
										.getNodeValue();

                                AssertionUtils.jettAssertEquals(messagePrefix+": Node <"
										+ expectedNode.getNodeName()
										+ "> node attribute check: ", expAttribute,
										actAttribute);
							}
						}
					}

					previousExpNode = expectedNode;

				} else {

/*					assertion.multiAssertEquals("Node <" + previousExpNode.getNodeName()
							+ "> node value type check: ", expectedNode.getNodeType(),
							actualNode.getNodeType(), bean);*/


					if (expectedNode.getNodeValue() == null) {
                        AssertionUtils.jettAssertNull(messagePrefix+": Node <"
								+ previousExpNode.getNodeName()
								+ "> check node value NULL: ",
								actualNode.getNodeValue());
					} else {

                        AssertionUtils.jettAssertEquals(messagePrefix+": Node <"
								+ previousExpNode.getNodeName()
								+ "> node value check: ", expectedNode
								.getNodeValue(), actualNode.getNodeValue());
					}

				}
			} else {
                AssertionUtils.actionFail(messagePrefix+": Node <" + expectedNode.getNodeName()
						+ "> node is missing.");
			}

			counter++;
		}

	}

	private void iterate(Node node, List<Node> passedList) {
		if (node.getFirstChild() != null) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				iterate(nodes.item(i), passedList);
			}
		}
		passedList.add(node);
	}

}
