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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Builder implementation for sets. Initial set by default is a HashSet, but this can
 * be changed using the toXXX() methods.
 */
public class SetBuilder<T> implements Builder<Set<T>> {
    private Set<T> ret;
    private boolean seal = true;

    public SetBuilder() {
        ret = new HashSet<>();
    }

    public SetBuilder(Set<T> set) {
        ret = set;
    }

    public SetBuilder<T> add(Builder<T> value) {
        add(value.build());
        return this;
    }

    public SetBuilder<T> add(T value) {
        ret.add(value);
        return this;
    }

    public SetBuilder<T> leaveModifiable() {
        seal = false;
        return this;
    }

    @Override
    public Set<T> build() {
        if (seal) {
            ret = Collections.unmodifiableSet(ret);
        }
        return ret;
    }

    public SetBuilder<T> toLinkedHashSet() {
        ret = new LinkedHashSet<>(ret);
        return this;
    }

    public SetBuilder<T> toConcurrentSkipListSet() {
        ret = new ConcurrentSkipListSet<>(ret);
        return this;
    }

    public SetBuilder<T> toCopyOnWriteArraySet() {
        ret = new CopyOnWriteArraySet<>(ret);
        return this;
    }
}
