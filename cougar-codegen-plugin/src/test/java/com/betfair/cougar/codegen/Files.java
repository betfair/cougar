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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.betfair.cougar.codegen.resolver.DefaultSchemaCatalogSource;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import com.betfair.cougar.codegen.resolver.InterceptingResolver;

/**
 * File-related util stuff
 */
public class Files {

    public static final File baseDir = new File(System.getProperty("user.dir"));

    /**
     * Retrieve a File from the given resource name.
     */
    public static File fromResource(String resourceName) {

        URL url = Files.class.getClassLoader().getResource(resourceName);

        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error converting resource to URI: " + e, e);
        }
    }


    /**
     * Assert that the contents of the two given files are the same.ó
     */
    public static void compare(File a, File b) {

            String strA = readFile(a);
            String strB = readFile(b);

            assertEquals(strA, strB);
    }


    public static String readFile(File a) {

        try {
            return IOUtils.toString(new FileReader(a));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file '" + a + "' to string: " + e, e);
        }
    }

    /**
     * Init a resolver for use with unit test code.
     * <p>
     * Not strictly 'File' related but seems a reasonable-enough place to dump this util method.
     * @return
     */
	public static InterceptingResolver initResolver(Log log) {
		File catalog = new DefaultSchemaCatalogSource().getCatalog(new File(Files.baseDir, "target/test-wrk/schemas"), log);
    	return new InterceptingResolver(new SystemStreamLog(), null,
    					new String[] { catalog.getAbsolutePath() });
	}
}
