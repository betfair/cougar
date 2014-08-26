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

package com.betfair.cougar.util.jmx;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.betfair.cougar.util.jmx.ApplicationChecksums.APPLICATION_CHECKSUM;
import static com.betfair.cougar.util.jmx.ApplicationChecksums.SEP;
import static junit.framework.Assert.*;

/**
 *
 */
public class ApplicationChecksumsTest {

    @Test
    public void ensureNoJdkJarsIncluded() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        ApplicationChecksums ac = new ApplicationChecksums();
        String javaHome = System.getProperty("java.home");
        List<File> jars = ac.findNonJdkJars();
        for (File f : jars) {
            assertFalse(f.getCanonicalPath().startsWith(javaHome));
        }
    }

    @Test
    public void ensureMd5Works() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("MD5");
        ac.afterPropertiesSet();
        assertNotNull(ac.getFileCheckSumsByAlgorithm().get("MD5"));
        assertNotNull(ac.getAppCheckSumsByAlgorithm().get("MD5"));
    }

    @Test
    public void ensureSha1Works() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("SHA1");
        ac.afterPropertiesSet();
        assertNotNull(ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertNotNull(ac.getAppCheckSumsByAlgorithm().get("SHA1"));
    }

    @Test
    public void ensureSubsequentCallsSameResult() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("SHA1");
        ac.afterPropertiesSet();
        Map initialValueFileChecksums = ac.getFileCheckSumsByAlgorithm().get("SHA1");
        String initialValueAppChecksum = ac.getAppCheckSumsByAlgorithm().get("SHA1");
        ac.afterPropertiesSet();
        assertEquals(initialValueFileChecksums, ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertEquals(initialValueAppChecksum, ac.getAppCheckSumsByAlgorithm().get("SHA1"));

        ac.afterPropertiesSet();
        assertEquals(initialValueFileChecksums, ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertEquals(initialValueAppChecksum, ac.getAppCheckSumsByAlgorithm().get("SHA1"));

        ac.afterPropertiesSet();
        assertEquals(initialValueFileChecksums, ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertEquals(initialValueAppChecksum, ac.getAppCheckSumsByAlgorithm().get("SHA1"));

        ac.afterPropertiesSet();
        assertEquals(initialValueFileChecksums, ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertEquals(initialValueAppChecksum, ac.getAppCheckSumsByAlgorithm().get("SHA1"));

        ac.afterPropertiesSet();
        assertEquals(initialValueFileChecksums, ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertEquals(initialValueAppChecksum, ac.getAppCheckSumsByAlgorithm().get("SHA1"));
    }

    @Test
    public void ensureCanHave2DifferentAlgorithms() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("SHA1,MD5");
        ac.afterPropertiesSet();
        assertNotNull(ac.getFileCheckSumsByAlgorithm().get("SHA1"));
        assertNotNull(ac.getFileCheckSumsByAlgorithm().get("MD5"));
        assertFalse(ac.getFileCheckSumsByAlgorithm().get("SHA1").equals(ac.getFileCheckSumsByAlgorithm().get("MD5")));
    }

    @Test
    public void ensureSomeFilesIncluded() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("MD5");
        ac.afterPropertiesSet();
        String files = (String) ac.getAttribute(ApplicationChecksums.FILES_INCLUDED_ATTRIBUTE);
        assertNotNull(files);
        assertTrue(files.length() > 0);
    }

    @Test
    public void ensureAttributeValuesAreAvailableOnJMX() throws Exception {
        ApplicationChecksums ac = new ApplicationChecksums();
        ac.setAlgorithms("MD5,SHA1");
        ac.afterPropertiesSet();
        assertNotNull(ac.getAttribute(APPLICATION_CHECKSUM + SEP + "MD5"));
        assertNotNull(ac.getAttribute(APPLICATION_CHECKSUM + SEP + "SHA1"));
    }
}
