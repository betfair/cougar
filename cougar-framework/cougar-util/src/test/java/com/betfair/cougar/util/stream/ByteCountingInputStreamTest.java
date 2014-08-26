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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class ByteCountingInputStreamTest extends TestCase {
    private byte[] source = "ABCDESGHIJKLMNOPQRSTUVWXYZ".getBytes();

    public void testReadSingleCount() throws Exception {
        ByteCountingInputStream bcis = new ByteCountingInputStream(new ByteArrayInputStream(source));

        int bytesRead = 0;
        while (bcis.read() > 0) {
            assertEquals(++bytesRead, bcis.getCount());
        }
        assertEquals(source.length, bcis.getCount());
    }

    public void testReadFullArray() throws Exception {
        ByteCountingInputStream bcis = new ByteCountingInputStream(new ByteArrayInputStream(source));

        int bytesRead = 0;
        byte[] dst = new byte[8];
        int c = 0;
        while ((c = bcis.read(dst)) > 0) {
            bytesRead += c;
            assertEquals(bytesRead, bcis.getCount());
        }
        assertEquals(source.length, bcis.getCount());
    }

    public void testReadPartArray() throws Exception {
        ByteCountingInputStream bcis = new ByteCountingInputStream(new ByteArrayInputStream(source));

        int bytesRead = 0;
        byte[] dst = new byte[8];
        int c = 0;
        while ((c = bcis.read(dst, 2, 3)) > 0) {
            bytesRead += c;
            assertEquals(bytesRead, bcis.getCount());
        }
        assertEquals(source.length, bcis.getCount());
    }

    public void testSkipPart() throws Exception {
        ByteCountingInputStream bcis = new ByteCountingInputStream(new ByteArrayInputStream(source));

        int bytesRead = 0;
        byte[] dst = new byte[8];
        int c = 0;
        bytesRead = (int)bcis.skip(11);
        while ((c = bcis.read(dst)) > 0) {
            bytesRead += c;
            assertEquals(bytesRead, bcis.getCount());
        }
        assertEquals(source.length, bcis.getCount());
    }
}
