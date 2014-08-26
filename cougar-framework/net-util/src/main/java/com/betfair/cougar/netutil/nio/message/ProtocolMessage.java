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

import org.apache.mina.common.ByteBuffer;


/**
 * This class represents an message for the envelope protocol to
 * be used for handshake / keepalive / ...
 */
public interface ProtocolMessage {

    public enum ProtocolMessageType {
        CONNECT((byte)0),
        ACCEPT((byte)1),
        REJECT((byte)2),
        KEEP_ALIVE((byte)3),
        MESSAGE((byte)4),
        DISCONNECT((byte)5),
        MESSAGE_REQUEST((byte)6),
        MESSAGE_RESPONSE((byte)7),
        EVENT((byte)8),
        SUSPEND((byte)9),
        START_TLS_REQUEST((byte)10),
        START_TLS_RESPONSE((byte)11);

        private byte messageType;

        private ProtocolMessageType(byte messageType) {
            this.messageType = messageType;
        }

        public byte getMessageType() {
            return messageType;
        }

        public static ProtocolMessageType getMessageByMessageType(byte messageType) {
            for (ProtocolMessageType pm : values()) {
                if (pm.messageType == messageType){
                    return pm;
                }
            }
            throw new IllegalArgumentException("message type: " + messageType  + " not found");
        }

    }
    public ProtocolMessageType getProtocolMessageType();
    public ByteBuffer getSerialisedForm(byte protocolVersion);
}
