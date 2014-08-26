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
import java.io.InputStream;

import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.SerializerFactory;

public class HessianObjectInput implements CougarObjectInput {

	private Hessian2Input hessian2Input;

	HessianObjectInput(InputStream is, SerializerFactory serializerFactory) {
		hessian2Input = new Hessian2Input(is);
        hessian2Input.setSerializerFactory(serializerFactory);
	}


	@Override
	public void close() throws IOException {
		hessian2Input.close();

	}

	@Override
	public boolean readBoolean() throws IOException {
		try {
			return hessian2Input.readBoolean();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}


	@Override
	public int readBytes(byte[] arg0, int arg1, int arg2) throws IOException {
		try {
			return hessian2Input.readBytes(arg0, arg1, arg2);
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public double readDouble() throws IOException {
		try {
			return hessian2Input.readDouble();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public int readInt() throws IOException {
		try {
			return hessian2Input.readInt();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public long readLong() throws IOException {
		try {
			return hessian2Input.readLong();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public Object readObject() throws IOException {
		try {
			return hessian2Input.readObject();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public String readString() throws IOException {
		try {
			return hessian2Input.readString();
		}
		catch (HessianProtocolException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

}
