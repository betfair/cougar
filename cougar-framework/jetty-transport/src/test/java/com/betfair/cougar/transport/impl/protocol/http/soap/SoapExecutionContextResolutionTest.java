package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.transport.api.TransportCommand;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpCommandProcessorTest;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpContextResolutionTest;
import org.apache.axiom.om.OMElement;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SoapExecutionContextResolutionTest extends AbstractHttpContextResolutionTest {
    @Override
    protected Protocol getProtocol() {
        return Protocol.SOAP;
    }

}
