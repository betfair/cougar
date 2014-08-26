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

import com.betfair.cougar.core.api.events.Event;

/**
 * Abstract baseclass for Event transfer objects
 */
public abstract class AbstractEvent implements Event {
    private String messageId;
    private String cougarMessageRouteString;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCougarMessageRouteString() {
        return cougarMessageRouteString;
    }

    public void setCougarMessageRouteString(String cougarMessageRouteString) {
        this.cougarMessageRouteString = cougarMessageRouteString;
    }
}

