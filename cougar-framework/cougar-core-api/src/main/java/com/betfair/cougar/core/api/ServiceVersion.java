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

package com.betfair.cougar.core.api;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ServiceVersion {
    private final int major;
    private final int minor;

    public ServiceVersion(String versionString) {
        if (versionString == null || versionString.length() == 0) {
            throw new IllegalArgumentException("Version string empty or null - " + versionString);
        }
        char firstChar = versionString.charAt(0);
        if (firstChar != 'V' && firstChar != 'v') {
            throw new IllegalArgumentException("Version string does not start with 'v' - " + versionString);
        }

        String[] versions = versionString.substring(1).split("\\.");
        if (versions.length > 2) {
            throw new IllegalArgumentException("Version string has too many parts - " + versionString);
        }

        major = Integer.parseInt(versions[0]);
        minor = versions.length < 2 ? 0 : Integer.parseInt(versions[1]);
    }

    public ServiceVersion(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return "v" + major + "." + minor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceVersion) {
            ServiceVersion other = (ServiceVersion)obj;
            return new EqualsBuilder().append(major, other.major).append(minor, other.minor).isEquals();
        }
        return false;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(major).append(minor).toHashCode();
    }
}
