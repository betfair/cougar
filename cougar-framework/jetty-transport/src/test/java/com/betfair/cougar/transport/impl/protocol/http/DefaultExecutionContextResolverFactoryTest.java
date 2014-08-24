package com.betfair.cougar.transport.impl.protocol.http;


import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.apache.axiom.om.OMElement;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultExecutionContextResolverFactoryTest {

    @Test
    public void returnsSoapSpecific() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<HttpCommand,OMElement>[] resolvers = factory.resolvers(Protocol.SOAP);
        assertTrue(resolvers[0] instanceof SoapIdentityTokenResolver);
    }

    @Test
    public void otherHttp() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<HttpCommand,Void>[] resolvers = factory.resolvers(Protocol.RESCRIPT);
        assertTrue(resolvers[0] instanceof HttpIdentityTokenResolver);
    }

    @Test
    public void nonHttp() {
        DefaultExecutionContextResolverFactory factory = new DefaultExecutionContextResolverFactory();
        DehydratedExecutionContextResolver<?,?>[] resolvers = factory.resolvers(Protocol.SOCKET);
        assertNull(resolvers);
    }
}
