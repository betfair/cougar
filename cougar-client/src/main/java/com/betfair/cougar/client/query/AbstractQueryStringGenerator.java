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

package com.betfair.cougar.client.query;

import java.util.Map;

/**
 * Base class for query string generation
 */
public abstract class AbstractQueryStringGenerator implements QueryStringGenerator {

    @Override
    public String generate(Map<String, Object> queryParmMap) {
        StringBuilder queryString = new StringBuilder();

        if (!queryParmMap.isEmpty()) {
            queryString.append("?");
            for (Map.Entry<String, Object>  entry : queryParmMap.entrySet()) {
                if (entry.getKey().equals("")) {
                    throw new IllegalArgumentException("Expected a non-empty key");
                }
                if (queryString.length() > 1) {
                    queryString.append("&");
                }
                queryString.append(entry.getKey());
                queryString.append("=");
                queryString.append(parseValue(entry.getValue()));
            }
        }
        return queryString.toString();
    }

    protected abstract String parseValue(Object o);
}
