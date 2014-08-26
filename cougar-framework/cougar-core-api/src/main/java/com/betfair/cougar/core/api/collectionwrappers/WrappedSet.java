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
import java.util.Iterator;
import java.util.Set;

import com.betfair.cougar.api.Result;

public class WrappedSet<E> implements Set<E>, Result {
    private final Set<E> underlyingSet;

    public WrappedSet(Set<E> toBeWrapped) {
        underlyingSet = toBeWrapped;
    }

    @Override
    public boolean add(E e) {
        return underlyingSet.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return underlyingSet.addAll(c);
    }

    @Override
    public void clear() {
        underlyingSet.clear();
    }

    @Override
    public boolean contains(Object o) {
        return underlyingSet.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return underlyingSet.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return underlyingSet.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return underlyingSet.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return underlyingSet.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return underlyingSet.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return underlyingSet.retainAll(c);
    }

    @Override
    public int size() {
        return underlyingSet.size();
    }

    @Override
    public Object[] toArray() {
        return underlyingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return underlyingSet.toArray(a);
    }

}
