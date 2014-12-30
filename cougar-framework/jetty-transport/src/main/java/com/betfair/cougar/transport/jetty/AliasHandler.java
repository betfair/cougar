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
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Allows simple mapping of one path to another. Used in place of the default JettyHandler.
 */
public class AliasHandler extends JettyHandler {

    private Map<String, String> pathAliases;

    public AliasHandler(TransportCommandProcessor<HttpCommand> commandProcessor, boolean suppressCommasInAccessLog, Map<String, String> pathAliases) {
        super(commandProcessor, suppressCommasInAccessLog);
        this.pathAliases = pathAliases;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (pathAliases.containsKey(target)) {
            String newTarget = pathAliases.get(target);
            ServletContext context = request.getServletContext().getContext(newTarget);
            String newPath = newTarget.substring(context.getContextPath().length());
            context.getRequestDispatcher(newPath).forward(request, response);
            return;
        }
        super.handle(target, baseRequest, request, response);
    }
}
