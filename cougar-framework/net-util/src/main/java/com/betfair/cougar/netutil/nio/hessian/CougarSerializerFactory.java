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

package com.betfair.cougar.netutil.nio.hessian;

import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.SerializerFactory;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CougarSerializerFactory extends SerializerFactory {


    private Set<TranscribableParams> transcriptionParams;

    public CougarSerializerFactory(Set<TranscribableParams> transcriptionParams) {
        this.transcriptionParams = transcriptionParams;
    }

    public static CougarSerializerFactory createInstance(Set<TranscribableParams> transcriptionParams) {
        return new CougarSerializerFactory(transcriptionParams);
    }

    /**
     * Returns a deserializer based on a string type.
     */
    public Deserializer getDeserializer(String type)
            throws HessianProtocolException
    {
        if (type == null || type.equals("")) {
            return null;
        }

        if (!transcriptionParams.contains(TranscribableParams.MajorOnlyPackageNaming)) {
            // look for vMajor
            type = ClassnameCompatibilityMapper.toMajorOnlyPackaging(type);
        }
        return super.getDeserializer(type);
    }
}
