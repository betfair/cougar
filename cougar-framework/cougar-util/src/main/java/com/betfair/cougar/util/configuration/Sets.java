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

package com.betfair.cougar.util.configuration;

import java.util.*;

public class Sets {

    /**
     * Given a map and a set of keys, return a set containing the values referred-to by the keys.
     * If the map is null or the keys are null, the EMPTY_SET is returned.
     * If a passed key does not exist in the map, nothing is added to the set.
     * However, if the key is present and maps to null, null is added to the set.
     */
    public static final Set fromMap(Map map, Object... keys) {
        if (keys != null && map != null) {
            Set answer = new HashSet();
            for (Object key : keys) {
                if (map.containsKey(key)) {
                    answer.add(map.get(key));
                }
            }
            return Collections.unmodifiableSet(answer);
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Given a comma-separated list of values, return a set of those values.
     * If the passed string is null, the EMPTY_SET is returned.
     * If the passed string is empty, the EMPTY_SET is returned.
     */
    public static final Set<String> fromCommaSeparatedValues(String csv) {
        if (csv == null || csv.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        String[] tokens = csv.split(",");
        return new HashSet<String>(Arrays.asList(tokens));
    }
}
