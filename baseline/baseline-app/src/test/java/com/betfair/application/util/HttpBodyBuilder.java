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

package com.betfair.application.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class HttpBodyBuilder {
	
	private String wholeBody;
    private String start;
    private String end;
    private String repeat;
    private String separator;
    
    private static  String CHARSET = "utf-8";
    
    public HttpBodyBuilder(String wholeBody) {
        this.wholeBody = wholeBody;
    }

    public HttpBodyBuilder(String start, String repeat, String separator, String end) {
        this.start = start;
        this.repeat = repeat;
        this.separator = separator;
        this.end = end;
    }
    
    public ByteArrayInputStream buildBody(int numrepeats) {
        try {
            String body = getRepeatedStringIfNecessary(numrepeats);
            return new ByteArrayInputStream(body.getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }

        
    }
    public ByteArrayInputStream buildBodyToBytes(int numrepeats) {
        try {
            String body = getRepeatedStringIfNecessary(numrepeats);
            body = String.format(body, numrepeats);
            return new ByteArrayInputStream(body.getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }

    }
    
    private String getRepeatedStringIfNecessary(int numRepeats) {
        if (wholeBody != null) {
            return wholeBody;
        } else {
            // need to build it.
            StringBuilder sb = new StringBuilder(start);
            sb.append(repeat);
            for (int i = 1; i < numRepeats; ++i) {
                sb.append(separator);
                sb.append(repeat);
            }
            sb.append(end);
            return sb.toString();
        }
    }
}
