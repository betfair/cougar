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

package com.betfair.testing.utils.cougar.misc;


import java.io.InputStream;

public class InputStreamHelpers {
	
	/**
	 * 
	 * 
	 * @param relativePath
	 * @return
	 */
	public static InputStream getInputStreamForResource(String relativePath){
		
		String resourcePath = null;
		InputStream is = null;
		
		//ClassLoader loader = getClass().getClassLoader();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        
		resourcePath = relativePath.replace("\\", "/");

        is = loader.getResourceAsStream(resourcePath);

        if(is == null){
        	throw new RuntimeException("Unable to create an input stream as the following resoure was mot found from the classpath " + resourcePath);
        }
        
		return is;
		
	}

}
