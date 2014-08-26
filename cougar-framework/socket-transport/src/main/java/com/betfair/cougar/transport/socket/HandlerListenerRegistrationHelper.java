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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.netutil.nio.HandlerListener;
import com.betfair.cougar.transport.nio.ExecutionVenueServerHandler;

import java.util.List;

/**
 * Registers IOHandlerListeners with ExecutionVenueServerHandler
 */
public class HandlerListenerRegistrationHelper {
    private List<HandlerListener> handlers;
    private ExecutionVenueServerHandler serverHandler;

    public void setHandlers(List<HandlerListener> handlers) {
        this.handlers = handlers;
    }

    public void setServerHandler(ExecutionVenueServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public void start() {
        if (handlers != null && !handlers.isEmpty() && serverHandler != null) {
            for (HandlerListener handler : handlers) {
                serverHandler.addListener(handler);
            }
        }
    }
}
