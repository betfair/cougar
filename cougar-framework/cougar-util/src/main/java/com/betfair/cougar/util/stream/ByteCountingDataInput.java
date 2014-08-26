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

import java.io.DataInput;
import java.io.IOException;

/**
 * Decorator class to facilitate counting bytes read through a DataInput object
 */
public class ByteCountingDataInput implements DataInput {
    private DataInput dataInput;

    private long count=0;

    public ByteCountingDataInput(DataInput dataInput) {
        this.dataInput = dataInput;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        dataInput.readFully(b);
        count+=b.length;
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        dataInput.readFully(b, off, len);
        count+=len;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int skip = dataInput.skipBytes(n);
        count += skip;
        return skip;
    }

    @Override
    public boolean readBoolean() throws IOException {
        boolean value = dataInput.readBoolean();
        count++;
        return value;
    }

    @Override
    public byte readByte() throws IOException {
        byte value = dataInput.readByte();
        count++;
        return value;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int value = dataInput.readUnsignedByte();
        count++;
        return value;
    }

    @Override
    public short readShort() throws IOException {
        short value = dataInput.readShort();
        count+=2;
        return value;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int value = dataInput.readUnsignedShort();
        count+=2;
        return value;
    }

    @Override
    public char readChar() throws IOException {
        char c = dataInput.readChar();
        count+=2;
        return c;
    }

    @Override
    public int readInt() throws IOException {
        int value = dataInput.readInt();
        count+=4;
        return value;
    }

    @Override
    public long readLong() throws IOException {
        long value = dataInput.readLong();
        count+=8;
        return value;
    }

    @Override
    public float readFloat() throws IOException {
        float value = dataInput.readFloat();
        count+=4;
        return value;
    }

    @Override
    public double readDouble() throws IOException {
        double value = dataInput.readDouble();
        count+=8;
        return value;
    }

    /**
     * Please note that this isn't going to accurately count line separators, which are discarded by
     * the underlying DataInput object
     * This call is deprecated because you cannot accurately account for the CR/LF count
     * @return
     * @throws IOException
     */
    @Override
    @Deprecated
    public String readLine() throws IOException {
        String value = dataInput.readLine();
        if (value != null) {
            count += value.getBytes().length;
        }
        return value;
    }

    @Override
    public String readUTF() throws IOException {
        String value = dataInput.readUTF();
        count += 2; //for the string length
        count += value.getBytes().length;
        return value;
    }

    public long getCount() {
        return count;
    }
}
