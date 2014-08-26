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

package com.betfair.cougar.transport.api;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.core.api.ServiceBindingDescriptor;

/**
 * Factory for creating TransportCommandProcessors that are bound to a specified service and channel resolver.
 *
 * @param <T> the type of TransportCommandProcessor that will be created
 */
public interface TransportCommandProcessorFactory<T extends TransportCommandProcessor> {

	T getCommandProcessor(Protocol protocol);

}
