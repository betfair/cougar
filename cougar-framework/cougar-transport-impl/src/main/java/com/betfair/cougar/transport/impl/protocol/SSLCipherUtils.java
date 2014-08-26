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

package com.betfair.cougar.transport.impl.protocol;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

/**
 *
 */
public class SSLCipherUtils {

    /**
     * Given the name of a TLS/SSL cipher suite, return an int representing it effective stream
     * cipher key strength. i.e. How much entropy material is in the key material being fed into the
     * encryption routines.
     * <p/>
     * <p/>
     * This is based on the information on effective key lengths in RFC 2246 - The TLS Protocol
     * Version 1.0, Appendix C. CipherSuite definitions:
     * <p/>
     * <pre>
     *                         Effective
     *     Cipher       Type    Key Bits
     *
     *     NULL       * Stream     0
     *     IDEA_CBC     Block    128
     *     RC2_CBC_40 * Block     40
     *     RC4_40     * Stream    40
     *     RC4_128      Stream   128
     *     DES40_CBC  * Block     40
     *     DES_CBC      Block     56
     *     3DES_EDE_CBC Block    168
     * </pre>
     * Copied from org.eclipse.jetty.server.ssl.ServletSSL in Jetty 7.1.6.
     *
     * @param cipherSuite String name of the TLS cipher suite.
     * @param unknownKeyLength The value to attribute when the cipher is not null but not recognised
     * @return int indicating the effective key entropy bit-length.
     */
    public static int deduceKeyLength(String cipherSuite, int unknownKeyLength) {
        // Roughly ordered from most common to least common.
        if (cipherSuite == null)
            return 0;
        else if (cipherSuite.contains("WITH_AES_256_"))
            return 256;
        else if (cipherSuite.contains("WITH_RC4_128_"))
            return 128;
        else if (cipherSuite.contains("WITH_AES_128_"))
            return 128;
        else if (cipherSuite.contains("WITH_RC4_40_"))
            return 40;
        else if (cipherSuite.contains("WITH_3DES_EDE_CBC_"))
            return 168;
        else if (cipherSuite.contains("WITH_IDEA_CBC_"))
            return 128;
        else if (cipherSuite.contains("WITH_RC2_CBC_40_"))
            return 40;
        else if (cipherSuite.contains("WITH_DES40_CBC_"))
            return 40;
        else if (cipherSuite.contains("WITH_DES_CBC_"))
            return 56;
        else
            return unknownKeyLength;
    }

    public static int deduceKeyLength(SSLEngine sslEngine, int unknownKeyLength) {
        SSLSession session = sslEngine.getSession();
        return deduceKeyLength(session.getCipherSuite(), unknownKeyLength);
    }
}
