/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.api;

import java.io.Externalizable;

/**
 * Represents a unique identifier for a request. May be received from transports and sent to servers to allow
 * identification of traffic flows all stemming from a common source. Consists of three components, each of which is
 * individually a guid. If this uuid is the root of a tree then the root and parent components will be null.
 */
public interface RequestUUID {
    /**
     * String representation of this uuid. Contains any/all relevant component uuids.
     */
	String getUUID();

    /**
     * Returns the representation to be used in standard cougar logs, including but not limited to request, access and
     * trace logs. This must always return a string compatible with the default cougar RequestUUID implementation.
     */
    String toCougarLogString();

    /**
     * Get the root component of this uuid. Returns null if there is none.
     */
    String getRootUUIDComponent();
    /**
     * Get the parent component of this uuid. Returns null if there is none.
     */
    String getParentUUIDComponent();
    /**
     * Get the local component of this uuid. Always returns a valid string.
     */
    String getLocalUUIDComponent();

    /**
     * Obtain a new sub-uuid.
     */
    RequestUUID getNewSubUUID();
}
