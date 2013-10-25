/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.util;

import com.betfair.cougar.api.RequestUUID;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RequestUUIDImplTest {

	@Test
	public void testStringValue() throws Exception {
		RequestUUID uuid = new RequestUUIDImpl();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(baos);

		uuid.writeExternal(out);
		
		// must close the writer or it doesn't actually do the write to the underlying
		// buffer
		out.close();
		
		ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		RequestUUID clone = new RequestUUIDImpl(in);
		
		assertEquals("RequestUUID implementation's toString() output do not match", uuid.toString(), clone.toString());
	}

    @Test
    public void testUUIDGenerationTooShort() {
        testGeneration("foofoofoofoofoofoof", true);
    }

    @Test
    public void testUUIDOKShort() {
        testGeneration("foofoofoofoofoofoofo", false);
    }

    @Test
    public void testUUIDOKLong() {
        testGeneration("foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofo", false);
    }

    @Test
    public void testUUIDGenerationTooLong() {
        testGeneration("foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo", true);
    }

    @Test
    public void testUUIDGenerationInvalidChars() {
        testGeneration("foofoofoofoofoofoofo.", true);
        testGeneration("foofoofoofoofoofoofo,", true);
        testGeneration("foofoofoofoofoofoofo*", true);
        testGeneration("foofoofoofoofoofoofo&", true);
        testGeneration("foofoofoofoofoofoofo$", true);
        testGeneration("foofoofoofoofoofoofo^", true);
        testGeneration("foofoofoofoofoofoofo#", true);
        testGeneration("foofoofoofoofoofoofo@", true);
        testGeneration("foofoofoofoofoofoofo ", true);
    }

    @Test
    public void testUUIDGenerationOK() {
        testGeneration("foof-oofoof-oofo-foofo", false);
        testGeneration("foo-123-1234567891011", false);
        testGeneration("123456789-77777777-foo", false);
    }

    private void testGeneration(String uuid, boolean expectFail) {
        try {
            new RequestUUIDImpl(uuid);
            if (expectFail) fail("Failed to fail for uuid "+uuid);
        } catch (IllegalArgumentException e) {
            if (!expectFail) fail("Unexpected Fail for uuid "+uuid);
        }
    }
}
