package com.betfair.cougar.util.configuration;

import java.util.Map;

/**
 * Introduces (puts) a kv pair into a map. Useful in Spring land.
 */
public class MapEntryIntroducer<K,V> {

    public MapEntryIntroducer(Map<K, V> target, K key, V value) {
        target.put(key, value);
    }
}
