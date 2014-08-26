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

package com.betfair.cougar.codegen.resolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.betfair.cougar.codegen.except.PluginException;
import com.betfair.cougar.codegen.resource.ResourceLoader;

/**
 * Our own custom resolver which knows to go looking for include files (*.inc) as resources, if
 * a flag is set.
 * <p>
 * Note that this overrides {@link XMLCatalogResolver} and that we still (for now) rely on the
 * older catalog functionality to work with the underlying schemas. We should be able to extend
 * this resolver to locate those schemas (as resources), and move away from the write-to-disk-
 * and-catalog functionality completely. TODO
 */
public class InterceptingResolver extends XMLCatalogResolver {

	private final Log log;

	/**
	 * The resource loader can find resources in places a plain ClassLoader couldn't
	 */
	private final ResourceLoader resourceLoader;

	public InterceptingResolver() {
		this(new SystemStreamLog(), null, new String[0]);
	}

	/**
	 * @param log mojo logger
	 * @param resourceLoader finds resources. If null, means should be used (ie. don't do
	 * 		resource-based loading)
	 * @param catalogs catalogs to be used by underlying resolver
	 */
	public InterceptingResolver(Log log, ResourceLoader resourceLoader, String[] catalogs) {
		super(catalogs);
		for (String f : catalogs) {
			if (!new File(f).exists()) {
				throw new PluginException("Given nonexistent catalog: " + f);
			}
		}
		this.log = log;
		this.resourceLoader = resourceLoader;
	}

	@Override
    public InputSource resolveEntity (
    			    String name,
    			    String publicId,
    			    String baseURI,
    			    String systemId)
	throws SAXException, IOException {

		try {
			if (shouldLoadAsResource(systemId)) {
				log.debug("Loading entity '" + systemId + "' as resource");
				return resourceToInputSource(publicId, systemId);
			}
			else {
				return super.resolveEntity(publicId, systemId);
			}
		}
		catch (Exception e) {
			// not in spec but too bad
			throw new PluginException("Error resolving entity: " + systemId + ": " + e, e);
		}
	}

	@Override
	public LSInput resolveResource(
					String type,
					String namespaceURI,
					String publicId,
					String systemId,
					String baseURI) {

		try {
			if (shouldLoadAsResource(systemId)) {
				log.debug("Loading resource '" + systemId + "' as resource");
				return resourceToLSInput(publicId, systemId);
			}
			else {
                return super.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
			}
		}
		catch (Exception e) {
			// this is cheating, the spec says allow for an exception, but we don't care
			throw new PluginException("Error resolving resource: " + systemId + ": " + e, e);
		}
	}

	private InputStream resourceAsStream(String systemId) {

		String resource = resourceFromId(systemId);
		InputStream is = resourceLoader.getResourceAsStream(resource);
		if (is == null) {
			// worth logging since we might otherwise miss this
			log.warn("Resource '" + resource + "' not found.");
		}
		return is;
	}

	private String resourceFromId(String systemId) {
		int endPos = systemId.lastIndexOf('/');
		return endPos > 0 ? systemId.substring(endPos) : systemId;
	}

	private InputSource resourceToInputSource(String publicId, String systemId) {

		InputStream is = resourceAsStream(systemId);

		if (is != null) {
			InputSource s = new InputSource(is);
			s.setPublicId(publicId);
			s.setSystemId(systemId);

			return s;
		}
		else {
			return null;
		}
	}

	private LSInput resourceToLSInput(String publicId, String systemId) {

		InputStream is = resourceAsStream(systemId);

		if (is != null) {
			DOMInputImpl result = new DOMInputImpl(); // any old impl would do
			result.setByteStream(is);
			result.setCharacterStream(null);
			result.setPublicId(publicId);
			result.setSystemId(systemId);

			return result;
		}
		else {
			return null;
		}
	}

	private boolean shouldLoadAsResource(String systemId) {
		return (resourceLoader != null) && systemId != null && systemId.endsWith(".inc");
	}
}
