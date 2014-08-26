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

import com.betfair.cougar.util.configuration.PropertyConfigurer;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 *
 */
public class KeyStoreManagementTest {

    private static String getPath(String filename) throws IOException {
        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        if (userDir.endsWith("/cougar-framework/cougar-util")) {
            userDir = userDir.substring(0, userDir.indexOf("/cougar-framework/cougar-util"));
        }
        else if (userDir.endsWith("\\cougar-framework\\cougar-util")) {
            userDir = userDir.substring(0, userDir.indexOf("\\cougar-framework\\cougar-util"));
        }

        return new File(userDir, "cougar-framework/cougar-util/src/test/resources/"+filename).getCanonicalPath();
    }

    public static String getKeystorePath() throws IOException {
        return getPath("cougar_server_cert.jks");
    }

    public static String getTruststorePath() throws IOException {
        return getPath("cougar_server_ca.jks");
    }

    @Test(expected = IllegalArgumentException.class)
    public void typeNull() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement(null, null, PropertyConfigurer.NO_DEFAULT);
        assertNull(ksm);
    }

    @Test
    public void resourceNull() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", null, PropertyConfigurer.NO_DEFAULT);
        assertNull(ksm);
    }

    @Test
    public void resourceNotSpecified() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(PropertyConfigurer.NO_DEFAULT), PropertyConfigurer.NO_DEFAULT);
        assertNull(ksm);
    }

    @Test
    public void keystore() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(getKeystorePath()), "password");
        assertNotNull(ksm);
    }

    @Test(expected = IOException.class)
    public void keystoreInvalidType() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("PKCS12", new FileSystemResource(getKeystorePath()), "password");
        assertNotNull(ksm);
    }

    @Test
    public void truststore() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(getTruststorePath()), "password");
        assertNotNull(ksm);

    }

    @Test(expected = IOException.class)
    public void truststoreInvalidPassword() throws Exception {
        KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(getTruststorePath()), "wibble");
    }

}
