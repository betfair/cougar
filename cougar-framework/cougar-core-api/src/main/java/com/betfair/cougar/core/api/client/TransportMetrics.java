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

package com.betfair.cougar.core.api.client;

/**
 * Exposes some metrics and health measurements about the transport. Not all values may be relevant to all Transports.
 * Instances of this class should update themselves as the underlying transport state changes.
 */
public interface TransportMetrics {

    /**
     * Number of connections this transport has open to the service.
     *
     * @return the number of connections
     */
    int getOpenConnections();

    /**
     * The maximum number of connections this transport may open to the service.
     *
     * @return the maximum number of connections
     */
    int getMaximumConnections();


    /**
     * The number of connections available for requests.
     *
     * @return the number of available connections
     */
    int getFreeConnections();

    /**
     * A transport-specific measurement of how busy it is. Value should be normalised to 0-100.
     *
     * @return the current load on this transport.
     */
    int getCurrentLoad();
}
