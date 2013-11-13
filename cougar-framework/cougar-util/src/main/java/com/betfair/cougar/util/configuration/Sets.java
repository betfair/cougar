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
