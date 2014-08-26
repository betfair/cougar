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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 *
 */
public class HttpsCommandValidatorTest {
    private HttpsCommandValidator validator = new HttpsCommandValidator();

    @Test
    public void defaultBehaviour() {
        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(command.getRequest()).thenReturn(request);
        when(request.getScheme()).thenReturn("https");

        // should throw no exception
        validator.validate(command);

        when(request.getScheme()).thenReturn("http");
        when(request.getHeader("Front-End-Https")).thenReturn("On");

        // should throw no exception
        validator.validate(command);


        when(request.getHeader("Front-End-Https")).thenReturn(null);
        try {
            validator.validate(command);
            fail("validate should throw exception if not secured");
        }
        catch (CougarException ce) {
            // should have happened, so ok
        }
    }

    @Test
    public void disabled() {
        validator.setEnabled(false);

        HttpCommand command = mock(HttpCommand.class);

        // should throw no exception
        validator.validate(command);
    }

    @Test
    public void onlyLocalTermination() {
        validator.setAllowExternalTermination(false);

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(command.getRequest()).thenReturn(request);
        when(request.getScheme()).thenReturn("http");

        try {
            validator.validate(command);
            fail("validate should throw exception if not secured");
        }
        catch (CougarException ce) {
            // should have happened, so ok
        }
    }

    @Test
    public void changeHeaderName() {
        validator.setExternalTerminationHeader("Some-Header");

        HttpCommand command = mock(HttpCommand.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(command.getRequest()).thenReturn(request);
        when(request.getScheme()).thenReturn("http");
        when(request.getHeader("Some-Header")).thenReturn("Wibble");

        // should throw no exception
        validator.validate(command);
    }
}
