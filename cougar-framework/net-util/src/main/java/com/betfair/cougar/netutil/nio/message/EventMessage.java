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

import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.netutil.nio.CougarProtocolEncoder;
import org.apache.mina.common.ByteBuffer;

import java.util.HashMap;
import java.util.Map;

public class EventMessage extends AbstractMessage implements ProtocolMessage {

    private Map<Byte, ByteBuffer> serialisedForms = new HashMap<Byte, ByteBuffer>();
    private byte[] payload;

    public EventMessage(byte[] payload) {//NOSONAR
        this.payload = payload;
        serialise();
    }

    private void serialise() {
        for (byte b=CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED; b<=CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED; b++) {
            serialisedForms.put(b, CougarProtocolEncoder.encode(this, b));
        }
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public ProtocolMessage.ProtocolMessageType getProtocolMessageType() {
        return ProtocolMessage.ProtocolMessageType.EVENT;
    }

    @Override
    public ByteBuffer getSerialisedForm(byte protocolVersion) {
        return serialisedForms.get(protocolVersion).duplicate();
    }
}