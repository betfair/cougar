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

import com.betfair.cougar.core.api.exception.CougarException;

/**
 * Interface for classes which want to be able to validate (and potentially reject) commands prior to execution of them.
 */
public interface CommandValidator<T extends TransportCommand> {

    /**
     * Called when validation required. Methods should throw an appropriate exception to prevent execution
     */
    void validate(T command) throws CougarException;
}
