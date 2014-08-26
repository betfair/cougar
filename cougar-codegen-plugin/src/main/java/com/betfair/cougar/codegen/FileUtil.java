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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class FileUtil {

	/**
	 * Copy the given resource to the given file.
	 *
	 * @param resourceName name of resource to copy
	 * @param destination file
	 */
	public static void resourceToFile(String resourceName, File dest, Class src) {

		InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = src.getClassLoader().getResourceAsStream(resourceName);
	        if (is == null) {
	            throw new RuntimeException("Could not load resource: " + resourceName);
	        }
	        dest.getParentFile().mkdirs();
	        os = new FileOutputStream(dest);

	        IOUtils.copy(is, os);

	    } catch (Exception e) {
	    	throw new RuntimeException("Error copying resource '" + resourceName + "' to file '"
	    					+ dest.getPath() + "': "+ e, e);
		}
	    finally {
	        IOUtils.closeQuietly(is);
	        IOUtils.closeQuietly(os);
	    }
	}
}
