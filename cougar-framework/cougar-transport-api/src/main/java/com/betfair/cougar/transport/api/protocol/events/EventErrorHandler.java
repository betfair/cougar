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

package com.betfair.cougar.transport.api.protocol.events;

/**
 * This interface describes an implementation of an error handler for the supplied Event based
 * transport.  Note that a default implementation will be made available, but this will likely
 * offer extremely basic functionality.  If you require anything more elaborate, then implement
 * this interface and wire your implementation in instead of the default one.
 */
public interface EventErrorHandler<T> {
    public void handleEventProcessingError(T errorEvent, Throwable exception);
}
