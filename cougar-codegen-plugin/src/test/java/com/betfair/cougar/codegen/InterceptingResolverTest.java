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

package com.betfair.cougar.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.betfair.cougar.codegen.except.PluginException;
import com.betfair.cougar.codegen.resolver.InterceptingResolver;

/**
 * Unit test {@link InterceptingResolver}.
 * <p>
 * NOTE this class tests only some corner case(s); the resolver itself is also tested indirectly via
 * other classes which rely on it.
 */
public class InterceptingResolverTest {

    /**
     * This makes it easier to trouble-shoot parsing problems
     */
    @Test
    public void testNonexistentCatalogFailsExplicitly() {

    	File badCatalog = new File("nonexistentcatalog.file");
    	assertFalse("This file shouldn't exist", badCatalog.exists());

    	try {
        	new InterceptingResolver(null, null, new String[] { badCatalog.getAbsolutePath() });
        	fail("Should have thrown an exception");
    	}
    	catch (PluginException e) {
    		assertEquals("Given nonexistent catalog: " + badCatalog.getAbsolutePath(), e.getMessage());
    	}
    }
}
