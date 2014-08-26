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

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.w3c.dom.Document;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit test {@link XmlGenerator}
 */
public class XmlGeneratorTest {

	/**
	 * Just tests the mechanics of the generation, not the correctness of the underlying stylesheet,
	 * which is covered by JETT and .Net tests.
	 */
    @Test
    public void testGenerateWsdl() throws Exception {

        Document iddDoc = XmlUtil.parse(
        				Files.fromResource("SomeService.xml"),
        				Files.initResolver(mock(Log.class)));

        File xslFile = Files.fromResource("test-wsdl.xsl");
        File outFile = new File(Files.baseDir, "target/wrk/SomeService.wsdl");
        outFile.getParentFile().mkdirs();

        XmlGenerator generator = new XmlGenerator();
        generator.transform(iddDoc, xslFile, outFile);

        //this test is flakey with respect to new line characters
        String expectedContent = Files.readFile(Files.fromResource("SomeService.wsdl")).replace("\r","").replace("\n","");
        String actualContent = Files.readFile(outFile).replace("\r","").replace("\n","");
        assertEquals(expectedContent, actualContent);
    }
}
