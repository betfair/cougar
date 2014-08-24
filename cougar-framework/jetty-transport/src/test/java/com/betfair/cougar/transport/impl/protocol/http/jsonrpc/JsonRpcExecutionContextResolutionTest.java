package com.betfair.cougar.transport.impl.protocol.http.jsonrpc;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpContextResolutionTest;

public class JsonRpcExecutionContextResolutionTest extends AbstractHttpContextResolutionTest {
    @Override
    protected Protocol getProtocol() {
        return Protocol.JSON_RPC;
    }
}
