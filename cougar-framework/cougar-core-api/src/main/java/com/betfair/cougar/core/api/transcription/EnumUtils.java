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

package com.betfair.cougar.core.api.transcription;

/**
 *
 */
public class EnumUtils {

    public static final String MISS_VALUE = "UNRECOGNIZED_VALUE";

    private static ThreadLocal<Boolean> hardFailure = new ThreadLocal<Boolean>();

    public static void setHardFailureForThisThread(boolean b) {
        hardFailure.set(b);
    }

    public static Boolean getHardFailureForThisThread() {
        return hardFailure.get();
    }

    public static <T extends Enum<T>> T readEnum(Class<T> cls, String name) throws IllegalArgumentException, NullPointerException {
        Boolean mode = hardFailure.get();
        if (mode == null) {
            mode = true;
        }
        return readEnum(cls, name, mode);
    }

    public static <T extends Enum<T>> T readEnum(Class<T> cls, String name, boolean hardFail) throws IllegalArgumentException, NullPointerException {
        try {
            if (MISS_VALUE.equals(name)) {
                throw new EnumDerialisationException("It is invalid to pass in an enum with (special) value: "+MISS_VALUE);
            }
            return Enum.valueOf(cls, name);
        }
        catch (IllegalArgumentException iae) {
            if (!hardFail) {
                return Enum.valueOf(cls, MISS_VALUE);
            }
            else {
                throw new EnumDerialisationException(iae);
            }
        }
    }
}
