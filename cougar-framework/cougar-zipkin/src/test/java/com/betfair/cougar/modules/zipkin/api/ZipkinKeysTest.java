/*
 * Copyright 2015, The Sporting Exchange Limited
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

package com.betfair.cougar.modules.zipkin.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ZipkinKeysTest {

    @Test
    public void sampledToString_WhenSampledIsNull_ShouldReturnNull() {
        assertEquals(null, ZipkinKeys.sampledToString(null));
    }

    @Test
    public void sampledToString_WhenSampledIsTrue_ShouldReturnQuoted1() {
        assertEquals("1", ZipkinKeys.sampledToString(Boolean.TRUE));
    }

    @Test
    public void sampledToString_WhenSampledIsFalse_ShouldReturnQuoted0() {
        assertEquals("0", ZipkinKeys.sampledToString(Boolean.FALSE));
    }

    @Test
    public void sampledToBoolean_WhenSampledIsNull_ShouldReturnNull() {
        assertEquals(null, ZipkinKeys.sampledToBoolean(null));
    }

    @Test
    public void sampledToBoolean_WhenSampledIsQuoted1_ShouldReturnTrue() {
        assertEquals(true, ZipkinKeys.sampledToBoolean("1"));
    }

    @Test
    public void sampledToBoolean_WhenSampledIsQuoted0_ShouldReturnFalse() {
        assertEquals(false, ZipkinKeys.sampledToBoolean("0"));
    }
}
