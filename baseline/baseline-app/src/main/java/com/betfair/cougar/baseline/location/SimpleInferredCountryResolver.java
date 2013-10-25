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

package com.betfair.cougar.baseline.location;

import com.betfair.cougar.api.security.InferredCountryResolver;
import com.betfair.cougar.util.HeaderUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * An implementation of InferredCountryResolver which infers country code from
 * HTTP host headers
 */
public class SimpleInferredCountryResolver implements InferredCountryResolver<HttpServletRequest> {

    private static final String HOST_HEADER = "Host";

    private static final Map<Pattern, String> patternMap;
    static {
        patternMap = new HashMap<Pattern, String>();
        patternMap.put(Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\-\\.]*\\.(es)$"), "ES");
        patternMap.put(Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\-\\.]*\\.(it)$"), "IT");
        patternMap.put(Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\-\\.]*\\.(in)$"), "IN");
    }

    @Override
    public String inferCountry(HttpServletRequest request) {
        String hostHeader = request.getHeader(HOST_HEADER);
        if(hostHeader == null) {
            return null;
        }
        String host = HeaderUtils.cleanHeaderValue(hostHeader).trim();
        if(host.isEmpty()) {
            return null;
        }
        for (Map.Entry<Pattern, String> entry : patternMap.entrySet()) {
            if(entry.getKey().matcher(host).matches()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
