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

import com.betfair.cougar.api.UUIDGenerator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UUIDGeneratorImpl implements UUIDGenerator {

    private static Pattern VALID_UUID_COMPONENT = Pattern.compile("[\\w\\d\\-]{20,50}", Pattern.CASE_INSENSITIVE);

    public static final String DEFAULT_HOSTNAME = "localhost";

    private static final AtomicLong count = new AtomicLong();
    private static final String uuidPrefix;

    static {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();

            Pattern HOST_FINDER = Pattern.compile("([\\w\\d\\-]{1,30}+).*", Pattern.CASE_INSENSITIVE);
            Matcher m = HOST_FINDER.matcher(host);
            if (m.matches()) {
                host = m.group(1);
            } else {
                host = DEFAULT_HOSTNAME;
            }
        } catch (UnknownHostException e) {
            host = DEFAULT_HOSTNAME;
        }
        uuidPrefix = host +  String.format("-%1$tm%1$td%1$tH%1$tM-",new Date());
    }

    @Override
    public String getNextUUID() {
        return randomUUID();
    }

    @Override
    public String[] validateUuid(String uuid) {
        String[] components = uuid.split(Pattern.quote(UUIDGenerator.COMPONENT_SEPARATOR));
        if (components.length != 1 && components.length != 3) {
            throw new IllegalArgumentException("UUid "+uuid+" has "+components.length+" component parts, expected 1 or 3");
        }
        for (int i=0; i<components.length; i++) {
            if (!VALID_UUID_COMPONENT.matcher(components[i]).matches()) {
                throw new IllegalArgumentException("UUid component "+i+"("+components[i]+") invalid - must match pattern "+ VALID_UUID_COMPONENT.pattern());
            }
        }
        if (components.length == 1) {
            return new String[] { null, null, uuid };
        }
        return components;
    }

    private static String randomUUID() {
        long val = count.getAndIncrement()%0xFFFFFFFFFFL; // loop at 0xFFFFFFFFFFL
        return uuidPrefix + getPrefixZeros(val) + Long.toHexString(val);
    }


    private static String getPrefixZeros(long val) {
        if (val <= 0xFL) return "000000000";
        if (val <= 0xFfL) return "00000000";
        if (val <= 0xFFFL) return "0000000";
        if (val <= 0xFFFFL) return "000000";
        if (val <= 0xFFFFFL) return "00000";
        if (val <= 0xFFFFFFL) return "0000";
        if (val <= 0xFFFFFFFL) return "000";
        if (val <= 0xFFFFFFFFL) return "00";
        if (val <= 0xFFFFFFFFFL) return "0";
        return "";
    }
}
