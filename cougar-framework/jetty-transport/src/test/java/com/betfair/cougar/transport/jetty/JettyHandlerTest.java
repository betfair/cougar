/*
 * Copyright 2014, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.transport.jetty;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityTokenResolver;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import com.betfair.cougar.transport.api.protocol.ProtocolBinding;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for @see JettyHandler
 */
public class JettyHandlerTest {

    private JettyHandler jettyHandler;
    private TransportCommandProcessor<HttpCommand> commandProcessor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Request jettyRequest;

    public static final String PROTOCOL_BINDING_CONTEXT = "/wibble";
    private JettyHandlerSpecification jettyHandlerSpec;

    @Before
    public void init() {
        commandProcessor = Mockito.mock(TransportCommandProcessor.class);

        jettyHandlerSpec = new JettyHandlerSpecification(PROTOCOL_BINDING_CONTEXT, Protocol.RESCRIPT, "serviceContext");

        jettyHandler = new JettyHandler(commandProcessor, jettyHandlerSpec, null, true);


        jettyRequest = Mockito.mock(Request.class);
        request = Mockito.mock(HttpServletRequest.class);
        Continuation c = Mockito.mock(Continuation.class);
        when(request.getAttribute(eq(Continuation.ATTRIBUTE))).thenReturn(c);

        response = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testHandle() throws Exception {
        when(request.getContextPath()).thenReturn(PROTOCOL_BINDING_CONTEXT + "/someotherurl");

        jettyHandler.handle(null, jettyRequest, request, response);
        verify(commandProcessor).process(any(HttpCommand.class));
    }

    @Test
    public void testHandle2() throws Exception {
        when(request.getContextPath()).thenReturn(PROTOCOL_BINDING_CONTEXT + "/someotherurl");

        jettyHandler.handle(null, jettyRequest, request, response);

        ArgumentCaptor<HttpCommand> commandArgumentCaptor = ArgumentCaptor.forClass(HttpCommand.class);
        verify(commandProcessor).process(commandArgumentCaptor.capture());
        //The default implementation of the jettyhandler has no identityTokenLookup thingo specified, so verify
        //that the command has no identityTokenResolver set on it
        HttpCommand command = commandArgumentCaptor.getValue();
        assertNull("Should not have an IdentityTokenResolver set", command.getIdentityTokenResolver());
    }

    @Test
    public void testHandle3() throws Exception {

        IdentityTokenResolver resolver = Mockito.mock(IdentityTokenResolver.class);
        JettyHandler.IdentityTokenResolverLookup identityTokenResolverLookup =
                Mockito.mock(JettyHandler.IdentityTokenResolverLookup.class);
        when(identityTokenResolverLookup.lookupIdentityTokenResolver(anyString())).thenReturn(resolver);

        JettyHandler jh = new JettyHandler(commandProcessor, null, identityTokenResolverLookup, true);
        jh.handle(null, jettyRequest, request, response);

        ArgumentCaptor<HttpCommand> commandArgumentCaptor = ArgumentCaptor.forClass(HttpCommand.class);
        verify(commandProcessor).process(commandArgumentCaptor.capture());
        //The default implementation of the jettyhandler has no identityTokenLookup thingo specified, so verify
        //that the command has no identityTokenResolver set on it
        HttpCommand command = commandArgumentCaptor.getValue();
        assertEquals("This command should have an identityTokenResolver set", resolver, command.getIdentityTokenResolver());
    }

    @Test
    public void testHandleGenericException() throws Exception {
        JettyHandler.IdentityTokenResolverLookup identityTokenResolverLookup =
                Mockito.mock(JettyHandler.IdentityTokenResolverLookup.class);
        when(identityTokenResolverLookup.lookupIdentityTokenResolver(anyString())).thenThrow(new IllegalArgumentException());

        JettyHandler jh = new JettyHandler(commandProcessor, null, identityTokenResolverLookup, true);
        jh.handle(null, jettyRequest, request, response);
        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testHandleCougarException() throws Exception {
        JettyHandler.IdentityTokenResolverLookup identityTokenResolverLookup =
                Mockito.mock(JettyHandler.IdentityTokenResolverLookup.class);
        when(identityTokenResolverLookup.lookupIdentityTokenResolver(anyString())).thenThrow(new CougarValidationException(ServerFaultCode.NoSuchService));

        JettyHandler jh = new JettyHandler(commandProcessor, null, identityTokenResolverLookup, true);
        jh.handle(null, jettyRequest, request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }


    @Test
    public void testCommandInstantiationWithBindingPath() {
        String operationPath = "/serviceContext/v2/operationUri";
        String fullpath = PROTOCOL_BINDING_CONTEXT + operationPath;
        when(request.getContextPath()).thenReturn(fullpath);

        HttpCommand cmd = jettyHandler.new JettyTransportCommand(request, response);

        assertEquals("Full path was not equal", cmd.getFullPath(), fullpath);
        assertEquals("Operation Path was not equal", cmd.getOperationPath(), operationPath);
    }

    @Test
    public void testCommandInstantiationWithoutBindingPath() {
        JettyHandlerSpecification spec = new JettyHandlerSpecification(null, Protocol.RESCRIPT, "serviceContext");
        JettyHandler jh = new JettyHandler(commandProcessor, spec, null, true);

        String operationPath = "/serviceContext/v2/operationUri";
        String fullpath = operationPath;
        when(request.getContextPath()).thenReturn(fullpath);

        HttpCommand cmd = jh.new JettyTransportCommand(request, response);

        assertEquals("Full path was not equal", cmd.getFullPath(), fullpath);
        assertEquals("Operation Path was not equal", cmd.getOperationPath(), operationPath);
    }

    @Test
    public void perf() {
        JettyHandlerSpecification spec = new JettyHandlerSpecification("/q", Protocol.RESCRIPT, "serviceContext");

        ProtocolBinding pb = new ProtocolBinding("/q", null, Protocol.RESCRIPT);
        JettyHandler jh = new JettyHandler(null, spec, null, true);

        final String cp = "/asdfasldkfjasdf/";
        final String pathInfo = "/qqqqq";

        final Continuation c = new Continuation() {
            @Override
            public void setTimeout(long timeoutMs) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void suspend() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void suspend(ServletResponse response) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void resume() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void complete() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isSuspended() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isResumed() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isExpired() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isInitial() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isResponseWrapped() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServletResponse getServletResponse() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addContinuationListener(ContinuationListener listener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setAttribute(String name, Object attribute) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getAttribute(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeAttribute(String name) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void undispatch() throws ContinuationThrowable {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        HttpServletRequest req = new HttpServletRequest() {
            @Override
            public String changeSessionId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long getContentLengthLong() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void login(String s, String s2) throws ServletException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void logout() throws ServletException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<Part> getParts() throws IOException, ServletException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Part getPart(String s) throws IOException, ServletException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServletContext getServletContext() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public AsyncContext startAsync() throws IllegalStateException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isAsyncStarted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isAsyncSupported() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public AsyncContext getAsyncContext() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public DispatcherType getDispatcherType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getAuthType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Cookie[] getCookies() {
                return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long getDateHeader(String name) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getHeader(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Enumeration getHeaders(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Enumeration getHeaderNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getIntHeader(String name) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getMethod() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getPathInfo() {
                return pathInfo;
            }

            @Override
            public String getPathTranslated() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getContextPath() {
                return cp;
            }

            @Override
            public String getQueryString() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRemoteUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Principal getUserPrincipal() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRequestedSessionId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRequestURI() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public StringBuffer getRequestURL() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getServletPath() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public HttpSession getSession(boolean create) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public HttpSession getSession() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getAttribute(String name) {
                return c;
            }

            @Override
            public Enumeration getAttributeNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getCharacterEncoding() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getContentLength() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getContentType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getParameter(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Enumeration getParameterNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String[] getParameterValues(String name) {
                return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Map getParameterMap() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getProtocol() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getScheme() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getServerName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getServerPort() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRemoteAddr() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRemoteHost() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setAttribute(String name, Object o) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeAttribute(String name) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Locale getLocale() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Enumeration getLocales() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isSecure() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getRealPath(String path) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getRemotePort() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getLocalName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getLocalAddr() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getLocalPort() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        HttpServletResponse resp = new HttpServletResponse() {

            @Override
            public int getStatus() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getHeader(String s) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<String> getHeaders(String s) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<String> getHeaderNames() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addCookie(Cookie cookie) {
                //To change body of implemented methods use File | Settings | File Templates.;
            }

            @Override
            public boolean containsHeader(String name) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String encodeURL(String url) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String encodeRedirectURL(String url) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String encodeUrl(String url) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String encodeRedirectUrl(String url) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void sendError(int sc) throws IOException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setDateHeader(String name, long date) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addDateHeader(String name, long date) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setHeader(String name, String value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addHeader(String name, String value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setIntHeader(String name, int value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addIntHeader(String name, int value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setStatus(int sc) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setStatus(int sc, String sm) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getCharacterEncoding() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getContentType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCharacterEncoding(String charset) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setContentLength(int len) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setContentType(String type) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setBufferSize(int size) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getBufferSize() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void flushBuffer() throws IOException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void resetBuffer() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isCommitted() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void reset() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLocale(Locale loc) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Locale getLocale() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setContentLengthLong(long len) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        for (int i=0; i<100000; i++) {
            HttpCommand cmd = jh.new JettyTransportCommand(req, resp);
        }


        long start = System.currentTimeMillis();
        for (int i=0; i<10000000; i++) {
            HttpCommand cmd = jh.new JettyTransportCommand(req, resp);

        }
        System.out.println("Execution time: " + (System.currentTimeMillis()-start));
    }

    @Test
    public void testSingletonIdentityTokenResolution() {
        IdentityTokenResolver identityTokenResolver = Mockito.mock(IdentityTokenResolver.class);
        JettyHandler.SingletonIdentityTokenResolverLookup lookup =
                new JettyHandler.SingletonIdentityTokenResolverLookup(identityTokenResolver);

        assertEquals("Incorrect identityTokenResolver returned", identityTokenResolver, lookup.lookupIdentityTokenResolver("xxx"));
    }

    @Test
    public void testGeneralIdentityTokenResolutionVersionScrubbing() {
        final String contextRoot = "/serviceUri";
        JettyHandler.GeneralHttpIdentityTokenResolverLookup lookup =
                new JettyHandler.GeneralHttpIdentityTokenResolverLookup(contextRoot, Mockito.mock(JettyHandlerSpecification.class));

        assertEquals("failed to extract version correctly", "v2", lookup.extractVersion("/serviceUri/v2.0/foo?arg=bar"));
        assertEquals("failed to extract version correctly", "v2", lookup.extractVersion("/serviceUri/V2.0/foo?arg=bar"));
        assertEquals("failed to extract version correctly", "v3", lookup.extractVersion("/serviceUri/v3.600/foo?arg=bar"));
        assertEquals("failed to extract version correctly", "v3", lookup.extractVersion("/serviceUri/V3.600/foo?arg=bar"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/v4/foo?arg=bar"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/v4/"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/v4.5/"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/V4.5/"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/v4"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/v4.5"));
        assertEquals("failed to extract version correctly", "v4", lookup.extractVersion("/serviceUri/V4.5"));

        try {
            lookup.extractVersion("/thereIsNoVersionHere");
            fail("This should have thrown an exception as the uri did not contain a version");
        } catch (CougarValidationException expected) {}
    }

    @Test
    public void testGeneralIdentityTokenResolutionLookup() {
        final String contextRoot = "/serviceUri";
        IdentityTokenResolver identityTokenResolver1 = Mockito.mock(IdentityTokenResolver.class);
        IdentityTokenResolver identityTokenResolver2 = Mockito.mock(IdentityTokenResolver.class);

        JettyHandlerSpecification spec = new JettyHandlerSpecification("", Protocol.RESCRIPT, contextRoot);
        spec.addServiceVersionToTokenResolverEntry(new ServiceVersion("v1.2"), identityTokenResolver1);
        spec.addServiceVersionToTokenResolverEntry(new ServiceVersion("v2.4"), identityTokenResolver2);

        JettyHandler.GeneralHttpIdentityTokenResolverLookup lookup =
                new JettyHandler.GeneralHttpIdentityTokenResolverLookup(contextRoot, spec);

        assertEquals("We didn't get the IdentityTokenResolver we anticipated", identityTokenResolver1,
                lookup.lookupIdentityTokenResolver(contextRoot + "/v1.1/myInterestingOperation?arg=foo"));

        assertEquals("We didn't get the IdentityTokenResolver we anticipated", identityTokenResolver2,
                lookup.lookupIdentityTokenResolver(contextRoot + "/v2.4/myInterestingOperation?arg=foo"));

        assertEquals("We didn't get the IdentityTokenResolver we anticipated", identityTokenResolver2,
                lookup.lookupIdentityTokenResolver(contextRoot + "/v2/myInterestingOperation?arg=foo"));

        try {
            lookup.lookupIdentityTokenResolver("noVersionHere");
            fail("This should have thrown an exception as the uri did not contain a version");
        } catch (CougarValidationException expected) {}
    }
}

