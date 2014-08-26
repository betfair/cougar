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

package com.betfair.cougar.codegen.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.betfair.cougar.codegen.except.PluginException;

/**
 * Responsible for getting resources. Does what we'd normally expect a {@link ClassLoader} to do,
 * but internally is clever enough to look at the classpaths of both the plugin, as well as the
 * runtime classpath of the project itself. We need this because of the problems caused by
 * declaring dependencies as part of the plugin (see our mojo's javadoc for more detail).
 * <p>
 * See http://old.nabble.com/How-to-get-files-from-JAR-dependencies-in-a-plugin---td21914851.html
 * and http://maven.apache.org/guides/mini/guide-maven-classloading.html.
 */

public class ResourceLoader {

	/**
	 * A classloader which recognises the runtime classpath of the project. Initialised by passing
	 * in a list of URLs (the runtime classpath). Null means 'don't use me'
	 */
	private final ClassLoader runtimeClassLoader;

	/**
	 * Util constructor for a loader which will NOT look at the runtime classpath.
	 */
	public ResourceLoader() {
		this(null);
	}

	/**
	 * Constructor
	 *
	 * @param classpath the runtime classpath (obtained from the Mojo). If non-null, this classpath
	 * 			will be consulted for resources which could not be found under the plugin's
	 * 			classpath.
	 */
	public ResourceLoader(List<String> classpath) {
		if (classpath != null) {
			try {
				runtimeClassLoader = urlClassLoader(classpath);
			} catch (MalformedURLException e) {
				throw new PluginException("Error initialising resource loader: " + e, e);
			}
		}
		else {
			runtimeClassLoader = null;
		}
	}

	/**
	 * Same as {@link ClassLoader#getResourceAsStream(String)} except it will look at the runtime
	 * classpath if not in the plugin ClassLoader's classpath.
	 */
	public InputStream getResourceAsStream(String resourceName) {

		URL url = getResource(resourceName);
		try {
			return url != null ? url.openStream() : null;
		} catch (IOException e) {
			return null;	// this is what a normal ClassLoader does
		}
	}

	private URL getResource(String resourceName) {

		URL url = getClass().getClassLoader().getResource(resourceName);
		if (url == null) {
			// try the run time class path
			url = runtimeClassLoader.getResource(resourceName);
		}
		return url;
	}

    private ClassLoader urlClassLoader(List<String> runtimeClassPath) throws MalformedURLException {

    	URL[] urls = new URL[runtimeClassPath.size()];
    	int i = 0;
    	for(String s : runtimeClassPath) {
    		urls[i++] = new File(s).toURI().toURL();
    	}
    	return new URLClassLoader(urls);
	}
}
