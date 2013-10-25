/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.testing.utils.cougar.manager;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

// Class for a naive trust manager that accepts all certificates for SSL requests

public class NaiveTrustManager implements X509TrustManager {

    public void checkClientTrusted(
            X509Certificate[] chain,
            String authType) throws CertificateException {

    }

    public void checkServerTrusted(
            X509Certificate[] chain,
            String authType) throws CertificateException {
    }


    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }

}
