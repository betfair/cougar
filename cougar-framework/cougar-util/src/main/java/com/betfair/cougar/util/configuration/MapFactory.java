package com.betfair.cougar.util.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple factory to generate a map from the properties in Cougar's PropertyConfigurer.
 * Takes all properties starting with the prefix, strips the prefix and uses this as a key to the value of that property.
 */
public class MapFactory {

    private final String prefix;

    public MapFactory(String prefix) {
        if (!prefix.endsWith(".")) {
            prefix += ".";
        }
        this.prefix = prefix;
    }

    public Map<String, String> create() {
        Map<String,String> ret = new HashMap<>();
        Map<String,String> allProps = PropertyConfigurer.getAllLoadedProperties();
        for (Map.Entry<String,String> entry : allProps.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                ret.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return ret;
    }
}
