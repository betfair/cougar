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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class LibraryVersionsTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { TestStyle.INHERIT_FROM_PARENT } , { TestStyle.LOCALLY_DEFINED } , { TestStyle.INVALID_POM } , { TestStyle.INVALID_JAR } };
		return Arrays.asList(data);
	}

    private enum TestStyle { INHERIT_FROM_PARENT, LOCALLY_DEFINED, INVALID_POM, INVALID_JAR }

    private String pomDataFromParent = "<?xml version='1.0'?><project><parent><groupId>parentGroup</groupId><artifactId>parentArtifact</artifactId><version>1.0</version></parent></project>";
    private String pomData = "<?xml version='1.0'?><project><parent><groupId>parentGroup</groupId><artifactId>parentArtifact</artifactId><version>1.0</version></parent>"+
                                       "<groupId>group</groupId><artifactId>artifact</artifactId><version>2.0</version></project>";
    private String invalidPom = "<?xml version='1.0'?><project></project>";

    private static AtomicInteger dirName = new AtomicInteger();
    private File dir;
    private TestStyle testStyle;

    public LibraryVersionsTest(TestStyle testStyle) {
        this.testStyle = testStyle;
    }

    @BeforeClass
    public static void logQuash() {
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.addAppender(new org.apache.log4j.varia.NullAppender());
    }


    @Before
    public void init() throws IOException {
        // create a directory to play in
        dir = new File(System.getProperty("java.io.tmpdir"), "libraryVersions_"+dirName.incrementAndGet()+".dir");
        if (!dir.mkdirs()) {
            boolean ok = true;
            if (dir.exists() && dir.canWrite()) {
                for (File f : dir.listFiles()) {
                    ok = ok && f.delete();
                }
            }
            if (!ok) {
                throw new IllegalStateException("Couldn't create working dir: "+dir);
            }
        }
        dir.deleteOnExit();
    }

    private void assertAttribute(LibraryVersions lv) throws ReflectionException, AttributeNotFoundException, MBeanException {
        switch (testStyle) {
            case INHERIT_FROM_PARENT:
                assertEquals("1.0", lv.getAttribute("parentGroup:parentArtifact"));
                break;
            case LOCALLY_DEFINED:
                assertEquals("2.0", lv.getAttribute("group:artifact"));
                break;
            case INVALID_JAR:
            case INVALID_POM:
                assertNull(lv.getAttribute("parentGroup:parentArtifact"));
                assertNull(lv.getAttribute("group:artifact"));
                break;
            default:
                throw new IllegalStateException("Unsupported test style: "+testStyle);
        }
    }

    private URL createJarWithPom(String s) throws IOException {
        File f = new File(dir, s);
        if (!f.createNewFile()) {
            if (!f.delete()) {
                throw new IllegalStateException("Couldn't create jar file: "+f);
            }
        }
        f.deleteOnExit();

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        if (testStyle != TestStyle.INVALID_JAR) {
            JarEntry je = new JarEntry("META-INF/maven/pom.xml");
            jos.putNextEntry(je);
            switch (testStyle) {
                case INHERIT_FROM_PARENT:
                    jos.write(pomDataFromParent.getBytes());
                    break;
                case LOCALLY_DEFINED:
                    jos.write(pomData.getBytes());
                    break;
                case INVALID_POM:
                    jos.write(invalidPom.getBytes());
                    break;
                default:
                    throw new IllegalStateException("Unsupported test style: "+testStyle);
            }
            jos.closeEntry();
        }
        else {
            JarEntry je = new JarEntry("blank.txt");
            jos.putNextEntry(je);
            jos.closeEntry();
        }
        jos.close();

        return new URL("jar:"+f.toURI().toString()+"!/META-INF/maven");
    }

    @Test
    public void normalPath() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException, ReflectionException, AttributeNotFoundException, MBeanException {
        URL u = createJarWithPom("somejar.jar");
        LibraryVersions lv = new LibraryVersions();
        Enumeration<URL> enumeration = new ArrayEnumerator<URL>(new URL[] {u});
        lv.findMavenArtifacts(enumeration);
        assertAttribute(lv);
    }

    @Test
    public void pathWithSpaces() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException, ReflectionException, AttributeNotFoundException, MBeanException {
        URL u = createJarWithPom("some jar.jar");
        LibraryVersions lv = new LibraryVersions();
        Enumeration<URL> enumeration = new ArrayEnumerator<URL>(new URL[] {u});
        lv.findMavenArtifacts(enumeration);
        assertAttribute(lv);
    }

    private class ArrayEnumerator<T> implements Enumeration<T> {

        private T[] array;
        private int index = 0;

        private ArrayEnumerator(T[] array) {
            this.array = array;
        }

        @Override
        public boolean hasMoreElements() {
            return index < array.length;
        }

        @Override
        public T nextElement() {
            if (hasMoreElements()) {
                return array[index++];
            }
            throw new NoSuchElementException();
        }
    }
}
