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

package com.betfair.cougar.util.configuration;

import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class PropertyLoaderTest{
    private Resource mockResource = Mockito.mock(Resource.class, "Resource");

    @Test
    public void ResourceConstructorTest() throws IOException{
        PropertyLoader pl = new PropertyLoader(mockResource, "overrides.properties");
        Resource[] resources = pl.constructResourceList();
        assertNotNull(resources);
        assertEquals(1, resources.length); // Assert only one resource being loaded from as no cougar-application.properties or overrides.properties exist
    }

    @Test
    public void ResourceConstructorWithFileSystemResourceTest() throws IOException{
        System.getProperties().setProperty("betfair.config.host", "");

        // Create a temporary properties file to point the property loader to
        // Identify OS running test to find temp dir location because on linux anthill build server can't rely on java.io.tmpdir
        String os = System.getProperty("os.name");
        String tempDir = "/tmp/";
        if (os.contains("Windows")) {
            tempDir = System.getProperty("java.io.tmpdir");
        }

        String filePath = tempDir+"test-overrides.properties";
        File file = new File(filePath);
        file.createNewFile();
        file.deleteOnExit();

        PropertyLoader pl = new PropertyLoader(mockResource, "file:"+filePath);
        Resource[] resources = pl.constructResourceList();
        assertNotNull(resources);
        assertEquals(2, resources.length); // Assert 2 resources being loaded from as a local properties file exists
    }

    @Test
    public void ResourceConstructorWithClassPathResourceTest() throws IOException{
        System.getProperties().setProperty("betfair.config.host", "");

        // Point the property loader to a pre existing properties file that will be on the clas path
        PropertyLoader pl = new PropertyLoader(mockResource, "classpath:test-overrides.properties");
        Resource[] resources = pl.constructResourceList();
        assertNotNull(resources);
        assertEquals(2, resources.length); // Assert 2 resources being loaded from as a properties file exists on the classpath
    }
}
