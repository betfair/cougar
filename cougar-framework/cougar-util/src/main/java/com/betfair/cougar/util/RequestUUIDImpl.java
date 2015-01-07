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

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class RequestUUIDImpl implements RequestUUID {

    private String rootUUid;
    private String parentUUid;
    private String localUUid;

    private static UUIDGenerator generator;

    /**
     * Note, this sets the system wide generator, not just the local one for this instance, nasty due to spring
     */
    public RequestUUIDImpl(UUIDGenerator generator) {
        setGenerator(generator);
    }

    RequestUUIDImpl(String rootUUid, String parentUUid, String localUUid) {
        this.rootUUid = rootUUid;
        this.parentUUid = parentUUid;
        this.localUUid = localUUid;
    }

    public RequestUUIDImpl() {
        this.localUUid = generator.getNextUUID();
    }

    public RequestUUIDImpl(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("The UUID argument cannot be null");
        }
        setUuidRaw(uuid);
    }

    private void setUuidRaw(String rawUuid) {
        String[] components = generator.validateUuid(rawUuid);
        rootUUid = components[0];
        parentUUid = components[1];
        localUUid = components[2];
    }

    public static void setGenerator(UUIDGenerator generator) {
        RequestUUIDImpl.generator = generator;
    }

    @Override
    public String toCougarLogString() {
        return internalGetUuidString();
    }

    @Override
    public String toString() {
        return getUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestUUIDImpl that = (RequestUUIDImpl) o;

        if (!localUUid.equals(that.localUUid)) return false;
        if (parentUUid != null ? !parentUUid.equals(that.parentUUid) : that.parentUUid != null) return false;
        if (rootUUid != null ? !rootUUid.equals(that.rootUUid) : that.rootUUid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rootUUid != null ? rootUUid.hashCode() : 0;
        result = 31 * result + (parentUUid != null ? parentUUid.hashCode() : 0);
        result = 31 * result + localUUid.hashCode();
        return result;
    }

    public String getUUID() {
        return internalGetUuidString();
    }

    private String internalGetUuidString()
    {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        if (rootUUid != null) {
            sb.append(rootUUid);
            sep = UUIDGenerator.COMPONENT_SEPARATOR;
            sb.append(sep).append(parentUUid);
        }
        sb.append(sep).append(localUUid);
        return sb.toString();
    }

    @Override
    public String getRootUUIDComponent() {
        return rootUUid;
    }

    @Override
    public String getParentUUIDComponent() {
        return parentUUid;
    }

    @Override
    public String getLocalUUIDComponent() {
        return localUUid;
    }

    @Override
    public RequestUUID getNewSubUUID() {
        // means that no uuid was passed into construction -> we are the root
        if (rootUUid == null) {
            return new RequestUUIDImpl(localUUid, localUUid, generator.getNextUUID());
        }
        return new RequestUUIDImpl(rootUUid, localUUid, generator.getNextUUID());
    }
}
