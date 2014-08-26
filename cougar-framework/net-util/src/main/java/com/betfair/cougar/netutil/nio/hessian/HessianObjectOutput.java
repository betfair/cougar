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

package com.betfair.cougar.netutil.nio.hessian;

import java.io.IOException;
import java.io.OutputStream;

import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

public class HessianObjectOutput implements CougarObjectOutput {

	private Hessian2Output hessian2Output;

	HessianObjectOutput(OutputStream wrapped, SerializerFactory factory) {
		hessian2Output = new Hessian2Output(wrapped);
        hessian2Output.setSerializerFactory(factory);
	}

	@Override
	public void close() throws IOException {
		hessian2Output.close();
	}

	@Override
	public void flush() throws IOException {
		hessian2Output.flush();
	}

	@Override
	public void writeBoolean(boolean b) throws IOException {
		hessian2Output.writeBoolean(b);

	}

	@Override
	public void writeBytes(byte[] buffer, int offset, int length) throws IOException {
		hessian2Output.writeBytes(buffer,offset,length);
	}

	@Override
	public void writeBytes(byte[] buffer) throws IOException {
		hessian2Output.writeBytes(buffer);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		hessian2Output.writeDouble(value);
	}

	@Override
	public void writeInt(int value) throws IOException {
		hessian2Output.writeInt(value);
	}

	@Override
	public void writeLong(long value) throws IOException {
		hessian2Output.writeLong(value);
	}

	@Override
	public void writeObject(Object object) throws IOException {
		hessian2Output.writeObject(object);
	}

	@Override
	public void writeString(String str) throws IOException {
		hessian2Output.writeString(str);
	}

}
