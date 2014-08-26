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

/**
 * Interface to be implemented by any Executables which wrap other executables. Used by various Cougar internals to
 * find the wrapping they need to implement certain hooks.
 */
public interface ExecutableWrapper extends Executable {
    /**
     * Gets the executable wrapped by the wrapper.
     */
    Executable getWrappedExecutable();

    /**
     * Finds a child (recursive) of the given type. If no child matching this type is found then this methods returns null.
     */
    <T extends Executable> T findChild(Class<T> clazz);
}
