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

package com.betfair.cougar.core.api.mediatype;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class MediaTypeHeaderProvider implements HeaderDelegate<MediaType> {
	private Map<String, MediaType> cacheParse =  new ConcurrentHashMap<String, MediaType>();
	private Map<MediaType, String> cacheFormat =  new ConcurrentHashMap<MediaType, String>();

    public MediaType fromString(String mType) {
    	mType = mType.trim().toLowerCase();
    	MediaType mediaType = cacheParse.get(mType);
    	if (mediaType == null) {
	        String type = null;
	        String subtype = null;
	        final int paramsStart =mType.indexOf(';');
	        String noParamPart = mType.substring(0, paramsStart == -1 ? mType.length() : paramsStart).trim();
            if(!MediaType.MEDIA_TYPE_WILDCARD.equals(noParamPart)) {
		        int i = noParamPart.indexOf('/');
		        if (i == -1) {
		            throw new IllegalArgumentException("Media type separator is missing");
		        }
		        type = noParamPart.substring(0, i).trim();
		        subtype = noParamPart.substring(i + 1, noParamPart.length()).trim();
	        }

	        Map<String, String> parameters = Collections.emptyMap();
	        if (paramsStart != -1) {
	            parameters = new HashMap<String, String>();
	            StringTokenizer st = new StringTokenizer(mType.substring(paramsStart + 1), ";");
	            while (st.hasMoreTokens()) {
	                String token = st.nextToken();
	                int equalSign = token.indexOf('=');
	                if (equalSign == -1) {
	                    throw new IllegalArgumentException("Wrong media type  parameter, seperator is missing");
	                }
	                parameters.put(token.substring(0, equalSign).trim(), token.substring(equalSign + 1).trim());
	            }

	        }
	        mediaType = new MediaType(type, subtype, parameters);
	        cacheParse.put(mType, mediaType);
    	}
    	return mediaType;
    }

    public String toString(final MediaType type) {
    	String mType = cacheFormat.get(type);
    	if (mType == null) {
	        StringBuilder sb = new StringBuilder();
	        sb.append(type.getType()).append('/').append(type.getSubtype());

	        Map<String, String> params = type.getParameters();
	        for (Map.Entry<String, String> entry : params.entrySet()) {
	            sb.append(';').append(entry.getKey()).append('=').append(entry.getValue());
	        }

	        mType = sb.toString().toLowerCase();
	        cacheFormat.put(type, mType);
    	}
    	return mType;
    }
}
