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

import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.security.KeyStore;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class X509CertificateUtilsTest {
    @Test
    public void javaToJavaxToJava() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(KeyStoreManagementTest.getKeystorePath()), "password");
        KeyStore ks = ksm.getKeyStore();
        long start = System.nanoTime();
        java.security.cert.X509Certificate c1 = (java.security.cert.X509Certificate) ks.getCertificate("selfsigned");
        javax.security.cert.X509Certificate c2 = X509CertificateUtils.convert(c1);
        java.security.cert.X509Certificate c3 = X509CertificateUtils.convert(c2);
        long total = System.nanoTime() - start;
        System.out.println("Total time: "+total);
        assertEquals(c1, c3);
    }
    @Test
    public void javaArrayToJavaxArrayToJavaArray() throws Exception {
        KeyStoreManagement ksm = KeyStoreManagement.getKeyStoreManagement("JKS", new FileSystemResource(KeyStoreManagementTest.getKeystorePath()), "password");
        KeyStore ks = ksm.getKeyStore();
        long start = System.nanoTime();
        java.security.cert.X509Certificate c1 = (java.security.cert.X509Certificate) ks.getCertificate("selfsigned");
        javax.security.cert.X509Certificate[] c2 = X509CertificateUtils.convert(new java.security.cert.X509Certificate[] {c1});
        java.security.cert.X509Certificate[] c3 = X509CertificateUtils.convert(c2);
        long total = System.nanoTime() - start;
        System.out.println("Total time: "+total);
        assertEquals(c1, c3[0]);
    }
}
