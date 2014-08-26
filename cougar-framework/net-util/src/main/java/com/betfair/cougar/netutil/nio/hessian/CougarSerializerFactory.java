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

import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.SerializerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CougarSerializerFactory extends SerializerFactory {

    private Set<TranscribableParams> transcriptionParams;

    private Set<String> missingTypes = Collections.newSetFromMap(new ConcurrentHashMap());

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

        return optimizedGetDeserializer(type);
    }

    /**
     * If a Cougar server response contains a class the client doesn't know about (which is legal and backwards compatible
     * in cases) then the default behavior of Hessian is to perform a lookup, fail, throw an exception and log it.
     * This has been measured at about 25 times slower than the happy path, and Hessian does not negatively cache 'misses',
     * so this is a per-response slowdown. This implementation caches type lookup misses, and so eradicates the problem.
     */
    private Deserializer optimizedGetDeserializer(String type)
            throws HessianProtocolException {
        if (missingTypes.contains(type)) {
            return null;
        }
        Deserializer answer = super.getDeserializer(type);
        if (answer == null) {
            missingTypes.add(type);
        }
        return answer;
    }

    /** Visible for testing */
    Set<String> getMissingTypes() {
        return missingTypes;
    }
}
