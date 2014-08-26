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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.ServiceVersion;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class OperationKey {

    public static enum Type {
        Request, Event, ConnectedObject
    }

    private final ServiceVersion version;
    private final String serviceName;
    private final String operationName;
    private final Type type;
    private final String namespace;

    private final OperationKey localKey;


    public OperationKey(OperationKey key, String namespace) {
        this (key.version, key.serviceName, key.operationName, key.type, namespace, key);

    }
    public OperationKey(final ServiceVersion version, final String serviceName, final String operationName) {
        this(version, serviceName, operationName, Type.Request, null, null);
    }

    public OperationKey(final ServiceVersion version, final String serviceName, final String operationName, final Type type) {
        this (version, serviceName, operationName, type, null, null);
    }

    private OperationKey(final ServiceVersion version, final String serviceName, final String operationName, final Type type, String namespace, OperationKey key) {
        this.version = version;
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.type = type;
        this.namespace = namespace;

        this.localKey = key;
    }

    public OperationKey getLocalKey() {
        return localKey == null ? this : localKey;
    }

    @ManagedAttribute
    public String getOperationName() {
        return operationName;
    }

    @ManagedAttribute
    public String getServiceName() {
        return serviceName;
    }

    @ManagedAttribute
    public ServiceVersion getVersion() {
        return version;
    }

    public String getNamespace() {
        return namespace;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(namespace)
            .append(operationName)
            .append(serviceName)
            .append(version)
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this){
            return true;
        }
        if (obj instanceof OperationKey) {
            OperationKey other = (OperationKey)obj;
            return new EqualsBuilder()
            .append(namespace, other.namespace)
            .append(operationName, other.operationName)
            .append(serviceName,other.serviceName)
            .append(version, other.version)
            .isEquals();
        }
        return false;
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean includeVersion) {
        return (namespace == null ? "" : (namespace+":")) + serviceName + (includeVersion ? "/" + version  : "") + "/" + operationName;
    }
}
