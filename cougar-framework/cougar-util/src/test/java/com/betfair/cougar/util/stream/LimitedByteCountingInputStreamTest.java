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

package com.betfair.cougar.util.stream;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class LimitedByteCountingInputStreamTest {

    private byte[] source = "ABCDESGHIJKLMNOPQRSTUVWXYZ".getBytes();

    @Test
    public void testReadFullArray_UnderMaximum() throws Exception {
        LimitedByteCountingInputStream bcis = new LimitedByteCountingInputStream(new ByteArrayInputStream(source), 1000);

        int bytesRead = 0;
        byte[] dst = new byte[8];
        int c = 0;
        while ((c = bcis.read(dst)) > 0) {
            bytesRead += c;
            assertEquals(bytesRead, bcis.getCount());
        }
        assertEquals(source.length, bcis.getCount());
    }

    @Test(expected = IOException.class)
    public void testReadFullArray_OverMaximum() throws Exception {
        LimitedByteCountingInputStream bcis = new LimitedByteCountingInputStream(new ByteArrayInputStream(source), 10);

        int bytesRead = 0;
        byte[] dst = new byte[8];
        int c = 0;
        while ((c = bcis.read(dst)) > 0) {
            bytesRead += c;
            assertEquals(bytesRead, bcis.getCount());
        }
    }
}
