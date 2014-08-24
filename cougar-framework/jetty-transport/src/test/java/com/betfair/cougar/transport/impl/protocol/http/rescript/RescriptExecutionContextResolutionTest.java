package com.betfair.cougar.transport.impl.protocol.http.rescript;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpContextResolutionTest;

/**
 */
public class RescriptExecutionContextResolutionTest extends AbstractHttpContextResolutionTest {
    @Override
    protected Protocol getProtocol() {
        return Protocol.RESCRIPT;
    }
}
