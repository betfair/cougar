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

package com.betfair.cougar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;

/**
 */
public class ServletResponseFileStreamer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletResponseFileStreamer.class);

    public static final String FILE_NOT_FOUND_PAGE = "/errorpages/404.html";

    public static final int BUFFSIZE = 10240;

    public static final ServletResponseFileStreamer theInstance = new ServletResponseFileStreamer();

    public static ServletResponseFileStreamer getInstance() {
        return theInstance;
    }

    private ServletResponseFileStreamer() {}

    public int stream404ToResponse(HttpServletResponse response) throws IOException {
        return streamFileToResponse(getClass().getResourceAsStream(FILE_NOT_FOUND_PAGE), response, HttpServletResponse.SC_NOT_FOUND, null, null);
    }

    public int streamFileToResponse(InputStream rawStream, HttpServletResponse response, int httpReturnCode, String contentType, String[][] headers) throws IOException {
        int bytesWritten = 0;
        BufferedInputStream htmlStream = new BufferedInputStream(rawStream); // won't throw
        try {
            // cache it a bit if it is not a 404
            if (httpReturnCode == HttpServletResponse.SC_OK) {
                if (headers != null) {
                    for (String[] headerPair : headers) {
                        if (headerPair.length >= 2) {
                            response.addHeader(headerPair[0], headerPair[1]);
                            LOGGER.debug( "Adding the following http header value [{}, {}]", headerPair[0], headerPair[1]);
                        } else {
                            LOGGER.warn("Unable to add header as " + Arrays.toString(headerPair) + " is not of the correct size/count");
                        }
                    }
                }
                if (contentType != null) {
                    response.setContentType(contentType);
                }
            }

            OutputStream out = response.getOutputStream();
            int len;
            byte[] buff = new byte[BUFFSIZE];
            while ((len = htmlStream.read(buff, 0, BUFFSIZE)) != -1) {
                out.write(buff, 0, len);
                bytesWritten += len;
            }
            response.setStatus(httpReturnCode);
        }
        finally {
            htmlStream.close();
            response.flushBuffer();
        }
        return bytesWritten;
    }
}
