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

package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.util.dates.DateTimeUtility;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.base64.Base64Utils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Transcribes from XML to types supported by BISDL or Transcribable Java objects.
 */
@SuppressWarnings("unchecked")
public class XMLTranscriptionInput implements TranscriptionInput {

    private static final CougarLogger logger = CougarLoggingUtils.getLogger(XMLTranscriptionInput.class);

    private static final QName keyAttName = new QName("key");

    private OMElement currentNode;

    public XMLTranscriptionInput(OMElement currentNode) {
        this.currentNode = currentNode;
    }

    public Object readObject(Parameter param) throws Exception {
        Iterator iterator = currentNode.getChildrenWithLocalName(param.getName());
        if (!iterator.hasNext()) {
            return null;
        }

        return readObject(param.getParameterType(), (OMElement)iterator.next());
    }

    private Object readObject(ParameterType paramType, OMElement node) throws Exception {
        switch (paramType.getType()) {
            case BOOLEAN:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case STRING:
            case ENUM:
            case DATE:
            case BYTE:
                return node == null ? null : readSimpleObject(paramType, node.getLocalName(), node.getText());
            case OBJECT:
                //descend - note possibly two levels if inside a collection recursion
                OMElement _copy = this.currentNode;
                currentNode = node;

                Transcribable t = (Transcribable)paramType.getImplementationClass().newInstance();
                t.transcribe(this, TranscribableParams.getAll());

                //ascend
                this.currentNode = _copy;
                return t;
            case MAP:
                Map map = new HashMap();
                for (Iterator i = node.getChildElements(); i.hasNext();) {
                    OMElement element = (OMElement)i.next();
                    Object key = readSimpleObject(paramType.getComponentTypes()[0],  node.getLocalName(), element.getAttributeValue(keyAttName));
                    map.put(key, readObject(paramType.getComponentTypes()[1], (OMElement)element.getChildElements().next()));
                }
                return map;
            case LIST:
                if (paramType.getComponentTypes()[0].getType() == ParameterType.Type.BYTE) {
                    try {
                        return Base64Utils.decode(node.getText());
                    } catch (Exception e) {
                        String message = "Unable to parse " + node.getText() + " as type " + paramType;
                        logger.log(Level.FINER, message, e);
                        throw new CougarValidationException(ServerFaultCode.SOAPDeserialisationFailure, message,e
                        );
                    }
                } else {
                    List list = new ArrayList();
                    for (Iterator i = node.getChildElements(); i.hasNext();) {
                        list.add(readObject(paramType.getComponentTypes()[0], (OMElement)i.next()));
                    }
                    return list;
                }
            case SET:
                Set set = new HashSet();
                for (Iterator i = node.getChildElements(); i.hasNext();) {
                    set.add(readObject(paramType.getComponentTypes()[0], (OMElement)i.next()));
                }
                return set;
        }
        return null;
    }

    private Object readSimpleObject(ParameterType paramType, String paramName, String textValue) {
        try {
            switch (paramType.getType()) {
                case BOOLEAN:
                    if (textValue.equalsIgnoreCase("true")) {
                        return true;
                    }
                    if (textValue.equalsIgnoreCase("false")) {
                        return false;
                    }
                    throw new IllegalArgumentException();
                case DOUBLE:
                    return Double.valueOf(textValue);
                case FLOAT:
                    return Float.valueOf(textValue);
                case INT:
                    return Integer.valueOf(textValue);
                case LONG:
                    return Long.valueOf(textValue);
                case STRING:
                    return textValue;
                case ENUM:
                    // this is converted to an enum further down in the Transcribable implementation so the original raw value can be stored in a soft failure..
                    return textValue;
                case DATE:
                    return DateTimeUtility.parse(textValue);
                case BYTE:
                    return Byte.valueOf(textValue);
            }
        } catch (Exception e) {
            throw exceptionDuringDeserialisation(paramType, paramName, e);
        }
        throw new UnsupportedOperationException("Parameter Type " + paramType + " is not supported as a simple object type");
    }

    public static CougarValidationException exceptionDuringDeserialisation(ParameterType paramType, String paramName, Exception e) {
        StringBuilder logBuffer = new StringBuilder();
        logBuffer.append("Unable to convert data in request to ");
        logBuffer.append(paramType.getType().name());
        logBuffer.append(" for parameter: ");
        logBuffer.append(paramName);
        String message = logBuffer.toString();

        logger.log(Level.FINER, message , e);
        return new CougarValidationException(ServerFaultCode.SOAPDeserialisationFailure, message,e);

    }
}