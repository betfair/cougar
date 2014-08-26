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

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Serializing an object for known object types.
 */
public class TranscribableEnumSerializer extends AbstractSerializer {
    private Method _name;
    private ServiceVersion _serviceVersion;
    private Set<TranscribableParams> transcriptionParams;

    public TranscribableEnumSerializer(Class cl, Set<TranscribableParams> transcriptionParams)
    {
        try {
            _name = cl.getMethod("name", new Class[0]);
            _serviceVersion = (ServiceVersion) cl.getDeclaredField("SERVICE_VERSION").get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.transcriptionParams = transcriptionParams;
    }

    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }

        Class cl = obj.getClass();

        String name = null;
        try {
            name = (String) _name.invoke(obj, (Object[]) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // if we're talking to an older version we need to migrate vMajor to vMajor_Minor
        String className = cl.getName();
        if (!transcriptionParams.contains(TranscribableParams.MajorOnlyPackageNaming)) {
            className = ClassnameCompatibilityMapper.toMajorMinorPackaging(cl, _serviceVersion);
        }
        int ref = out.writeObjectBegin(className);

        if (ref < -1) {
            out.writeString("name");
            out.writeString(name);
            out.writeMapEnd();
        }
        else {
            if (ref == -1) {
                out.writeClassFieldLength(1);
                out.writeString("name");
                out.writeObjectBegin(className);
            }

            out.writeString(name);
        }
    }
}
