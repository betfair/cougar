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

package com.betfair.cougar;

import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CougarVersion {

    private final static Logger LOGGER = LoggerFactory.getLogger(CougarVersion.class);

    private static String VERSION;
    private static String MAJOR_MINOR_VERSION;

    static {
        init("/version/CougarVersion.properties");
    }

    static void init(String properties) {
        VERSION = null;
        MAJOR_MINOR_VERSION = null;
        InputStream is = null;
        try {
            is = new CougarVersion().getClass().getResourceAsStream(properties);
            Properties prop = new Properties();
            prop.load(is);
            VERSION = prop.getProperty("version");
            LOGGER.info("Labeled Cougar version is "+VERSION);
        } catch (Exception e) {
            LOGGER.warn("Failed to read Cougar version", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception closing version stream", e);
                }
            }
        }
        if (VERSION == null) {
            VERSION = "SNAPSHOT";
        }
        Pattern p = Pattern.compile("^[0-9]+\\.[0-9]+");
        Matcher matcher = p.matcher(VERSION);
        if (matcher.find()) {
            MAJOR_MINOR_VERSION = matcher.group();
        }
        else {
            throw new CougarFrameworkException("Cannot resolve Cougar's version, something is seriously amiss");
        }
    }

    public static String getVersion(){
        return VERSION;
    }

    /**
     * Returns the version in the format &lt;major>.&lt;minor>. It will always strip snapshot strings and will also
     * strip anything after the minor version.
     * @return The version in the correct format
     */
    public static String getMajorMinorVersion() {
        return MAJOR_MINOR_VERSION;
    }
}