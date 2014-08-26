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

package com.betfair.cougar.core.impl.logging;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CollectionFileWriterTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data =  new Object[][] {
            // Case: new file
            {filename(), false},
            // Case: overwrite existing file
            {filename(), true},
            // Case: no file name
            {null, false},
        };
        return Arrays.asList(data);
    }

    private static String filename() {
        return new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID() + ".log").getAbsolutePath();
    }

    private Boolean enabled;
    private String fileName;
    private boolean overwrite;

    private File outputFile;
    private Collection<String> content;

    public CollectionFileWriterTest(
        String fileName,
        boolean overwrite
    ) {
        this.fileName = fileName;
        this.overwrite = overwrite;
        content = new ArrayList<String>() {{
            add(UUID.randomUUID().toString());
            add(UUID.randomUUID().toString());
        }};
    }

    @Test
    public void testCase() throws Exception {
        configure();
        Exception thrown = null;
        try {
            CollectionFileWriter.write(fileName, content);
        }
        catch (Exception e) {
            thrown = e;
        }
        verify(thrown);
    }

    private void configure() throws Exception {
        if (fileName != null) {
            outputFile = new File(fileName);
            if (overwrite) {
                touch(outputFile);
            }
        }
    }

    private void touch(File file) throws Exception {
        // UnixFileSystem doesn't seem to like File.createNewFile on AHP
        Writer writer = new FileWriter(file);
        writer.write("foo");
        writer.close();
    }

    private void verify(Exception e) throws Exception {
        // Exception thrown
        if (e != null) {
            try {
                assertNull("exception should only be thrown if there is no file name", fileName);
                assertEquals("thrown exception type is wrong", IllegalArgumentException.class, e.getClass());
                return;
            }
            catch (AssertionError f) {
                // Throw the original Exception because it's more informative on AHP where you can't see the test logs
                throw e;
            }
        }
        assertTrue("log should exist", outputFile.exists());
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        for (Object expectedLine : content) {
            assertEquals("correct content should be written to " + fileName, expectedLine, reader.readLine());
        }
        assertNull("more than expected content was found in " + fileName, reader.readLine());

    }

    @After
    public void after() {
        if (outputFile != null) outputFile.deleteOnExit();
    }
}
