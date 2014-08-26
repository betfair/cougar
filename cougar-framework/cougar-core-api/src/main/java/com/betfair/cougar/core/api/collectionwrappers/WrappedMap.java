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

package com.betfair.cougar.core.api.collectionwrappers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.betfair.cougar.api.Result;

/**
 * A delegating map implementing the Result interface.
 */
public class WrappedMap<K,V> implements Map<K,V>, Result{
    private final Map<K,V> underlyingMap;

    public WrappedMap(Map<K,V> toBeWrapped) {
        underlyingMap = toBeWrapped;
    }

    @Override
    public void clear() {
        underlyingMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return underlyingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return underlyingMap.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return underlyingMap.entrySet();
    }

    @Override
    public V get(Object key) {
        return underlyingMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return underlyingMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return underlyingMap.keySet();
    }

    @Override
    public V put(K key, V value) {
        return underlyingMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        underlyingMap.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return underlyingMap.remove(key);
    }

    @Override
    public int size() {
        return underlyingMap.size();
    }

    @Override
    public Collection<V> values() {
        return underlyingMap.values();
    }
}

