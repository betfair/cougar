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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.betfair.cougar.test.CougarTestCase;

public class CougarOutputStreamTest extends CougarTestCase {
	ByteArrayOutputStream underlying = new ByteArrayOutputStream();
	CougarOutputStream os = new CougarOutputStream(underlying);


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		os.close();
	}

	public void testWriteInt() throws Exception {
		os.write(1);
		os.flush();
		assertEquals(1, new ByteArrayInputStream(underlying.toByteArray()).read());
	}

	public void testWriteByteArray() throws Exception {
		byte[] bytes = new byte[98];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte)i;
		}
		os.write(bytes);
		os.flush();
		assertEqualsArray(bytes, underlying.toByteArray());
	}

	public void testWriteByteArrayIntInt() throws Exception {
		byte[] bytes = new byte[98];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte)i;
		}
		os.write(bytes, 10, 20);
		os.flush();
		assertEqualsArray(Arrays.copyOfRange(bytes, 10, 30), underlying.toByteArray());
	}

	public void testGetContent() throws Exception {
		byte[] bytes = new byte[98];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte)i;
		}
		os.write(bytes);
		os.flush();
		assertEqualsArray(bytes, underlying.toByteArray());

		byte[] content = os.getContent();
		assertEqualsArray(content, underlying.toByteArray());

		byte[] contentAgain = os.getContent();
		assertTrue(contentAgain == content);
}

}
