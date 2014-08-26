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

import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class XMLMarshaller implements Marshaller, FaultMarshaller {
    static {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
    }
    private static final XMLOutputFactory factory = XMLOutputFactory.newInstance();
    private static ConcurrentMap<String,JAXBContext> jaxbContexts = new ConcurrentHashMap<>();

    XMLMarshaller() {

    }

	@Override
	public String getFormat() {
		return "xml";
	}

	@Override
	public void marshall(final OutputStream outputStream, final Object result, String encoding, boolean client) {
		XMLStreamWriter xmlWriter = null;
        try {
			xmlWriter = factory.createXMLStreamWriter(outputStream);
			String resultPackage = result.getClass().getPackage().getName();
			JAXBContext jc = getJAXBContext(resultPackage);
            final javax.xml.bind.Marshaller marshaller = jc.createMarshaller();
            if (encoding!=null) {
                marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, encoding);
            }
			marshaller.marshal(result, xmlWriter);
        } catch (final XMLStreamException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to stream object to XML", e, client);
        } catch (final JAXBException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to marshal object to XML", e, client);
        } finally {
        	if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (final XMLStreamException ignored) {}
        	}
        }


	}

	@Override
	public void marshallFault(final OutputStream outputStream, final CougarFault fault, String encoding) {
		XMLStreamWriter xmlWriter = null;
        try {
			xmlWriter = factory.createXMLStreamWriter(outputStream);
			xmlWriter.writeStartDocument(encoding, "1.0");
	        xmlWriter.writeStartElement("fault");//start soap body

            writeElement("faultcode",fault.getFaultCode().name(),xmlWriter);
            writeElement("faultstring",fault.getErrorCode(),xmlWriter);

	        writeFaultDetail( fault, xmlWriter);

		    xmlWriter.writeEndElement();
		    xmlWriter.writeEndDocument();

        } catch (final XMLStreamException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to stream fault "+fault.getClass()+" to XML", e, false);
        } catch (final JAXBException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to marshal fault "+fault.getClass()+" to XML", e, false);
        }
        finally {
        	if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (final XMLStreamException ignored) {}
        	}
        }


	}

    private void writeElement(String name,String data, XMLStreamWriter xmlWriter) throws XMLStreamException, JAXBException {
    		xmlWriter.writeStartElement(name);
		    xmlWriter.writeCharacters(data);
		    xmlWriter.writeEndElement();
    }


	private void writeFaultDetail(CougarFault cougarFault, XMLStreamWriter xmlWriter) throws XMLStreamException, JAXBException {
	    xmlWriter.writeStartElement("detail");
	    FaultDetail detail = cougarFault.getDetail();
	    if(detail != null ) {

	        List<String[]> faultMessages = detail.getFaultMessages();
	        if (faultMessages != null) {
                writeElement("exceptionname", detail.getFaultName(), xmlWriter);
		        xmlWriter.writeStartElement(detail.getFaultName());
	        	for (String[] msg: faultMessages) {
                    writeElement(msg[0],msg[1],xmlWriter);
	        	}
		        xmlWriter.writeEndElement();
	        }
        	if (FaultController.getInstance().isDetailedFaults()) {
                writeElement("trace",detail.getStackTrace(),xmlWriter);
                writeElement("message",detail.getDetailMessage(),xmlWriter);
        	}
	    }
	    xmlWriter.writeEndElement();
	}


	private static JAXBContext getJAXBContext(String namespaces) throws JAXBException {
		JAXBContext jc = jaxbContexts.get(namespaces);
		if(jc == null) {
		    jc = JAXBContext.newInstance(namespaces);
		    JAXBContext prev = jaxbContexts.putIfAbsent(namespaces, jc);
		    if (prev != null){
		    	jc = prev;
		    }
		}
		return jc;

	}
}
