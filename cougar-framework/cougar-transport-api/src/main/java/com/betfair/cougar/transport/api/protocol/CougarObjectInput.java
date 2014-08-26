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

package com.betfair.cougar.transport.api.protocol;

import java.io.IOException;

public interface CougarObjectInput {

	public void close() throws IOException;

	public boolean readBoolean() throws IOException;

	public int readBytes(byte[] arg0, int arg1, int arg2) throws IOException;

	public double readDouble() throws IOException;

	public int readInt() throws IOException;

	public Object readObject() throws IOException, ClassNotFoundException;

	public String readString() throws IOException;

	public long readLong() throws IOException;
}
