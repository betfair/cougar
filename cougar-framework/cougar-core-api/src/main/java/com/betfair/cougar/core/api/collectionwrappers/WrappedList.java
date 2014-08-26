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
import java.util.List;
import java.util.ListIterator;

import com.betfair.cougar.api.Result;

public class WrappedList<E> implements List<E>, Result {
    private final List<E> underlyingList;

    public WrappedList(List<E> toBeWrapped) {
        underlyingList = toBeWrapped;
    }

    @Override
    public boolean add(E e) {
        return underlyingList.add(e);
    }

    @Override
    public void add(int index, E element) {
        underlyingList.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return underlyingList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return underlyingList.addAll(index, c);
    }

    @Override
    public void clear() {
        underlyingList.clear();
    }

    @Override
    public boolean contains(Object o) {
        return underlyingList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return underlyingList.containsAll(c);
    }

    @Override
    public E get(int index) {
        return underlyingList.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return underlyingList.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return underlyingList.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return underlyingList.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return underlyingList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return underlyingList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return underlyingList.listIterator(index);
    }

    @Override
    public boolean remove(Object o) {
        return underlyingList.remove(o);
    }

    @Override
    public E remove(int index) {
        return underlyingList.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return underlyingList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return underlyingList.retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        return underlyingList.set(index, element);
    }

    @Override
    public int size() {
        return underlyingList.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return underlyingList.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return underlyingList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return underlyingList.toArray(a);
    }

}
