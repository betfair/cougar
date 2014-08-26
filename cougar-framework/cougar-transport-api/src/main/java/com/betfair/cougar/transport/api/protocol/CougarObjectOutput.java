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

public interface CougarObjectOutput {

	void close() throws IOException;

	void flush() throws IOException;

	void writeBytes(byte[] arg0, int arg1, int arg2) throws IOException;

	void writeBytes(byte[] buffer) throws IOException;

	void writeDouble(double value) throws IOException;

	void writeInt(int value) throws IOException;

	void writeLong(long value) throws IOException;

	void writeObject(Object object) throws IOException;

	void writeString(String arg0) throws IOException;

	void writeBoolean(boolean b) throws IOException;
}
