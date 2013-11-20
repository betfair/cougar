package com.betfair.cougar.netutil.nio.hessian;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.SerializerFactory;
import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void testMissingTypesCache() throws Exception {

        int iterations = 1000;

        long defaultFactoryTiming = 0;
        suppressLogging();
        try {
            SerializerFactory defaultSerializerFactory = SerializerFactory.createDefault();
            defaultFactoryTiming = time(defaultSerializerFactory, iterations);
        }
        finally {
            unsuppressLogging();
        }

        CougarSerializerFactory cougarSerializerFactory = new CougarSerializerFactory(Collections.EMPTY_SET);
        long cougarSerializerFactoryTiming = time(cougarSerializerFactory, iterations);

        // make sure it's at least 10 times faster
        assertTrue(cougarSerializerFactoryTiming < defaultFactoryTiming / 10);
    }

    private long time(SerializerFactory factory, int iterations) throws Exception {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        for (int i = 0; i < iterations; i++) {
            factory.getDeserializer(MISSING_TYPE_NAME);
        }
        stopwatch.stop();
        return stopwatch.getTime();
    }

    private void suppressLogging() {
        Logger logger = Logger.getLogger(SerializerFactory.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    private void unsuppressLogging() {
        Logger logger = Logger.getLogger(SerializerFactory.class.getName());
        logger.setLevel(Level.WARNING);
    }
}