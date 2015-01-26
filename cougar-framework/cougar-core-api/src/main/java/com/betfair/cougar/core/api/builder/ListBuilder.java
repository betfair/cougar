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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Builder implementation for lists. Initial list by default is a LinkedList, but this can
 * be changed using the toXXX() methods.
 */
public class ListBuilder<T> implements Builder<List<T>> {
    private List<T> ret;
    private boolean seal = true;

    public ListBuilder() {
        ret = new LinkedList<>();
    }

    public ListBuilder(List<T> list) {
        ret = list;
    }

    public ListBuilder<T> add(Builder<T> value) {
        add(value.build());
        return this;
    }

    public ListBuilder<T> add(T value) {
        ret.add(value);
        return this;
    }

    public ListBuilder<T> toArrayList() {
        ret = new ArrayList<>(ret);
        return this;
    }

    public ListBuilder<T> toVector() {
        ret = new Vector<>(ret);
        return this;
    }

    public ListBuilder<T> toCopyOnWriteArrayList() {
        ret = new CopyOnWriteArrayList<>(ret);
        return this;
    }

    public ListBuilder<T> leaveModifiable() {
        seal = false;
        return this;
    }

    @Override
    public List<T> build() {
        if (seal) {
            ret = Collections.unmodifiableList(ret);
        }
        return ret;
    }
}
