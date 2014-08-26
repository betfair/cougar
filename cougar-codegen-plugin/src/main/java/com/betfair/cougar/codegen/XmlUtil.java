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

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;

import com.betfair.cougar.codegen.except.PluginException;


public class XmlUtil {

    /**
     * Parse an XML document (with appropriate flags for our purposes) from the given stream.
     *
     * @param is input stream containing the document
     * @param resolver resolver to be used for parsing
     *
     * @throws PluginException for all errors
     */
    public static Document parse(InputStream is, EntityResolver resolver) {

        try {
            DocumentBuilder parser = createParser(resolver);
            return parser.parse(is);
        }
        catch (Exception e) {
            throw new PluginException("Error parsing XML document from input stream: " + e, e);
        }
    }

    /**
     * Parse an XML document (with appropriate flags for our purposes) from the file.
     *
     * @param xmlFile file to be parsed.
     * @param resolver resolver to be used for parsing
     * @throws PluginException for all errors
     */
    public static Document parse(File xmlFile, EntityResolver resolver) {

        try {
            DocumentBuilder parser = createParser(resolver);
            return parser.parse(xmlFile);
        }
        catch (Exception e) {
            throw new PluginException("Error parsing XML document '" + xmlFile + "': " + e, e);
        }
    }

    private static DocumentBuilder createParser(EntityResolver resolver) throws Exception {

    	DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();

    	// standard flags needed for our parsing
    	dbfactory.setNamespaceAware(true);
    	dbfactory.setXIncludeAware(true);

    	DocumentBuilder parser = dbfactory.newDocumentBuilder();
    	parser.setEntityResolver(resolver);
    	return parser;
    }
}
