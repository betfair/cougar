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

import java.io.ByteArrayInputStream;

public class X509CertificateUtils {

    public static java.security.cert.X509Certificate[] convert(javax.security.cert.X509Certificate[] cert) {
        java.security.cert.X509Certificate[] ret = new java.security.cert.X509Certificate[cert.length];
        for (int i=0; i<cert.length; i++) {
            ret[i] = convert(cert[i]);
        }
        return ret;
    }

    public static javax.security.cert.X509Certificate[] convert(java.security.cert.X509Certificate[] cert) {
        javax.security.cert.X509Certificate[] ret = new javax.security.cert.X509Certificate[cert.length];
        for (int i=0; i<cert.length; i++) {
            ret[i] = convert(cert[i]);
        }
        return ret;
    }

    // Converts to java.security
    public static java.security.cert.X509Certificate convert(javax.security.cert.X509Certificate cert) {
        try {
            byte[] encoded = cert.getEncoded();
            ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
            java.security.cert.CertificateFactory cf
                = java.security.cert.CertificateFactory.getInstance("X.509");
            return (java.security.cert.X509Certificate)cf.generateCertificate(bis);
        } catch (java.security.cert.CertificateEncodingException e) {
        } catch (javax.security.cert.CertificateEncodingException e) {
        } catch (java.security.cert.CertificateException e) {
        }
        return null;
    }

    // Converts to javax.security
    public static javax.security.cert.X509Certificate convert(java.security.cert.X509Certificate cert) {
        try {
            byte[] encoded = cert.getEncoded();
            return javax.security.cert.X509Certificate.getInstance(encoded);
        } catch (java.security.cert.CertificateEncodingException e) {
        } catch (javax.security.cert.CertificateEncodingException e) {
        } catch (javax.security.cert.CertificateException e) {
        }
        return null;
    }
}
