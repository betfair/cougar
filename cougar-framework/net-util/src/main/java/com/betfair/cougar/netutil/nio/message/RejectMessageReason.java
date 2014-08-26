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

package com.betfair.cougar.netutil.nio.message;

/**
 * This class represents the reason a connection request was rejected
 */
public enum RejectMessageReason {
    /**
     * Permanent error, trying again with the same versions isn't going to get you anywhere
     */
    INCOMPATIBLE_VERSION((byte)100),

    /**
     * Server isn't accepting requests right this instant, but it may be worth calling back later
     */
    SERVER_UNAVAILABLE((byte)101);

    private byte reasonCode;

    private RejectMessageReason(byte reasonCode) {
        this.reasonCode = reasonCode;
    }

    public byte getReasonCode() {
        return reasonCode;
    }

    public static RejectMessageReason getByReasonCode(byte reason) {
        for (RejectMessageReason rejectReason : values()) {
            if (rejectReason.reasonCode == reason) {
                return rejectReason;
            }
        }
        throw new IllegalArgumentException("Unknown reason code " + reason);
    }
}
