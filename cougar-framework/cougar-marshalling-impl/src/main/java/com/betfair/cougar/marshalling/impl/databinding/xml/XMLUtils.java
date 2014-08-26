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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.betfair.cougar.core.api.exception.PanicInTheCougar;

public class XMLUtils {

    public static Schema getSchema(JAXBContext context) {
        Result result = new ResultImplementation();
        SchemaOutputResolver outputResolver = new SchemaOutputResolverImpl(result);
        File tempFile = null;
        try {
            context.generateSchema(outputResolver);
            String schemaFile = result.getSystemId();
            if (schemaFile != null) {
                tempFile = new File(URI.create(schemaFile));
                String content = FileUtils.readFileToString(tempFile);

                // JAXB nicely generates a schema with element refs, unfortunately it also adds the nillable attribute to the
                // referencing element, which is invalid, it has to go on the target. this string manipulation is to move the
                // nillable attribute to the correct place, preserving it's value.
                Map<String, Boolean> referenceElementsWithNillable = new HashMap<>();
                BufferedReader br = new BufferedReader(new StringReader(content));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("<xs:element") && line.contains(" ref=\"") && line.contains(" nillable=\"")) {
                        // we've got a reference element with nillable set
                        int refStartIndex = line.indexOf(" ref=\"") + 6;
                        int refEndIndex = line.indexOf("\"", refStartIndex);
                        int nillableStartIndex = line.indexOf(" nillable=\"") + 11;
                        int nillableEndIndex = line.indexOf("\"",nillableStartIndex);
                        String ref = line.substring(refStartIndex, refEndIndex);
                        if (ref.startsWith("tns:")) {
                            ref = ref.substring(4);
                        }
                        String nillable = line.substring(nillableStartIndex, nillableEndIndex);
                        referenceElementsWithNillable.put(ref, Boolean.valueOf(nillable));
                    }
                }
                // if we got some hits, then we need to rewrite this schema
                if (!referenceElementsWithNillable.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    br = new BufferedReader(new StringReader(content));
                    while ((line = br.readLine()) != null) {
                        // these we need to remove the nillable section from
                        if (line.contains("<xs:element") && line.contains(" ref=\"") && line.contains(" nillable=\"")) {
                            int nillableStartIndex = line.indexOf(" nillable=\"");
                            int nillableEndIndex = line.indexOf("\"",nillableStartIndex+11);
                            line = line.substring(0, nillableStartIndex) + line.substring(nillableEndIndex+1);
                        }
                        else if (line.contains("<xs:element name=\"")) {
                            for (String key : referenceElementsWithNillable.keySet()) {
                                // this we need to add the nillable back to
                                String elementTagWithName = "<xs:element name=\""+key+"\"";
                                if (line.contains(elementTagWithName)) {
                                    int endOfElementName = line.indexOf(elementTagWithName) + elementTagWithName.length();
                                    line = line.substring(0, endOfElementName) + " nillable=\"" + referenceElementsWithNillable.get(key) + "\"" + line.substring(endOfElementName);
                                    break;
                                }
                            }
                        }
                        sb.append(line).append("\n");
                    }
                    content = sb.toString();
                }

                SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return sf.newSchema(new StreamSource(new StringReader(content)));
            }
        } catch (IOException e) {
            throw new PanicInTheCougar(e);
        } catch (SAXException e) {
            throw new PanicInTheCougar(e);
        } finally {
            if(tempFile!=null)tempFile.delete();
        }
        return null;
    }


    private static final class SchemaOutputResolverImpl extends SchemaOutputResolver {
        private final Result result;

        public SchemaOutputResolverImpl(Result result) {
            this.result = result;
        }

        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName)
                throws IOException {
            File schemaFile = File.createTempFile(suggestedFileName, "xsd");
            result.setSystemId(schemaFile.toURI().toString());
            return result;
        }
    }

    private static final class ResultImplementation extends StreamResult implements Result {
        private String systemId;

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }
    }

}
