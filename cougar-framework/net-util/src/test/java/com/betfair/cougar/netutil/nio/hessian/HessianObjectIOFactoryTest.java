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

import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class HessianObjectIOFactoryTest {

    private static final byte PROTOCOL_VERSION = 0x01;

    private HessianObjectIOFactory target = new HessianObjectIOFactory(true);

    @Test
    public void testSerializationAndDeserialization() throws Exception {

        AnObject originalObject = new AnObject();
        originalObject.setField("foo");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CougarObjectOutput out = target.newCougarObjectOutput(baos, PROTOCOL_VERSION);
        out.writeObject(originalObject);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        CougarObjectInput in = target.newCougarObjectInput(bais, PROTOCOL_VERSION);
        AnObject deserializedObject = (AnObject) in.readObject();
        in.close();

        assertNotNull(deserializedObject);
        assertEquals(originalObject.getField(), deserializedObject.getField());
    }

    @Test
    /**
     * The file AnObjectThatDoesNotExist.ser contains the serialized form of a class called AnObjectThatDoesNotExist,
     * The instance that was serialized had a property called 'field' with a value of 'bar'.
     * The class was then deleted, so it can no longer be found by the classloader.
     * Default Hessian deserialization behavior on ClassNotFoundException is to return the fields in a Map.
     */
    public void testDeserializationOfUnknownType() throws Exception {

        InputStream is = getClass().getClassLoader().getResourceAsStream("AnObjectThatDoesNotExist.ser");
        CougarObjectInput in = target.newCougarObjectInput(is, PROTOCOL_VERSION);
        Map deserialized = (Map) in.readObject();
        in.close();

        assertNotNull(deserialized);
        assertEquals(HashMap.class, deserialized.getClass());
        assertEquals(1, deserialized.size());
        assertEquals("bar", deserialized.get("field"));
    }
}
