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

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test case for ByteCountingDataInput class
 */
public class ByteCountingDataInputTest {
    private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    @Test
    public void testBoolean() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeBoolean(true);
        output.writeBoolean(false);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertTrue(bcdi.readBoolean());
        assertFalse(bcdi.readBoolean());
        assertEquals("Incorrect byte count returned", 2, bcdi.getCount());
    }

    @Test
    public void testByte() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeByte('7');

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(55, bcdi.readByte());
        assertEquals("Incorrect byte count returned", 1, bcdi.getCount());
    }

    @Test
    public void testChar() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeChar('7');

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals('7', bcdi.readChar());
        assertEquals("Incorrect byte count returned", 2, bcdi.getCount());
    }

    @Test
    public void testDouble() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        double d = 2.3456;
        output.writeDouble(d);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(d, bcdi.readDouble(), 0);
        assertEquals("Incorrect byte count returned", 8, bcdi.getCount());
    }

    @Test
    public void testFloat() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        float f = 3.21F;
        output.writeFloat(f);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(f, bcdi.readFloat(), 0);
        assertEquals("Incorrect byte count returned", 4, bcdi.getCount());
    }

    @Test
    public void testReadFully() throws Exception {
        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(alphabet.getBytes())));

        byte[] bytes = new byte[26];
        bcdi.readFully(bytes);
        assertEquals(26, bcdi.getCount());
        assertArrayEquals(alphabet.getBytes(), bytes);
    }

    @Test
    public void testReadFully2() throws Exception {
        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(alphabet.getBytes())));

        byte[] bytes = new byte[10];
        bcdi.readFully(bytes, 2, 8);
        assertEquals(8, bcdi.getCount());
        assertArrayEquals(Arrays.copyOfRange(alphabet.getBytes(), 0, 8), Arrays.copyOfRange(bytes, 2, 10));
    }

    @Test
    public void testInt() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeInt(3);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(3, bcdi.readInt());
        assertEquals("Incorrect byte count returned", 4, bcdi.getCount());
    }

    @Test
    public void testReadLine() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        String expectedString = "A perfectly formed sentence";
        String nextExpectedString = "With other nonsense on the end";

        String theString = expectedString + "\r\n" + nextExpectedString + "\n";
        output.write(theString.getBytes());


        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(expectedString, bcdi.readLine());
        long count= bcdi.getCount();
        assertEquals("Incorrect byte count returned", expectedString.getBytes().length, count);

        assertEquals(nextExpectedString, bcdi.readLine());
        assertEquals("Incorrect byte count returned", nextExpectedString.getBytes().length, bcdi.getCount() - count);
    }

    @Test
    public void testLong() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        long val = (long)Math.pow(2, 60);
        output.writeLong(val);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(val, bcdi.readLong());
        assertEquals("Incorrect byte count returned", 8, bcdi.getCount());
    }

    @Test
    public void testShort() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        short val = -395;
        output.writeShort(val);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        assertEquals(val, bcdi.readShort());
        assertEquals("Incorrect byte count returned", 2, bcdi.getCount());
    }

    @Test
    public void testUnsignedByte() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeInt(255);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        bcdi.skipBytes(3);

        assertEquals(255, (short)bcdi.readUnsignedByte());
        assertEquals("Incorrect byte count returned", 1, bcdi.getCount()-3);
    }

    @Test
    public void testUnsignedShort() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeInt(300);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));
        //an int is 4 bytes, skip the first two
        bcdi.skipBytes(2);

        assertEquals(300, (int)bcdi.readUnsignedShort());
        assertEquals("Incorrect byte count returned", 2, bcdi.getCount()-2);
    }

    @Test
    public void testReadUTF() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(stream);

        output.writeUTF(alphabet);

        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(stream.toByteArray())));

        String readAlphabet = bcdi.readUTF();
        assertEquals("Incorrect byte count read for string", 28, bcdi.getCount());


        long countBefore = bcdi.getCount();
        try {
            bcdi.readUTF();
        } catch (IOException expected) {}
        assertEquals(countBefore, bcdi.getCount());
    }

    @Test
    public void testSkipPart() throws Exception {
        ByteCountingDataInput bcdi = new ByteCountingDataInput(new DataInputStream(new ByteArrayInputStream(alphabet.getBytes())));

        byte[] bytes = new byte[2];
        bcdi.readFully(bytes);
        assertArrayEquals("incorrect bytes read", Arrays.copyOfRange(alphabet.getBytes(), 0, 2), bytes);
        bcdi.skipBytes(2);
        bcdi.readFully(bytes);
        assertArrayEquals("incorrect bytes read", Arrays.copyOfRange(alphabet.getBytes(), 4, 6), bytes);
        assertEquals("incorrect byte count read", 6, bcdi.getCount());
    }


}
