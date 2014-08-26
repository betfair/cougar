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

package com.betfair.cougar.marshalling.impl.databinding.xml;

import javax.xml.bind.*;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.core.api.transcription.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import org.xml.sax.SAXParseException;

public class XMLUnMarshaller implements UnMarshaller, FaultUnMarshaller {
	private final static Logger LOGGER = LoggerFactory.getLogger(XMLUnMarshaller.class);

    // todo: make schema validation configurable for rescript/xml (already done for soap)
    private static final ConcurrentMap<Class<?>,JAXBContext> jaxbContexts = new ConcurrentHashMap<>();
    private static final ConcurrentMap<JAXBContext,Schema> schemas = new ConcurrentHashMap<>();

    private SchemaValidationFailureParser schemaValidationFailureParser;

    public XMLUnMarshaller(SchemaValidationFailureParser schemaValidationFailureParser) {
        this.schemaValidationFailureParser = schemaValidationFailureParser;
    }

    @Override
	public String getFormat() {
		return "xml";
	}

    @Override
    public Object unmarshall(InputStream inputStream, ParameterType parameterType, String encoding, boolean client) {
        //It would be possible to change the way this marshaller works
        //entirely to use the XMLTranscription approach - could be done -
        //see SoapTransportCommandProcessor
        return new UnsupportedOperationException("This XML UnMarshaller does not [yet] support unmarshalling by parameterType");
    }

    @Override
    public CougarFault unMarshallFault(InputStream inputStream, String encoding) {
        //noinspection unchecked
        final HashMap<String,Object> faultMap = (HashMap<String,Object>) unmarshall(inputStream, HashMap.class, encoding, true);

        final String faultString = (String)faultMap.get("faultstring");
        final FaultCode faultCode = FaultCode.valueOf((String) faultMap.get("faultcode"));


        //noinspection unchecked
        final HashMap<String, Object> detailMap = (HashMap<String, Object>)faultMap.get("detail");
        String exceptionName = (String)detailMap.get("exceptionname");

        List<String[]> faultParams = Collections.emptyList();
        if (exceptionName != null) {
            faultParams = new ArrayList<>();
            //noinspection unchecked
            Map<String, Object> paramMap = (Map<String, Object>) detailMap.get(exceptionName);

            for(Map.Entry e:paramMap.entrySet()){
                String[] nvpair=new String[] { (String)e.getKey(), e.getValue().toString() };
                faultParams.add(nvpair);
            }
        }

        final FaultDetail fd=new FaultDetail(faultString, faultParams);

        return new CougarFault() {
            @Override
            public String getErrorCode() {
                return faultString;
            }

            @Override
            public FaultCode getFaultCode() {
                return faultCode;
            }

            @Override
            public FaultDetail getDetail() {
                return fd;
            }
        };

    }

    @Override
	public Object unmarshall(InputStream inputStream, Class<?> clazz, String encoding, boolean client) {
		try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLStreamReader reader = factory.createXMLStreamReader(new BufferedReader(new InputStreamReader(inputStream,encoding)));
            JAXBContext jc = jaxbContexts.get(clazz);
            if (jc == null) {
                jc = JAXBContext.newInstance(clazz);
                JAXBContext prev = jaxbContexts.putIfAbsent(clazz, jc);
                if (prev != null){
                	jc = prev;
                }
                Schema schema = XMLUtils.getSchema(jc);
                schemas.putIfAbsent(jc, schema);
            }
	        Unmarshaller u = jc.createUnmarshaller();
	        ValidationEventCollector validationHandler = new ValidationEventCollector();
			u.setEventHandler(validationHandler);
	        setSchema(jc, u);
	        Object obj = u.unmarshal(reader);
	        if (!clazz.isAssignableFrom(obj.getClass())) {
	            throw CougarMarshallingException.unmarshallingException("xml", "Deserialised object was not of class "+clazz.getName(),false);
	        }
	        validate(validationHandler);
	        return obj;
		} catch (UnmarshalException e) {
            Throwable linkedException = e.getLinkedException();
            if(linkedException!=null) {
                LOGGER.debug(linkedException.getMessage());
                if (linkedException instanceof SAXParseException) {
                    CougarException ce = schemaValidationFailureParser.parse((SAXParseException)linkedException, getFormat(), client);
                    if (ce != null) {
                        throw ce;
                    }
                }
                throw CougarMarshallingException.unmarshallingException("xml",linkedException,client);
            }
            throw CougarMarshallingException.unmarshallingException("xml",e,client);
        } catch (CougarMarshallingException e) {
            throw e;
		} catch (Exception e) {
			throw CougarMarshallingException.unmarshallingException(getFormat(), "Unable to deserialise REST/XML request", e, client);
        }
	}

    private void setSchema(JAXBContext jc, Unmarshaller u) {
        Schema schema = schemas.get(jc);
        if(schema!=null) {
            u.setSchema(schema);
        }
    }

	private void validate(ValidationEventCollector handler)
			throws ValidationException {
		if(handler.hasEvents()) {
			ValidationEvent[] events = handler.getEvents();
			StringBuilder sb = new StringBuilder();
			for(ValidationEvent event : events) {
				sb.append(event.getMessage());
			}
			throw new ValidationException(sb.toString());
		}
	}

}
