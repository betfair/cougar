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

package com.betfair.cougar.core.impl;

import com.betfair.cougar.core.impl.logging.NullLogBootstrap;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test case for @see CougarSpringCtxFactoryImpl. Note that this test is primarily concerned
 * with testing the log initialisation process
 */
public class CougarSpringCtxFactoryImplTest {

    @Test
    public void testEstablishLogInitialisationClass() {
        CougarSpringCtxFactoryImpl classUnderTest = new CougarSpringCtxFactoryImpl();
        //Start by confirming that null system property results in the default

        Class logInitialisationClass = classUnderTest.establishLogInitialisationClass(null);
        assertNotNull(logInitialisationClass);
        assertEquals(CougarSpringCtxFactoryImpl.DEFAULT_COUGAR_LOG_INIT_CLASS, logInitialisationClass);

        //Now check that we can chose one of our own doing
        logInitialisationClass = classUnderTest.establishLogInitialisationClass("com.betfair.cougar.core.impl.TestLogBootstrap");
        assertNotNull(logInitialisationClass);
        assertEquals(TestLogBootstrap.class, logInitialisationClass);

        //Next check that we are able to make the NullBootstrap run
        logInitialisationClass = classUnderTest.establishLogInitialisationClass("none");
        assertNotNull(logInitialisationClass);
        assertEquals(NullLogBootstrap.class, logInitialisationClass);

        //Finally ensure we get something sensible when nonsense is supplied
        logInitialisationClass = classUnderTest.establishLogInitialisationClass("wibble");
        assertNotNull(logInitialisationClass);
        assertEquals(CougarSpringCtxFactoryImpl.DEFAULT_COUGAR_LOG_INIT_CLASS, logInitialisationClass);
    }
}
