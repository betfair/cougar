package com.betfair.cougar.modules.zipkin.impl;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.modules.zipkin.impl.jetty.ZipkinHttpRequestUuidResolver;
import com.betfair.cougar.modules.zipkin.impl.socket.ZipkinSocketRequestUuidResolver;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class ZipkinExecutionContextResolverFactoryTest {

    private ZipkinExecutionContextResolverFactory victim;

    @Mock
    private ZipkinManager zipkinManager;

    private String cougarUUIDHeader = "X-UUID";
    private String uuidParentsHeader = "X-UUID-Parents";

    @Before
    public void init() {
        victim = new ZipkinExecutionContextResolverFactory();
        victim.setCougarUUIDHeader(cougarUUIDHeader);
        victim.setUuidParentsHeader(uuidParentsHeader);
        victim.setZipkinManager(zipkinManager);
    }

    @Test
    public void testGetName() {
        assertEquals("Zipkin ContextResolverFactory", victim.getName());
    }

    @Test
    public void resolvers_WhenUnderlyingTransportIsHttp_ShouldReturnZipkinHttpRequestUuidResolver() {
        Protocol protocol = Protocol.RESCRIPT;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0].getClass().equals(ZipkinHttpRequestUuidResolver.class));
    }

    @Test
    public void resolvers_WhenProtocolIsSocket_ShouldReturnZipkinSocketRequestUuidResolver() {
        Protocol protocol = Protocol.SOCKET;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertTrue(result[0].getClass().equals(ZipkinSocketRequestUuidResolver.class));
    }

    @Test
    public void resolvers_WhenProtocolIsNotHttpNorSocketBased_ShouldReturnNull() {
        Protocol protocol = Protocol.IN_PROCESS;

        DehydratedExecutionContextResolver[] result = victim.resolvers(protocol);
        assertNull(result);
    }
}
