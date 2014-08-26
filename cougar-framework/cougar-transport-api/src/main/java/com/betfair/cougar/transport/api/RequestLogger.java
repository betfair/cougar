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

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;

import javax.ws.rs.core.MediaType;

public interface RequestLogger {

    /**
     * Creates a comma-delimited Access Log entry of the following format:
     * <ol>
     * <li>received date-time, format yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>request UUID</li>
     * <li>path</li>
     * <li>channel id</li>
     * <li>response content type</li>
     * <li>user IP address</li>
     * <li>user country</li>
     * <li>user agent</li>
     * <li>response code</li>
     * <li>process time in nanoseconds</li>
     * <li>number of bytes read</li>
     * <li>number of bytes written</li>
     * <li>request media sub-type</li>
     * <li>response media sub-type</li>
     * </ol>
     * @param command Contains the request, response and timer
     * @param context Contains resolved channel and location details
     * @param bytesRead Number of bytes read from the request
     * @param bytesWritten Number of bytes written to the response
     * @param requestMediaType
     * @param responseMediaType
     * @param responseCode
     */
    public void logAccess(final HttpCommand command,
            final ExecutionContext context, final long bytesRead,
            final long bytesWritten, final MediaType requestMediaType,
            final MediaType responseMediaType, final ResponseCode responseCode);
}
