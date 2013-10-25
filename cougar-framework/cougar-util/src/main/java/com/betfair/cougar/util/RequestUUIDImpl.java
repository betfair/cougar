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

package com.betfair.cougar.util;

import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.UUIDGenerator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUUIDImpl implements RequestUUID {

    private String uuid;

    private static UUIDGenerator generator;

    /**
     * Note, this sets the system wide generator, not just the local one for this instance, nasty due to spring
     */
    public RequestUUIDImpl(UUIDGenerator generator) {
        setGenerator(generator);
    }

    public RequestUUIDImpl() {
        this.uuid = generator.getNextUUID();
    }

    public RequestUUIDImpl(String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("The UUID argument cannot be null");
        }
        generator.validateUuid(uuid);
        this.uuid = uuid;
    }

    public RequestUUIDImpl(ObjectInput in) throws IOException {
        readExternal(in);
    }

    public static void setGenerator(UUIDGenerator generator) {
        RequestUUIDImpl.generator = generator;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        try {
            uuid = (String) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(uuid);
    }

    @Override
    public String toString() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        if (getClass() != rhs.getClass()) {
            return false;
        }
        RequestUUIDImpl that = (RequestUUIDImpl) rhs;
        return this.uuid.equals(that.uuid);
    }

    public String getUUID() {
        return uuid;
    }
}
