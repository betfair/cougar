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

package com.betfair.cougar.core.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CougarOutputStream extends OutputStream {
    private final ByteArrayOutputStream cache = new ByteArrayOutputStream();
    byte[] buffer ;
    private final OutputStream actualOutputStream;

    public CougarOutputStream(OutputStream out) {
        super();
        this.actualOutputStream = out;

    }

    @Override
    public void write(final int b) throws IOException {
        cache.write(b);
        actualOutputStream.write(b);
    }

    @Override
    public void write(final byte[] bytes) throws IOException {
        cache.write(bytes);
        actualOutputStream.write(bytes);
    }

    @Override
    public void write(final byte[] bytes, final int off, final int len) throws IOException {
        cache.write(bytes, off, len);
        actualOutputStream.write(bytes, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        actualOutputStream.close();
        if(buffer == null) {
            createBuffer();
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        actualOutputStream.flush();
    }

    public byte[] getContent() {
        if(buffer == null) {
            createBuffer();
        }
        return buffer;
    }

    private void createBuffer() {
        buffer = cache.toByteArray();
    }


}
