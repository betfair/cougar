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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.betfair.cougar.codegen.resolver.InterceptingResolver;

/**
 * Unit test {@link XmlValidator}
 */
public class XmlValidatorTest {

	private InterceptingResolver resolver;

	@Before
	public void setUp() {
    	resolver = Files.initResolver(mock(Log.class));
	}

    @Test
    public void testValidFile() {

        File xmlFile = Files.fromResource("SomeService.xml");
        Document xml = XmlUtil.parse(xmlFile, resolver);

        new XmlValidator(resolver).validate(xml);
    }

    @Test
    public void testValidFileNewVersion() {

        File xmlFile = Files.fromResource("SomeService3.3.xml");
        Document xml = XmlUtil.parse(xmlFile, resolver);

        new XmlValidator(resolver).validate(xml);
    }

    @Test
    public void testInvalidFile() {

        File xmlFile = Files.fromResource("BrokenService.xml");
        Document xml = XmlUtil.parse(xmlFile, resolver);
        try {
            new XmlValidator(resolver).validate(xml);
            fail("Should have thrown an exception");
        }
        catch (Exception e) {
            assertTrue("Should have validation error about invalid element, instead got: " + e,
                            e.getMessage().contains("AnInvalidElement"));
        }
    }
}
