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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HeaderUtils {

	public static final String ACCEPT = "Accept";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String USER_AGENT = "User-Agent";
	public static final String NO_CACHE = "no-cache";

	public static String getAccept(HttpServletRequest request) {
		return cleanHeaderValue(request.getHeader(ACCEPT));
	}

	public static String getAcceptEncoding(HttpServletRequest request) {
		return cleanHeaderValue(request.getHeader(ACCEPT_ENCODING));
	}

	public static String getCacheControl(HttpServletRequest request) {
		return cleanHeaderValue(request.getHeader(CACHE_CONTROL));
	}

	public static String getUserAgent(HttpServletRequest request) {
		return cleanHeaderValue(request.getHeader(USER_AGENT));
	}

	public static void setContentEncoding(HttpServletResponse response, String contentEncoding) {
		response.setHeader(CONTENT_ENCODING, contentEncoding);
	}

	public static void setCacheControl(HttpServletResponse response, String cacheControl) {
		response.setHeader(CACHE_CONTROL, cacheControl);
	}

	public static void setNoCache(HttpServletResponse response) {
		response.setHeader(CACHE_CONTROL, NO_CACHE);
	}

    public static String cleanHeaderValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.replace("\n"," ");
        value = value.replace("\t"," ");
        return value;
    }

}
