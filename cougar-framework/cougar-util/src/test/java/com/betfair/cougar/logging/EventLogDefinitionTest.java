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

package com.betfair.cougar.logging;

import java.io.IOException;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.logging.handlers.AbstractLogHandler;
import org.junit.Test;


public class EventLogDefinitionTest extends CougarUtilTestCase {

    EventLoggingRegistry registry = new EventLoggingRegistry();

    @Test(expected = IllegalArgumentException.class)
    public void testGetConcreteLogger() {
        addHandler("foo", true);
        registry.registerConcreteLogger(null,"bar");
        EventLogDefinition eld = registry.getInvokableLogger("foobar");
        assertEquals(eld.getLogName(), "foobar");
        assertEquals(eld.isAbstract(), false);

        registry.registerConcreteLogger("ns","bar");
        eld = registry.getInvokableLogger("nsfoobar");
        assertEquals(eld.getLogName(), "nsfoobar");
        assertEquals(eld.isAbstract(), false);

        registry.registerConcreteLogger("ns","foo");
        eld = registry.getInvokableLogger("nsfoofoo");
        assertEquals(eld.getLogName(), "nsfoofoo");
        assertEquals(eld.isAbstract(), false);

        try {
            eld.getConcreteLogger("ns","foo");
            assertFalse("IllegalArgumentException expected", true);
        } catch (IllegalArgumentException e) {
        }


    }

    private void addHandler(String name, boolean isAbstract) {
        EventLogDefinition eld = new EventLogDefinition();
        eld.setRegistry(registry);
        eld.setAbstract(isAbstract);
        eld.setHandler(new AbstractLogHandler(isAbstract) {
            @Override
            protected AbstractLogHandler cloneHandlerToName(String loggerName, String serviceName, String namespace) throws IOException {
                return this;
            }
        });
        eld.setLogName(name);
        eld.register();

    }

}
