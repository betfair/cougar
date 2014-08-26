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
import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test functionality of {@link IdlToDSMojo}.
 */
public class IdlToDSMojoTest {

    private static final String RSRC = "test-wsdl.xsl";

    /**
     * There are a number of things being done by the mojo, may as well test 'em all at once.
     * <p/>
     * TODO incomplete. Current tests only wsdl generation and classpath adding.
     */
    @Test
    public void testHappyPath() throws Exception {

        final String baseDir = System.getProperty("user.dir");

        IdlToDSMojo mojo = new IdlToDSMojo();
        MavenProject project = addPrivateFieldTo(mojo, "project", new MavenProject());
        mojo.setBaseDir(baseDir);
        mojo.setWsdlXslResource(RSRC);
        mojo.setXsdXslResource(RSRC);
        mojo.setServices(new Service[]{});

        mojo.execute();

        // check that WSDL.xsl was written correctly
        Files.compare(
                new File(baseDir, "src/test/resources/" + RSRC),
                new File(baseDir, "target/wrk/wsdl.xsl"));
        Files.compare(
                new File(baseDir, "src/test/resources/" + RSRC),
                new File(baseDir, "target/wrk/xsd.xsl"));

        // check that we've added the source path
        checkSourcePaths(project);
    }

    @SuppressWarnings("unchecked")
    private void checkSourcePaths(MavenProject project) {

        List<String> paths = project.getCompileSourceRoots();

        assertEquals(1, paths.size());

        // this jiggery-pokery because I lack the resolve to fiddle with paths and separators and
        //	escapes for a simple string comparison that works on all platforms
        File path = new File(paths.get(0));
        assertEquals("java", path.getName());
        path = path.getParentFile();
        assertEquals("generated-sources", path.getName());
        path = path.getParentFile();
        assertEquals("target", path.getName());

    }

    /**
     * Util which assigns the given value to the given field on the given object, and returns
     * the value (as a convenience)
     */
    private <T> T addPrivateFieldTo(Object o, String fieldName, T value) throws Exception {

        Class<?> cls = o.getClass();
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(o, value);

        return value;
    }
}
