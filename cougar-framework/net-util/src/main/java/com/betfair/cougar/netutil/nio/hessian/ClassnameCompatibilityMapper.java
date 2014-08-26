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

package com.betfair.cougar.netutil.nio.hessian;

import com.betfair.cougar.core.api.ServiceVersion;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ClassnameCompatibilityMapper {
    private static ConcurrentHashMap<Class, String> serialisationMap = new ConcurrentHashMap<Class, String>();
    private static ConcurrentHashMap<String, String> deserialisationMap = new ConcurrentHashMap<String, String>();

    public static String toMajorMinorPackaging(Class myClass, ServiceVersion serviceVersion) {
        if (serialisationMap.containsKey(myClass)) {
            return serialisationMap.get(myClass);
        }

        String ret = myClass.getName().replace("v" + serviceVersion.getMajor(), "v" + serviceVersion.getMajor() + "_" + serviceVersion.getMinor());
        serialisationMap.put(myClass, ret);
        return ret;
    }

    public static String toMajorOnlyPackaging(String origClassName) {
        if (deserialisationMap.containsKey(origClassName)) {
            return deserialisationMap.get(origClassName);
        }

        Pattern p = Pattern.compile("\\.v[0-9]_[0-9]\\.");
        Matcher m = p.matcher(origClassName);
        if (m.find()) {
            String found = m.group();
            String newBit = found.substring(0,3)+".";
            String newType = m.replaceFirst(newBit);
            deserialisationMap.put(origClassName, newType);
            return newType;
        }
        // might not have been one of our classes..
        return origClassName;
    }
}
