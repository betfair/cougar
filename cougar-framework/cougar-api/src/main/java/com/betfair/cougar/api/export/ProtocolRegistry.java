/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.api.export;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry of protocols.
 */
public class ProtocolRegistry {

    private static Map<ProtocolParadigm, Set<Protocol>> protocolsByParadigm = new HashMap<>();
    private static Set<Protocol> protocols = new HashSet<>();

    // todo:  (#83)
    static
    {
        for (Protocol p : Protocol.values()) {
            registerProtocol(p);
        }
    }

    public static void registerProtocol(Protocol p) {
        protocols.add(p);
        for (ProtocolParadigm pp : p.getParadigms()) {
            Set<Protocol> protocols = protocolsByParadigm.get(pp);
            if (protocols == null) {
                protocols = new HashSet<>();
                protocolsByParadigm.put(pp, protocols);
            }
            protocols.add(p);
        }
    }

    public static Set<Protocol> protocols(ProtocolParadigm paradigm) {
        return protocolsByParadigm.get(paradigm);
    }
}
