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
