/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations.Mock;

/**
 *
 */
public class AliasHandlerTest {

    private Map<String, String> pathAliases = new HashMap<>();
    @Mock
    private TransportCommandProcessor<HttpCommand> commandProcessor;
    @Mock
    private Request baseRequest;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void aliasMiss() throws Exception {
        AliasHandler handler = new AliasHandler(commandProcessor, true, pathAliases);
        Continuation continuation = mock(Continuation.class);
        when(request.getAttribute(Continuation.ATTRIBUTE)).thenReturn(continuation);
        handler.handle("/missPath", baseRequest, request, response);
        verify(commandProcessor, times(1)).process(any(HttpCommand.class));
    }

    @Test
    public void aliasHit() throws IOException, ServletException {
        AliasHandler handler = new AliasHandler(commandProcessor, true, pathAliases);
        pathAliases.put("/hitPath","/context/alternatePath");

        ServletContext origContext = mock(ServletContext.class);
        ServletContext newContext = mock(ServletContext.class);
        when(request.getServletContext()).thenReturn(origContext);
        ArgumentCaptor<String> requestContextPath = ArgumentCaptor.forClass(String.class);
        when(origContext.getContext(requestContextPath.capture())).thenReturn(newContext);
        when(newContext.getContextPath()).thenReturn("/context");
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        ArgumentCaptor<String> requestDispatcherPath = ArgumentCaptor.forClass(String.class);
        when(newContext.getRequestDispatcher(requestDispatcherPath.capture())).thenReturn(requestDispatcher);

        handler.handle("/hitPath",baseRequest,request,response);
        verify(requestDispatcher, times(1)).forward(request, response);

        assertEquals("/context/alternatePath", requestContextPath.getValue());
        assertEquals("/alternatePath", requestDispatcherPath.getValue());
    }
}
