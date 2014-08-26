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

import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.TranscribableEnum;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.IOExceptionWrapper;

import java.io.IOException;
import java.util.Set;

/**
 * Deserializing an enum valued object
 */
public class TranscribableEnumDeserializer extends AbstractDeserializer {

    private Class _enumType;

    private Set<TranscribableParams> transcriptionParams;

    public TranscribableEnumDeserializer(Class cl, Set<TranscribableParams> transcriptionParams) {
        if (TranscribableEnum.class.isAssignableFrom(cl)) {
            _enumType = cl;
        }
        else {
            throw new RuntimeException("Class " + cl.getName() + " is not a TranscribableEnum");
        }
        this.transcriptionParams = transcriptionParams;
    }

    public Class getType() {
        return _enumType;
    }

    public Object readMap(AbstractHessianInput in) throws IOException {
        String name = null;

        while (!in.isEnd()) {
            String key = in.readString();

            if (key.equals("name")) {
                name = in.readString();
            }
            else {
                in.readObject();
            }
        }

        in.readMapEnd();

        Object obj = transcriptionParams.contains(TranscribableParams.EnumsWrittenAsStrings) ? name : create(name);

        in.addRef(obj);

        return obj;
    }

    // called when as a result of a call to TranscribableDeserialiser
    public Object readObject(AbstractHessianInput in) throws IOException {
        return in.readString();
    }

    @Override
    public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
        String[] fieldNames = (String[]) fields;
        String name = null;

        for (int i = 0; i < fieldNames.length; i++) {
            if ("name".equals(fieldNames[i])) {
                name = in.readString();
            }
            else {
                in.readObject();
            }
        }

        Object obj = create(name);

        in.addRef(obj);

        return obj;
    }

    private Object create(String name) throws IOException {
        if (name == null) {
            throw new IOException(_enumType.getName() + " expects name.");
        }

        try {
            return EnumUtils.readEnum(_enumType, name);
        } catch (EnumDerialisationException cfe) {
            throw cfe;
        } catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }
}