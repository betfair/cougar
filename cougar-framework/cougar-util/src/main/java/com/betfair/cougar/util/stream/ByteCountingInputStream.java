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

import java.io.IOException;
import java.io.InputStream;

public class ByteCountingInputStream extends InputStream {
    private long count;
    private final InputStream is;
    public ByteCountingInputStream(InputStream in) {
        is = in;
    }

    public long getCount() {
        return count;
    }

    // Delegate all methods to the underlying stream
    @Override
    public int read() throws IOException {
        int readChar = is.read();
        if (readChar >= 0) {
            incrementCount(1);
        }
        return readChar;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readChars = is.read(b, off, len);
        if (readChars >= 0) {
            incrementCount(readChars);
        }
        return readChars;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int readChars = is.read(b);
        if (readChars >= 0) {
            incrementCount(readChars);
        }
        return readChars;
    }

    @Override
    public long skip(long n) throws IOException {
        // Even if we've skipping, we still have to read.
        long skip = is.skip(n);
        incrementCount(skip);
        return skip;
    }

    protected void incrementCount(long increment) throws IOException {
        count += increment;
    }
}
