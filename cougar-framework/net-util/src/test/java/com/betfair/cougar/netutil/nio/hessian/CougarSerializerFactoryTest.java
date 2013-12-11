package com.betfair.cougar.netutil.nio.hessian;

import com.caucho.hessian.io.Deserializer;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class CougarSerializerFactoryTest {

    private static final String MISSING_TYPE_NAME = "com.betfair.cougar.transport.api.protocol.hessian.AnObjectThatDoesNotExist";

    @Test
    public void testDeserializerFound() throws Exception {
        CougarSerializerFactory factory = new CougarSerializerFactory(Collections.EMPTY_SET);
        Deserializer deserializer = factory.getDeserializer(Integer.class.getName());
        assertNotNull(deserializer);
        Set<String> missingTypes = factory.getMissingTypes();
        assertEquals(0, missingTypes.size());
    }

    @Test
    public void testNoDeserializerFound() throws Exception {
        CougarSerializerFactory factory = new CougarSerializerFactory(Collections.EMPTY_SET);
        Deserializer deserializer = factory.getDeserializer(MISSING_TYPE_NAME);
        assertNull(deserializer);
        Set<String> missingTypes = factory.getMissingTypes();
        assertEquals(1, missingTypes.size());
        assertTrue(missingTypes.contains(MISSING_TYPE_NAME));
    }

    @Test
    public void testMissingTypeCacheHit() throws Exception {
        String presentType = getClass().getName();
        CougarSerializerFactory factory = new CougarSerializerFactory(Collections.EMPTY_SET);
        Deserializer deserializer = factory.getDeserializer(presentType);
        assertNotNull(deserializer);

        Set<String> missingTypes = factory.getMissingTypes();
        missingTypes.add(presentType);
        deserializer = factory.getDeserializer(presentType);
        assertNull(deserializer);
    }
}