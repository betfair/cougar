package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.CougarVersion;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link com.betfair.cougar.transport.jetty.CrossOriginHandler}
 */
public class CrossOriginHandlerTest {


    @Test
    public void testHandlerSetsServerHeaderInTheResponse() throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler("betfair.com", "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = Mockito.mock(MockJettyRequest.class);
        final MockJettyResponse res = Mockito.mock(MockJettyResponse.class);

        victim.handle("/", req, req, res);

        Mockito.verify(res, Mockito.times(1)).setHeader(eq("Server"), eq("Cougar 2 - " + CougarVersion.getVersion()));
    }

    @Test
    public void testHandlerMarksRequestAsHandledByDefault() throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler("betfair.com", "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = Mockito.mock(MockJettyRequest.class);
        final MockJettyResponse res = Mockito.mock(MockJettyResponse.class);

        victim.handle("/", req, req, res);

        Mockito.verify(req, Mockito.times(1)).setHandled(eq(true));
        Mockito.verify(req, Mockito.times(1)).setHandled(eq(false));
    }

    @Test
    public void testHandlerUnmarksRequestAsHandledIfFilterContinuesTheChain() throws Exception {
        final CrossOriginHandler victim = new CrossOriginHandler("betfair.com", "GET,POST,HEAD", "X-Requested-With,Content-Type,Accept,Origin", "1800", "true", "");
        final MockJettyRequest req = Mockito.mock(MockJettyRequest.class);
        final MockJettyResponse res = Mockito.mock(MockJettyResponse.class);

        Mockito.when(req.getMethod()).thenReturn("OPTIONS");
        Mockito.when(req.getHeader("Origin")).thenReturn("betfair.com");
        Mockito.when(req.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER)).thenReturn("PUT");
        Mockito.when(req.getHeaders("Connection")).thenReturn(Collections.<String>emptyEnumeration());

        victim.handle("/", req, req, res);

        Mockito.verify(req, Mockito.times(1)).setHandled(eq(true));
        Mockito.verify(req, Mockito.never()).setHandled(eq(false));
    }

}
