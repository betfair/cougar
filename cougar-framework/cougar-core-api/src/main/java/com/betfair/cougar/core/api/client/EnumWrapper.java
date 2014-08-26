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

package com.betfair.cougar.core.api.client;

import com.betfair.cougar.core.api.transcription.EnumUtils;

/**
 *
 */
public class EnumWrapper<T extends Enum<T>> {
    private T value;
    private String rawValue;

    public EnumWrapper(T value) {
        setValue(value);
    }

    public EnumWrapper(Class<T> clazz, String rawValue) {
        setRawValue(clazz, rawValue);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (value != null && value.name().equals("UNRECOGNIZED_VALUE")) {
            throw new IllegalArgumentException("UNRECOGNIZED_VALUE reserved for soft enum deserialisation handling");
        }
        this.value = value;
        this.rawValue = value != null ? value.name() : null;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(Class<T> clazz, String rawValue) {
        T value = rawValue != null ? EnumUtils.readEnum(clazz, rawValue) : null;
        this.value = value;
        this.rawValue = rawValue;
    }
}
