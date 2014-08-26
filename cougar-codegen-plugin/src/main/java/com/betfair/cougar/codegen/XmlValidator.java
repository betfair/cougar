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

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;

import com.betfair.cougar.codegen.except.PluginException;
import com.betfair.cougar.codegen.resolver.InterceptingResolver;

/**
 * Does schema validation of XML documents (replacing xml-maven-plugin, which can't handle
 * include files).
 * <p>
 * <h2>A note on catalogs</h2>
 * <p>
 * We realised (the hard way) that because Interface.xsd (which drives service definition validation)
 * references the w3.org xml spec (for xi:include support), our build server was constantly hitting
 * www.w3.org, and when we started getting timeouts to that server, our builds starting failing.
 * <p>
 * This validator now uses a {@link org.apache.xerces.util.XMLCatalogResolver} so we can retrieve schemas locally.
 * See http://xml.apache.org/commons/components/resolver/resolver-article.html.
 */
public class XmlValidator {

	private final InterceptingResolver resolver;


    public XmlValidator(InterceptingResolver resolver) {
    	this.resolver = resolver;
	}

	/**
     * Validate the given xmlDocument, using any schemas specified in the document itself.
     *
     * @throws PluginException for any validation or IO errors.
     */
    public void validate(Document doc) {
    	try {
    		doValidate(doc);
    	}
        catch (Exception e) {
            throw new PluginException("Error validating document: " + e, e);
        }
    }

    private void doValidate(Document doc) throws Exception {

        // preparsed schema seems to be the only way to get xi:include working,
        // see http://www.xml.com/lpt/a/1597
        DOMSource source = new DOMSource(doc);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema();
        javax.xml.validation.Validator validator = schema.newValidator();

        // see notes in javadoc above
        validator.setResourceResolver(resolver);

        validator.validate(source);
    }
}
