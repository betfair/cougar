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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.core.api.transports.EventTransport;

/**
 * Interface that a client executable resolver needs to implement in order
 * for the cougar client to be registered with EV dynamically i.e.
 * programmatically at runtime as opposed to via static spring config
 */
public interface RegisterableClientExecutableResolver extends ExecutableResolver {

    public void setDefaultOperationTransport(Executable defaultOperationTransport);

    public void setEventTransport(EventTransport eventTransport);

    public void init();
}
