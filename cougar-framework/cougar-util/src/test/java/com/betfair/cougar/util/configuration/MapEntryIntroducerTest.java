package com.betfair.cougar.util.configuration;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;


/**
 *
 */
public class MapEntryIntroducerTest {
    @Test
    public void singleEntry() throws Exception {
        Map<String,String> map = new HashMap<>();
        new MapEntryIntroducer<>(map, "key", "value");

        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }
    @Test
    public void multiEntry() throws Exception {
        Map<String,String> map = new HashMap<>();
        new MapEntryIntroducer<>(map, "key1", "value1");
        new MapEntryIntroducer<>(map, "key2", "value2");

        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }
}
