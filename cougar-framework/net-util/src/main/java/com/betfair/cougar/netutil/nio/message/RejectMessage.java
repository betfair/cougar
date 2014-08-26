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

 */
public class RejectMessage extends AbstractMessage implements ProtocolMessage{

    private byte[] acceptableVersions;
    private RejectMessageReason rejectReason;

    public RejectMessage(RejectMessageReason reason, byte[] acceptableVersions) {//NOSONAR
        this.rejectReason = reason;
        this.acceptableVersions = acceptableVersions;
    }

    public byte[] getAcceptableVersions() {
        return acceptableVersions;
    }

    public RejectMessageReason getRejectReason() {
        return rejectReason;
    }

    @Override
    public ProtocolMessage.ProtocolMessageType getProtocolMessageType() {
        return ProtocolMessageType.REJECT;
    }
}
