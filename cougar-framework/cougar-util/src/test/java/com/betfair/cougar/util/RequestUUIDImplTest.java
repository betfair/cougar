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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class RequestUUIDImplTest {

    @BeforeClass
    public static void installGenerator() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
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

    @Test
    public void tripleComponent() {
        testGeneration("abcd001-abcdef-00001:defg002-ghijkl-00001:hijk003-mnopqr-00001",false);
    }

    @Test
    public void tripleComponentWrongSep() {
        testGeneration("abcd001-abcdef-00001;defg002-ghijkl-00001;hijk003-mnopqr-00001",true);
    }

    @Test
    public void twoComponents() {
        testGeneration("abcd001-abcdef-00001:defg002-ghijkl-00001",true);
    }

    @Test
    public void subsequentSubUuids() {
        RequestUUID parentUuid = new RequestUUIDImpl("abcd001-abcdefghijkl");
        RequestUUID firstUuid = parentUuid.getNewSubUUID();
        RequestUUID secondUuid = parentUuid.getNewSubUUID();
        RequestUUID thirdUuid = parentUuid.getNewSubUUID();
        assertNotEquals(firstUuid, secondUuid);
        assertNotEquals(secondUuid, thirdUuid);
        assertNotEquals(thirdUuid, firstUuid);
        assertEquals(parentUuid.getLocalUUIDComponent(), firstUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), firstUuid.getParentUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), secondUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), secondUuid.getParentUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), thirdUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), thirdUuid.getParentUUIDComponent());
    }

    @Test
    public void subsequentSubUuidsWithComponents() {
        RequestUUID parentUuid = new RequestUUIDImpl("abcd001-abcdef-00001:defg002-ghijkl-00001:hijk003-mnopqr-00001");
        RequestUUID firstUuid = parentUuid.getNewSubUUID();
        RequestUUID secondUuid = parentUuid.getNewSubUUID();
        RequestUUID thirdUuid = parentUuid.getNewSubUUID();
        assertEquals(parentUuid.getRootUUIDComponent(), firstUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getRootUUIDComponent(),secondUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getRootUUIDComponent(), thirdUuid.getRootUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), firstUuid.getParentUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), secondUuid.getParentUUIDComponent());
        assertEquals(parentUuid.getLocalUUIDComponent(), thirdUuid.getParentUUIDComponent());
        assertNotEquals(firstUuid, secondUuid);
        assertNotEquals(secondUuid, thirdUuid);
        assertNotEquals(thirdUuid, firstUuid);
    }

    private void testGeneration(String uuid, boolean expectFail) {
        try {
            new RequestUUIDImpl(uuid);
            if (expectFail) fail("Failed to fail for uuid "+uuid);
        } catch (IllegalArgumentException e) {
            if (!expectFail) fail("Unexpected Fail for uuid "+uuid+": "+e.getMessage());
        }
    }
}
