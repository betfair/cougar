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

package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.betfair.cougar.util.dates.DateTimeUtility;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.util.base64.Base64Utils;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transcribes Transcribable java objects to XML.
 *
 */
@SuppressWarnings("unchecked")
public class XMLTranscriptionOutput implements TranscriptionOutput {

	private QName entryElementName;
	private OMNamespace ns;
	private OMFactory factory;
	private OMElement currentNode;
	private OMNamespace xsiNamespace;

	/**
	 * Create an XMLTranscriptionOutput which will write transcibable objects to the specified
	 * element with the specified namespace.
	 * @param currentNode the XML element to transcribe the object to
	 * @param ns the namespace that should be used
	 * @param factory
	 */
	public XMLTranscriptionOutput(OMElement currentNode, OMNamespace ns, OMFactory factory) {
		this.ns = ns;
		this.factory = factory;
		this.currentNode = currentNode;
		entryElementName = new QName(ns.getNamespaceURI(), "entry", ns.getPrefix());
		xsiNamespace = factory.createOMNamespace(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
	}

	@Override
	public void writeObject(Object obj, Parameter param, boolean client) throws Exception {
		if (obj != null || parameterIsNillable(param)) {
			OMElement paramNode = factory.createOMElement(param.getName(), ns, currentNode);
			writeObject(obj, param.getParameterType(), paramNode, client);
		}
	}

	private boolean parameterIsNillable(Parameter param) {
		return param.isMandatory()
			|| param.getParameterType().getType() == ParameterType.Type.LIST
			|| param.getParameterType().getType() == ParameterType.Type.SET
			|| param.getParameterType().getType() == ParameterType.Type.MAP;
	}

	private void writeObject(Object obj, ParameterType paramType, OMElement node, boolean client) throws Exception {
		if (obj == null) {
			node.addAttribute("nil", "true", xsiNamespace);
		} else {
			switch(paramType.getType()) {
			case OBJECT:
				//decend
				OMElement _copy = currentNode;
				currentNode = node;
				//transcribe
                if (paramType.getImplementationClass().equals(EnumWrapper.class)) {
                    factory.createOMText(node, writeSimpleObjectString(obj, paramType.getComponentTypes()[0]));
                }
                else {
                    Transcribable t = (Transcribable)obj;
                    t.transcribe(this, TranscribableParams.getNone(), client);
                    //ascend
                    currentNode = _copy;
                }
				break;
			case MAP:
				Map map = (Map)obj;
                for(Object obje:map.entrySet()){
                    Map.Entry e=(Map.Entry)obje;
                    OMElement entryElement = factory.createOMElement("entry", ns, node);
                    entryElement.addAttribute("key", writeSimpleObjectString(e.getKey(), paramType.getComponentTypes()[0]), null);
                    if (e.getValue() != null) {
                        writeObject(e.getValue(), paramType.getComponentTypes()[1], factory.createOMElement(paramType.getComponentTypes()[1].getImplementationClass().getSimpleName(), ns, entryElement), client);
                    }
                }
				break;
			case LIST:
				if (paramType.getComponentTypes()[0].getType() == ParameterType.Type.BYTE) {
					factory.createOMText(node, Base64Utils.encode((byte[])obj));
				} else {
					List list = (List)obj;
					for (Object element : list) {
						//create element node
						writeObject(element, paramType.getComponentTypes()[0], factory.createOMElement(paramType.getComponentTypes()[0].getImplementationClass().getSimpleName(), ns, node), client);
					}
				}
				break;
			case SET:
				Set set = (Set)obj;
				for (Object element : set) {
					//create element node
					writeObject(element, paramType.getComponentTypes()[0], factory.createOMElement(paramType.getComponentTypes()[0].getImplementationClass().getSimpleName(), ns, node), client);
				}
				break;
			default :
				factory.createOMText(node, writeSimpleObjectString(obj, paramType));
			}
		}
	}

	private String writeSimpleObjectString(Object obj, ParameterType paramType) {
		switch(paramType.getType()) {
		case BOOLEAN:
		case DOUBLE:
		case FLOAT:
		case INT:
		case LONG:
		case STRING:
		case ENUM:
		case BYTE:
			return obj.toString();
		case DATE:
			return DateTimeUtility.encode((Date)obj);
		}
		throw new UnsupportedOperationException("Parameter Type " + paramType + " is not a simple object");
	}
}