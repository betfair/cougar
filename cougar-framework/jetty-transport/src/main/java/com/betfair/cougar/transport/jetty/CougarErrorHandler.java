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

package com.betfair.cougar.transport.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.handler.ErrorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CougarErrorHandler extends ErrorHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(CougarErrorHandler.class);

	public static final int BUFFSIZE = 1024;

	@Override
    protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks)
    	throws IOException
    {
		// We don't want to show generic Jetty error pages.
		LOGGER.debug("Finding error page for code %d", code);
		InputStream is = getClass().getResourceAsStream("/errorpages/"+code+".html");
		if (is != null) {
			InputStreamReader reader = new InputStreamReader(is);
			try {
				int len;
				char[] buff = new char[BUFFSIZE];
				while ((len = reader.read(buff, 0, BUFFSIZE)) != -1) {
					writer.write(buff, 0, len);
				}
				writer.flush();
			}
			finally {
				reader.close();
			}
		}
    }

}

