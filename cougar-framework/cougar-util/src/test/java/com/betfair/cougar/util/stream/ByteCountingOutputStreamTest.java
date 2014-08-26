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

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public class ByteCountingOutputStreamTest extends TestCase {
    private byte[] source = "ABCDESGHIJKLMNOPQRSTUVWXYZ".getBytes();
    private ByteArrayOutputStream destination = new ByteArrayOutputStream();

    public void testWriteSingleCount() throws Exception {
        ByteCountingOutputStream bcos = new ByteCountingOutputStream(destination);

        int bytesWritten = 0;
        for (int i = 0; i < source.length; i++) {
            bcos.write(source[i]);
            assertEquals(++bytesWritten, bcos.getCount());
        }
        assertEquals(source.length, bcos.getCount());
        int count=0;
        for (byte b: source) {
            assertEquals(destination.toByteArray()[count++], b);
        }
    }

    public void testWriteFullArray() throws Exception {
        ByteCountingOutputStream bcos = new ByteCountingOutputStream(destination);

        int bytesWritten = 0;
        int numberToCopy = 8;
        for (int i = 0; i < source.length; i+=numberToCopy) {
            if (numberToCopy  + i > source.length) {
                numberToCopy = source.length - i;
            }
            byte[] src = new byte[numberToCopy];
            System.arraycopy(source, i, src, 0, numberToCopy);
            bcos.write(src);
            bytesWritten += numberToCopy;
            assertEquals(bytesWritten, bcos.getCount());
        }

        assertEquals(source.length, bcos.getCount());
        int count=0;
        for (byte b: source) {
            assertEquals(destination.toByteArray()[count++], b);
        }
    }

    public void testWritePartArray() throws Exception {
        ByteCountingOutputStream bcos = new ByteCountingOutputStream(destination);

        int bytesWritten = 0;
        int numberToCopy = 5;
        for (int i = 0; i < source.length; i+=numberToCopy) {
            byte[] src = new byte[8];
            if (numberToCopy  + i > source.length) {
                numberToCopy = source.length - i;
            }
            System.arraycopy(source, i, src, 1, numberToCopy);
            bcos.write(src, 1, numberToCopy);
            bytesWritten += numberToCopy;
            assertEquals(bytesWritten, bcos.getCount());
        }

        assertEquals(source.length, bcos.getCount());
        int count=0;
        for (byte b: source) {
            assertEquals(destination.toByteArray()[count++], b);
        }
    }
}
