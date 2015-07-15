/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.common;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.TranscribableEnum;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration of valid values
 */
@SuppressWarnings("all")
public enum EchoExceptionErrorCodeEnum implements Externalizable, TranscribableEnum {
    /** * Generic Error */ GENERIC(1) ,UNRECOGNIZED_VALUE(null);

    private static Set<EchoExceptionErrorCodeEnum> validValues = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(EchoExceptionErrorCodeEnum.UNRECOGNIZED_VALUE)));
    public static Set<EchoExceptionErrorCodeEnum> validValues() { return validValues; }

    private String value;

    private EchoExceptionErrorCodeEnum(String value) {
        this.value=value;
    }

    private EchoExceptionErrorCodeEnum(int id) {
        this.value=String.format("%04d", id);
    }

    public String getCode() {
        return value;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(ordinal());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public static EchoExceptionErrorCodeEnum getInstance(ObjectInput in) throws IOException, ClassNotFoundException{
        int index = in.readInt();
        if (index<0 || index>=(EchoExceptionErrorCodeEnum.values().length)){
            throw new ClassNotFoundException("Invalid enum value");
        }
        return EchoExceptionErrorCodeEnum.values()[index];
    }

    public static final ServiceVersion SERVICE_VERSION = new ServiceVersion("v3.0");

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

}
