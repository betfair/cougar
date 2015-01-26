/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.core.api.builder;

import java.util.*;

/**
 * Builder implementation for maps. Initial map by default is a HashMap, but this can
 * be changed using the toXXX() methods.
 */
public class MapBuilder<K,V> implements Builder<Map<K,V>> {
    private Map<K,V> ret;
    private boolean seal = true;

    public MapBuilder() {
        ret = new HashMap<>();
    }

    public MapBuilder(Map<K,V> set) {
        ret = set;
    }

    public MapBuilder<K,V> put(Builder<K> key, Builder<V> value) {
        put(key.build(), value.build());
        return this;
    }

    public MapBuilder<K,V> put(Builder<K> key, V value) {
        put(key.build(), value);
        return this;
    }

    public MapBuilder<K,V> put(K key, Builder<V> value) {
        put(key, value.build());
        return this;
    }

    public MapBuilder<K,V> put(K key, V value) {
        ret.put(key,value);
        return this;
    }

    public MapBuilder<K,V> toHashtable() {
        ret = new Hashtable<>(ret);
        return this;
    }

    public MapBuilder<K,V> toIdentityHashMap() {
        ret = new IdentityHashMap<>(ret);
        return this;
    }

    public MapBuilder<K, V> leaveModifiable() {
        seal = false;
        return this;
    }

    @Override
    public Map<K,V> build() {
        if (seal) {
            ret = Collections.unmodifiableMap(ret);
        }
        return ret;
    }
}
