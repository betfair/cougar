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

package com.betfair.cougar.core.api.ev.processors;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 * A class to 'match' OperationKeys
 * <p/>
 * By default this Matcher will return true for any OperationKey. by setting the fields
 * on the OperationKeyMatcher one may restrict the set of operationKeys for which this is the case:
 * <p/>
 * OperationKeyMatcher matcher = new OperationKeyMatcher();
 * matcher.setServiceName("myService");
 * matcher.setMajorVersion(2);
 * boolean matched = matcher.matches(givenOperationKey);
 * <p/>
 * The above  matcher will only match operation keys for version 2.x of myService.
 * Wildcards are also possible:
 * <p/>
 * matcher.setServiceName("my*"); //would match any service starting with 'my'
 * <p/>
 * The wildcard rule is simplified for reasons of speed efficiency.
 * The string to be matched may start or end with a '*' to match only the start or end of the target string.
 * If the string to be matched starts AND ends with a '*' the content must appear somewhere within the target.
 * NO OTHER WILDCARD OPERATIONS ARE SUPPORTED.
 * <p/>
 * Optionally, for namespace restrictions, it's possible to mandate that the operation key has a null namespace.
 * <p/>
 * Finally, an OperationKeyMatcher may have it's logic inverted, such that for example:
 * <p/>
 * matcher.setOperationName("login");
 * matcher.setInverted(true);
 * <p/>
 * Will only match operations which are NOT of type 'login', this could be useful to secure an entire service
 * while allowing the login method to be called.
 */

public class OperationKeyMatcher implements Matcher {

    private boolean inverted = false;
    private int majorVersion = -1;
    private int minorVersion = -1;
    private String serviceName;
    private String operationName;
    private OperationKey.Type type;
    private String namespace;
    private boolean requireNullNamespace;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }


    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isRequireNullNamespace() {
        return requireNullNamespace;
    }

    public void setRequireNullNamespace(boolean requireNullNamespace) {
        this.requireNullNamespace = requireNullNamespace;
    }

    public OperationKey.Type getType() {
        return type;
    }

    public void setType(String type) {
        if (type.equalsIgnoreCase("request")) {
            setType(OperationKey.Type.Request);
        }
        else if (type.equalsIgnoreCase("event")) {
            setType(OperationKey.Type.Event);
        }
        else {
            throw new IllegalArgumentException("Unsupported type: "+type);
        }
    }

    public void setType(OperationKey.Type type) {
        this.type = type;
    }

    @Override
    public boolean matches(ExecutionContext ctx, OperationKey key, Object[] args) {
        return matches(key);
    }

    public boolean matches(OperationKey key) {
        boolean result =
                matchType(key.getType()) &&
                        matchStringsWithWildcard(getOperationName(), key.getOperationName()) &&
                        matchStringsWithWildcard(getServiceName(), key.getServiceName()) &&
                        matchStringsWithWildcard(getNamespace(), key.getNamespace()) &&
                        matchStrictNullNamespace(isRequireNullNamespace(), key.getNamespace()) &&
                        matchVersion(key);
        if (inverted) {
            result = !result;
        }
        return result;
    }

    private boolean matchStrictNullNamespace(boolean requireNullNamespace, String namespace) {
        if (!requireNullNamespace) {
            return true;
        }
        return (namespace == null);
    }

    private boolean matchStringsWithWildcard(String expression, String data) {
        if (expression == null) {
            return true;
        }
        if (expression.startsWith("*")) {
            if (expression.endsWith("*")) {
                return data.contains(expression.substring(1, expression.length() - 1));
            }
            return data.endsWith(expression.substring(1));
        }
        if (expression.endsWith("*")) {
            return data.startsWith(expression.substring(0, expression.length() - 1));
        }
        return expression.equals(data);
    }


    private boolean matchType(OperationKey.Type type) {
        return getType() == null || getType().equals(type);
    }

    private boolean matchVersion(OperationKey key) {
        boolean match = true;
        if (getMajorVersion() > -1) {
            match = key.getVersion().getMajor() == getMajorVersion();
        }
        if (!match) {
            return false;
        }
        if (getMinorVersion() > -1) {
            match = key.getVersion().getMinor() == getMinorVersion();
        }
        return match;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
    	buffer.append("TYPE=").append(type).append("|")
    			.append("SERVICE=").append(getServiceName()).append("|")
    			.append("MAJORVERSION=").append(getMajorVersion()).append("|")
    			.append("MINORVERSION=").append(getMinorVersion()).append("|")
                .append("NAMESPACE=").append(getNamespace()).append("|")
    			.append("OPERATION=").append(getOperationName()).append("|");
        return buffer.toString();
    }


}
