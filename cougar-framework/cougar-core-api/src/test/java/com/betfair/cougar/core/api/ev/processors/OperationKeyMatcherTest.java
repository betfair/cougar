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

package com.betfair.cougar.core.api.ev.processors;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationKeyMatcherTest {

    private OperationKey key;
    private OperationKeyMatcher operationKeyMatcher;

    @Before
    public void setup() {
        key = new OperationKey(new ServiceVersion(1, 2), "serviceName", "operationName");
        // set namespace..
        key = new OperationKey(key, "namespace");
        operationKeyMatcher = new OperationKeyMatcher();
    }

    @Test
    public void emptyMatcher() {
        assertInvocation("With NO match properties set th interceptor must be invoked", true);
    }

    @Test
    public void majorVersionMatching() {
        operationKeyMatcher.setMajorVersion(2);
        assertInvocation("With wrong majorVersion, key must fail to match", false);
        operationKeyMatcher.setMajorVersion(1);
        assertInvocation("With right majorVersion, key must match", true);
    }

    @Test
    public void minorVersionMatching() {
        operationKeyMatcher.setMinorVersion(3);
        assertInvocation("With wrong minorVersion, key must fail to match", false);
        operationKeyMatcher.setMinorVersion(2);
        assertInvocation("With right minorVersion, key must match", true);
    }

    @Test
    public void serviceNameMatching() {
        operationKeyMatcher.setServiceName("wrongService");
        assertInvocation("With wrong serviceName, key must fail to match", false);
        operationKeyMatcher.setServiceName("serviceName");
        assertInvocation("With right serviceName, key must match", true);

        // wild card tests
        operationKeyMatcher.setServiceName("*viceName");
        assertInvocation("With left wildcard, key must match", true);
        operationKeyMatcher.setServiceName("service*");
        assertInvocation("With right wildcard, key must match", true);
        operationKeyMatcher.setServiceName("*erviceNam*");
        assertInvocation("With both wildcards, key must match", true);

        operationKeyMatcher.setServiceName("*reviceName");
        assertInvocation("With wrong left wildcard, key must fail to match", false);
        operationKeyMatcher.setServiceName("servce*");
        assertInvocation("With wrong right wildcard, key must fail to match", false);
        operationKeyMatcher.setServiceName("*revicNma*");
        assertInvocation("With wrong double wildcard, key must fail to match", false);
    }

    @Test
    public void operationNameMatching() {
        operationKeyMatcher.setOperationName("wrongOperation");
        assertInvocation("With wrong operationName, key must fail to match", false);
        operationKeyMatcher.setOperationName("operationName");
        assertInvocation("With right operationName, key must match", true);

        // wild card tests
        operationKeyMatcher.setOperationName("*tionName");
        assertInvocation("With left wildcard, key must match", true);
        operationKeyMatcher.setOperationName("operation*");
        assertInvocation("With right wildcard, key must match", true);
        operationKeyMatcher.setOperationName("*perationNam*");
        assertInvocation("With both wildcards, key must match", true);

        operationKeyMatcher.setOperationName("*aionName");
        assertInvocation("With wrong left wildcard, key must fail to match", false);
        operationKeyMatcher.setOperationName("operatioz*");
        assertInvocation("With wrong right wildcard, key must fail to match", false);
        operationKeyMatcher.setOperationName("*qeratioz*");
        assertInvocation("With wrong double wildcard, key must fail to match", false);
    }

    @Test
    public void typeMatching() {
        operationKeyMatcher.setType("event");
        assertInvocation("With wrong type, key must fail to match", false);
        operationKeyMatcher.setType("request");
        assertInvocation("With right type, key must match", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTypeSpecified() {
        operationKeyMatcher.setType("wibble");
    }

    @Test
    public void invertedMatching() {
        // basic inverted tests
        operationKeyMatcher.setServiceName("service*");
        operationKeyMatcher.setOperationName("operation*");
        assertInvocation("Must match normally", true);
        operationKeyMatcher.setInverted(true);
        assertInvocation("Must not match now inverted", false);
    }

    @Test
    public void namespaceMatching() {
        operationKeyMatcher.setNamespace("wrongNamespace");
        assertInvocation("With wrong namespace, key must fail to match", false);
        operationKeyMatcher.setNamespace("namespace");
        assertInvocation("With right namespace, key must match", true);

        // wild card tests
        operationKeyMatcher.setNamespace("*amespace");
        assertInvocation("With left wildcard, key must match", true);
        operationKeyMatcher.setNamespace("namespac*");
        assertInvocation("With right wildcard, key must match", true);
        operationKeyMatcher.setNamespace("*amespac*");
        assertInvocation("With both wildcards, key must match", true);

        operationKeyMatcher.setNamespace("*measpace");
        assertInvocation("With wrong left wildcard, key must fail to match", false);
        operationKeyMatcher.setNamespace("namespca*");
        assertInvocation("With wrong right wildcard, key must fail to match", false);
        operationKeyMatcher.setNamespace("*measpca*");
        assertInvocation("With wrong double wildcard, key must fail to match", false);

        operationKeyMatcher.setNamespace(null);
        assertInvocation("With no namespace requirement, should match fine", true);
        operationKeyMatcher.setRequireNullNamespace(true);
        assertInvocation("With requiring null namespace, should fail to match", false);
        // set the namespace to be null
        key = new OperationKey(key, null);
        assertInvocation("With requiring null namespace, should match", true);
        operationKeyMatcher.setNamespace("namespace");
        assertInvocation("With namespace requirement, should fail", false);

    }

    private void assertInvocation(String message, boolean expected) {
        assertEquals(message, expected, operationKeyMatcher.matches(null, key, new Object[0]));
    }

    @Test
    public void toStringTest(){
        OperationKeyMatcher okm = new OperationKeyMatcher();
        //Should not fail with null values
        okm.toString();

        okm.setMajorVersion(2);
        okm.setMinorVersion(1);
        okm.setOperationName("op");
        okm.setServiceName("testService");
        okm.setNamespace("ns");

        String val = okm.toString();
        assertTrue(val.contains("2"));
        assertTrue(val.contains("1"));
        assertTrue(val.contains("op"));
        assertTrue(val.contains("testService"));
        assertTrue(val.contains("ns"));
    }

}