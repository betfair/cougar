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

package com.betfair.cougar.util;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class UUIDGeneratorImplTest {

    @BeforeClass
    public static void setup() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

	@Test
	public void testRequestIdsShouldNotBeEqual() throws Exception {
        final String uuid1 = new UUIDGeneratorImpl().getNextUUID();
        final String uuid2 = new UUIDGeneratorImpl().getNextUUID();
        assertFalse("Uuids should not be equal", uuid1.equals(uuid2));
    }

    @Test
	public void testRequestIdShouldContainFirstPartOfHostname() throws Exception {
        final String uuid = new UUIDGeneratorImpl().getNextUUID();

        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
            Pattern HOST_FINDER = Pattern.compile("([\\w\\d\\-]{1,30}+).*", Pattern.CASE_INSENSITIVE);
            Matcher m = HOST_FINDER.matcher(host);
            if (m.matches()) {
                host = m.group(1);
            } else {
                host = UUIDGeneratorImpl.DEFAULT_HOSTNAME;
            }
        } catch (UnknownHostException e) {
            host = UUIDGeneratorImpl.DEFAULT_HOSTNAME;
        }
        assertTrue("Request id should contain first part of hostname "+host, uuid.contains(host));
    }

    @Test
	public void testRequestIdShouldContainTimestamp() throws Exception {
        final String uuid = new UUIDGeneratorImpl().getNextUUID();
        final Pattern pattern = Pattern.compile("-\\d{8}-");
        assertTrue("Request id should contain timestamp",pattern.matcher(uuid).find());
    }

    @Test
	public void testRequestIdShouldContainCounter() throws Exception {
        final String uuid = new UUIDGeneratorImpl().getNextUUID();
        final Pattern pattern = Pattern.compile("-(\\d|[a-f]){10}");
        assertTrue("Request id should contain counter",pattern.matcher(uuid).find());
    }

    @Test
	public void testRequestIdShouldHaveCorrectFormat() throws Exception {
        final String uuid = new UUIDGeneratorImpl().getNextUUID();
        final Pattern pattern = Pattern.compile("\\S*-\\d{8}-(\\d|[a-f]){10}");
        assertTrue("Request id should have correct format",pattern.matcher(uuid).matches());
    }

    @Test
    public void testLengthAlwaysEqual() throws Exception {
        Field f = UUIDGeneratorImpl.class.getDeclaredField("count");
        f.setAccessible(true);
        AtomicLong c = (AtomicLong)f.get(null);
        c.set(0);
        int referenceLength = new UUIDGeneratorImpl().getNextUUID().length();

        int numUUIDsCreated = 0;
        long lastVal = -1;
        while (true) {
            UUIDGenerator uuid = new UUIDGeneratorImpl();
            ++numUUIDsCreated;
            assertTrue("Bad uuid: "+ uuid , uuid.getNextUUID().length() == referenceLength);
            c.set(c.get() * 2);
            long thisVal = Long.parseLong(uuid.getNextUUID().substring(uuid.getNextUUID().lastIndexOf("-")+1), 16);
            if (thisVal < lastVal) {
                if (c.get() < 0xFFFFFFFFFFL) {
                    Assert.fail("Cycled after "+numUUIDsCreated+" uuids. lastVal was "+lastVal+", thisVal is "+thisVal);
                } else {
                    break;
                }
            } else {
                lastVal = thisVal;
            }
        }
        System.out.println("Length tested OK after "+ numUUIDsCreated+ "iterations");
    }


    @Test
	public void testPerformance() throws Exception {
        UUIDThread[] threads = new UUIDThread[5];
        for (int i=0; i < threads.length; ++i) {
            threads[i] = new UUIDThread() ;
        }
        for (int i=0; i < threads.length; ++i) {
            threads[i].start();
        }
        for (int i=0; i < threads.length; ++i) {
            threads[i].join();
        }

        long totalTime = 0;
        for (int i=0; i < threads.length; ++i) {
            if (threads[i].failString != null) {
                Assert.fail("Thread " + i + " failed - UUID found was " + threads[i].failString);
            }
            totalTime += threads[i].timeTaken;
        }

        System.out.println("Time taken for 5,000,000 requests was "+totalTime+" ms");
    }

    @Test
    public void tripleComponent() {
        UUIDGeneratorImpl impl = new UUIDGeneratorImpl();
        String[] component = impl.validateUuid("abcd001-abcdef-00001:defg002-ghijkl-00001:hijk003-mnopqr-00001");
        assertEquals(3, component.length);
        assertEquals("abcd001-abcdef-00001",component[0]);
        assertEquals("defg002-ghijkl-00001",component[1]);
        assertEquals("hijk003-mnopqr-00001",component[2]);
    }
    @Test
    public void singleComponent() {
        UUIDGeneratorImpl impl = new UUIDGeneratorImpl();
        String[] component = impl.validateUuid("abcd001-abcdef-00001");
        assertEquals(3, component.length);
        assertNull(component[0]);
        assertNull(component[1]);
        assertEquals("abcd001-abcdef-00001",component[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tripleComponentWrongSep() {
        UUIDGeneratorImpl impl = new UUIDGeneratorImpl();
        impl.validateUuid("abcd001-abcdef-00001;defg002-ghijkl-00001;hijk003-mnopqr-00001");
    }

    @Test(expected = IllegalArgumentException.class)
    public void twoComponents() {
        UUIDGeneratorImpl impl = new UUIDGeneratorImpl();
        impl.validateUuid("abcd001-abcdef-00001:defg002-ghijkl-00001");
    }


    private static class UUIDThread extends Thread {
        private long timeTaken;
        private String failString;
        @Override
        public void run() {
            timeTaken = System.currentTimeMillis();
            for (int i = 0; i < 1000000; ++i) {
                UUIDGenerator foo = new UUIDGeneratorImpl();
                String uuid = foo.getNextUUID();
                if (uuid.length() - uuid.lastIndexOf("-", uuid.lastIndexOf("-") - 1) != 20) {
                    failString = uuid;
                }
            }
            timeTaken = System.currentTimeMillis() - timeTaken;
        }
    }
}
