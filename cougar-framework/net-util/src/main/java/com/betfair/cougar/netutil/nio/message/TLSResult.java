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

package com.betfair.cougar.netutil.nio.message;

/**
 *
 */
public enum TLSResult {
    FAILED_NEGOTIATION((byte)0), PLAINTEXT((byte)1), SSL((byte)2);

    private byte value;

    private TLSResult(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static TLSResult getByValue(byte value) {
        for (TLSResult requirement : values()) {
            if (requirement.value == value) {
                return requirement;
            }
        }
        throw new IllegalArgumentException("Unknown value " + value);
    }
}
