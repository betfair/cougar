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
import java.io.OutputStream;

public class ByteCountingOutputStream extends OutputStream {

    private long count;
    private final OutputStream os;

    public ByteCountingOutputStream(OutputStream out) {
        os = out;
    }

    // Delegate all methods to the underlying stream
    public long getCount() {
        return count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        count += len;
        os.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        count += b.length;
        os.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        ++count;
        os.write(b);
    }
}