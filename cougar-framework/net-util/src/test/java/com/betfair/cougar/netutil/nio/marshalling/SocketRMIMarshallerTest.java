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

package com.betfair.cougar.netutil.nio.marshalling;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.IdentityResolver;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.core.api.builder.ExecutionContextBuilder;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.transcription.TranscribableParams;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.core.impl.security.CommonNameCertInfoExtractor;
import com.betfair.cougar.marshalling.impl.RandomException;
import com.betfair.cougar.marshalling.impl.SimpleApplicationException;
import com.betfair.cougar.marshalling.impl.SimpleExecutionContext;
import com.betfair.cougar.marshalling.impl.SimpleGeoLocationDetails;
import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.transport.api.*;
import com.betfair.cougar.transport.impl.DehydratedExecutionContextResolutionImpl;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;
import com.betfair.cougar.util.geolocation.RemoteAddressUtils;
import com.google.common.collect.ImmutableMultiset;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Credential;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.api.security.InvalidCredentialsException;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.core.api.transcription.ParameterType.Type;
import com.betfair.cougar.core.api.transcription.Transcribable;
import com.betfair.cougar.core.api.transcription.TranscriptionInput;
import com.betfair.cougar.core.api.transcription.TranscriptionOutput;
import com.betfair.cougar.marshalling.impl.to.Cycle1;
import com.betfair.cougar.marshalling.impl.to.Cycle2;
import com.betfair.cougar.marshalling.impl.to.Foo;
import com.betfair.cougar.marshalling.impl.to.FooDelegateImpl;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.netutil.nio.hessian.HessianObjectIOFactory;
import com.betfair.cougar.transport.api.protocol.socket.InvocationRequest;
import com.betfair.cougar.transport.api.protocol.socket.InvocationResponse;
import com.betfair.cougar.util.geolocation.GeoIPLocator;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for the @see SocketRMIMarshallerTest class
 *
 */
@RunWith(Parameterized.class)
public class SocketRMIMarshallerTest {

    private byte protocolVersion;

    public SocketRMIMarshallerTest(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        List<Object[]> ret = new ArrayList<Object[]>();
        for (byte b=CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED; b<=CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED; b++) {
            ret.add(new Object[] {b});
        }
        return ret;
    }

    @BeforeClass
    public static void setup() {
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
    }

    private class PrincipalImpl implements Principal {
        private String name;
        public PrincipalImpl(String name) { this.name = name; }
        @Override public String getName() { return name; }
        @Override public boolean equals(Object that) { return EqualsBuilder.reflectionEquals(this, that); }
        @Override public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
    };

    private class CredentialImpl implements Credential {
        private String name, value;
        public CredentialImpl(String name, String value) { this.name = name; this.value = value; }
        @Override public String getName() { return name; }
        @Override public Object getValue() { return value; }
        @Override public boolean equals(Object that) { return EqualsBuilder.reflectionEquals(this, that); }
        @Override public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
    }

    private class IdentityImpl implements Identity {
        private Principal principal;
        private Credential credential;
        public IdentityImpl(Principal principal, Credential credential) {
            this.principal = principal; this.credential = credential;
        }
        @Override public Principal getPrincipal() { return principal; }
        @Override public Credential getCredential() { return credential; }
        @Override public boolean equals(Object that) { return EqualsBuilder.reflectionEquals(this, that); }
        @Override public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
    }

    private class IdentityChainImpl implements IdentityChain {
        private List<Identity> identities;
        public IdentityChainImpl(List<Identity> identities) { this.identities = identities; }
        @Override public void addIdentity(Identity identity) { identities.add(identity); }
        @Override public List<Identity> getIdentities() { return identities; }
        @Override public <T extends Identity> List<T> getIdentities(Class<T> clazz) { return null; }
        @Override public boolean equals(Object that) { return EqualsBuilder.reflectionEquals(this, that); }
        @Override public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
    }

    private class IdentityResolverImpl implements IdentityResolver {
        @Override
        // Convert identity chain into tokens
        public List<IdentityToken> tokenise(IdentityChain chain) {
            List<IdentityToken> tokens = new ArrayList<IdentityToken>();
            if (chain != null) {
                for (Identity identity : chain.getIdentities()) {
                    IdentityToken token = new IdentityToken(
                        identity.getPrincipal().getName(),
                        new StringBuilder(
                            identity.getCredential().getName())
                            .append(",")
                            .append(identity.getCredential().getValue())
                            .toString()
                    );
                    tokens.add(token);
                }
            }
            return tokens;
        }

        @Override
        public void resolve(IdentityChain chain, DehydratedExecutionContext ctx) throws InvalidCredentialsException {
            for (final IdentityToken token : ctx.getIdentityTokens()) {
                Principal principal = new PrincipalImpl(token.getName());
                Scanner scanner = new Scanner(token.getValue()).useDelimiter(",");
                Credential credential = new CredentialImpl(scanner.next(), scanner.next());
                Identity identity = new IdentityImpl(principal, credential);
                chain.addIdentity(identity);
            }
        }
    }

	private static HessianObjectIOFactory ioFactory;

    private static DehydratedExecutionContextResolutionImpl contextResolution = new DehydratedExecutionContextResolutionImpl();
    private SocketRMIMarshaller cut = new SocketRMIMarshaller(new CommonNameCertInfoExtractor(), contextResolution);

    private static ArgumentCaptor<SocketContextResolutionParams> socketContextResolutionParamsArgumentCaptor = ArgumentCaptor.forClass(SocketContextResolutionParams.class);

    @BeforeClass
    public static void staticBefore() {
        DehydratedExecutionContextResolver<SocketContextResolutionParams, Void> additionalParamsMock = mock(DehydratedExecutionContextResolver.class);
        when(additionalParamsMock.supportedComponents()).thenReturn(new DehydratedExecutionContextComponent[] { DehydratedExecutionContextComponent.ReceivedTime });
        final ArgumentCaptor<DehydratedExecutionContextBuilder> builderArgumentCaptor = ArgumentCaptor.forClass(DehydratedExecutionContextBuilder.class);
        final ArgumentCaptor<Void> voidArgumentCaptor = ArgumentCaptor.forClass(Void.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                List<DehydratedExecutionContextBuilder> allBuilders = builderArgumentCaptor.getAllValues();
                allBuilders.get(allBuilders.size()-1).setReceivedTime(new Date());
                return null;
            }
        }).when(additionalParamsMock).resolve(socketContextResolutionParamsArgumentCaptor.capture(),voidArgumentCaptor.capture(),builderArgumentCaptor.capture());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Set<DehydratedExecutionContextComponent> components = (Set<DehydratedExecutionContextComponent>) invocation.getArguments()[0];
                if (!components.contains(DehydratedExecutionContextComponent.ReceivedTime))
                {
                    throw new RuntimeException("I'm not handling what i want to!");
                }
                return null;
            }
        }).when(additionalParamsMock).resolving(anySet());
        DehydratedExecutionContextResolverFactory additionalParamsFactory = mock(DehydratedExecutionContextResolverFactory.class);
        when(additionalParamsFactory.resolvers(Protocol.SOCKET)).thenReturn(new DehydratedExecutionContextResolver[] { additionalParamsMock });

        contextResolution.registerFactory(new DefaultExecutionContextResolverFactory(mock(GeoIPLocator.class), mock(RequestTimeResolver.class)));
        contextResolution.registerFactory(additionalParamsFactory);
        contextResolution.init(false);
    }

    @Before
    public void before() {
    	ioFactory = new HessianObjectIOFactory(false);
    }


    @Test
    public void testGeoLocationMarshalling() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        //Only the resolved IP is relevant here
        SimpleGeoLocationDetails toMarshall = new SimpleGeoLocationDetails("1.2.3.4");

        cut.writeGeoLocation(toMarshall, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        byte[] theBytes = outputStream.toByteArray();
        assertNotNull(theBytes);

        GeoLocationParameters unMarshalled = cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(theBytes), protocolVersion), "10.20.30.40", protocolVersion);
        assertEquals("10.20.30.40", unMarshalled.getRemoteAddress());
        assertEquals(RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4," + RemoteAddressUtils.localAddressList), unMarshalled.getAddressList());
        assertNull(unMarshalled.getInferredCountry());
    }


    @Test
    public void testGeoLocationMarshallingWithInferredCountry() throws IOException {
        String inferredCountry = null;
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            inferredCountry = "JM";
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        //Only the resolved IP is relevant here
        SimpleGeoLocationDetails toMarshall = new SimpleGeoLocationDetails(Collections.singletonList("1.2.3.4"), inferredCountry);

        cut.writeGeoLocation(toMarshall, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        byte[] theBytes = outputStream.toByteArray();
        assertNotNull(theBytes);

        GeoLocationParameters unMarshalled = cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(theBytes), protocolVersion), "10.20.30.40", protocolVersion);
        assertEquals("10.20.30.40", unMarshalled.getRemoteAddress());
        assertEquals(RemoteAddressUtils.parse("1.2.3.4", "1.2.3.4," + RemoteAddressUtils.localAddressList), unMarshalled.getAddressList());
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            assertEquals("JM",unMarshalled.getInferredCountry());
        }
        else {
            assertNull(unMarshalled.getInferredCountry());
        }
    }

    @Test
    public void testGeoLocationMarshallingWithNullResolvedIP() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        //Only the resolved IP is relevant here
        SimpleGeoLocationDetails toMarshall = new SimpleGeoLocationDetails((List)null);

        cut.writeGeoLocation(toMarshall, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        byte[] theBytes = outputStream.toByteArray();
        assertNotNull(theBytes);

        GeoLocationParameters unMarshalled = cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(theBytes), protocolVersion), "10.20.30.40", protocolVersion);
        assertEquals("10.20.30.40", unMarshalled.getRemoteAddress());
        assertEquals(RemoteAddressUtils.parse(RemoteAddressUtils.localAddressList, RemoteAddressUtils.localAddressList), unMarshalled.getAddressList());
        assertNull(unMarshalled.getInferredCountry());
    }

    @Test
    public void testGeoLocationMarshallingWithEmptyResolvedIP() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        //Only the resolved IP is relevant here
        List empty = Collections.emptyList();
        SimpleGeoLocationDetails toMarshall = new SimpleGeoLocationDetails(empty);

        cut.writeGeoLocation(toMarshall, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        byte[] theBytes = outputStream.toByteArray();
        assertNotNull(theBytes);

        GeoLocationParameters unMarshalled = cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(theBytes), protocolVersion), "10.20.30.40", protocolVersion);
        assertEquals("10.20.30.40", unMarshalled.getRemoteAddress());
        assertEquals(RemoteAddressUtils.parse(RemoteAddressUtils.localAddressList, RemoteAddressUtils.localAddressList), unMarshalled.getAddressList());
        assertNull(unMarshalled.getInferredCountry());
    }

    @Test
    public void testGeoLocationMarshallingWithMultipleResolvedIP() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        //Only the resolved IP is relevant here
        List empty = Collections.emptyList();
        SimpleGeoLocationDetails toMarshall = new SimpleGeoLocationDetails(Arrays.asList("127.0.0.1","128.0.0.1"));

        cut.writeGeoLocation(toMarshall, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        byte[] theBytes = outputStream.toByteArray();
        assertNotNull(theBytes);

        final GeoLocationDetails gld = mock(GeoLocationDetails.class);
        GeoLocationParameters unMarshalled = cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(theBytes), protocolVersion), "10.20.30.40", protocolVersion);
        assertEquals("10.20.30.40", unMarshalled.getRemoteAddress());
        assertEquals(RemoteAddressUtils.parse("127.0.0.1", "127.0.0.1,128.0.0.1," + RemoteAddressUtils.localAddressList), unMarshalled.getAddressList());
        assertNull(unMarshalled.getInferredCountry());
    }



    @Test(expected=IOException.class)
    public void testGeoUnmarshallingWithEmptyInputStream() throws IOException {
        //When we attempt to unmarshall an empty stream, an EOFException is thrown
        cut.readGeoLocation(ioFactory.newCougarObjectInput(new ByteArrayInputStream(new byte[] {}), protocolVersion), "10.20.30.40", protocolVersion);
    }

    @Test
    public void testRequestMarshalling() throws IOException {
        final ExecutionContext ctx = new SimpleExecutionContext();
        final OperationKey key = new OperationKey(new ServiceVersion("v1.0"), "UnitTestService", "myUnitTestMethod");
        final Parameter[] params = new Parameter[] {
            new Parameter("param1", new ParameterType(String.class, null), true)
        };
        final TimeConstraints timeConstraints = DefaultTimeConstraints.NO_CONSTRAINTS;

        final Object[] args = new Object[] {
            "hello"
        };

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);

        InvocationRequest request = new InvocationRequest() {
            @Override
            public Object[] getArgs() {
                return args;
            }

            @Override
            public ExecutionContext getExecutionContext() {
                return ctx;
            }

            @Override
            public OperationKey getOperationKey() {
                return key;
            }

            @Override
            public Parameter[] getParameters() {
                return params;
            }

            @Override
            public TimeConstraints getTimeConstraints() {
                return timeConstraints;
            }
        };

        Map<String,String> additionalParams = new HashMap<>();
        additionalParams.put("paramA","valueA");

        cut.writeInvocationRequest(request, cougarObjectOutput, identityResolver, additionalParams, protocolVersion);
        cougarObjectOutput.flush();
        cougarObjectOutput.close();
        //String resolvedAddresses = RemoteAddressUtils.externaliseWithLocalAddresses(ctx.getLocation().getResolvedAddresses());
        //when(geoIpLocator.getGeoLocation("10.20.30.40", RemoteAddressUtils.parse("10.20.30.40", resolvedAddresses), null)).thenReturn(ctx.getLocation());
        CougarObjectInput in = ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion);
        DehydratedExecutionContext actualContext = cut.readExecutionContext(in, "10.20.30.40", new X509Certificate[0], 0, protocolVersion);
        OperationKey actualKey = cut.readOperationKey(in);
        Object[] actualArgs = cut.readArgs(params, in);

        assertNotNull(actualContext);
        assertEquals(key, actualKey);
        assertArrayEquals(args, actualArgs);
        List<SocketContextResolutionParams> allSocketParams = socketContextResolutionParamsArgumentCaptor.getAllValues();
        Map<String,String> reslvedAdditionalParams = allSocketParams.get(allSocketParams.size()-1).getAdditionalData();
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_COMPOUND_REQUEST_UUID) {
            assertEquals(1,reslvedAdditionalParams.size());
            assertEquals("valueA",reslvedAdditionalParams.get("paramA"));
        }
        else {
            assertEquals(0,reslvedAdditionalParams.size());
        }
    }

    @Test
    public void testResponseMarshallingWithReturnedValue() throws IOException {
        ParameterType resultType = new ParameterType(String.class, null);
        InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(new String("result!"), null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertTrue(actualResponse.isSuccess());
        assertEquals(response.getResult(), actualResponse.getResult());
        assertNull(actualResponse.getException());
    }

    @Test
    /**
     * test the serialisation and deserialisation of response object where the response is created using a delegate
     */
    public void testResponseMarshallingWithReturnedDelegate() throws Exception {
    	ParameterType resultType = new ParameterType(Foo.class,null);
    	Foo foo = new Foo(new FooDelegateImpl("foo"));
    	InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(foo,null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertTrue(actualResponse.isSuccess());
        Object responseObject = removeDelegates(response.getResult());
        assertEquals(responseObject, actualResponse.getResult());
        assertNull(actualResponse.getException());

    }

    @Test
    /**
     * test the serialisation and deserialisation of response object where the response graph has cycles
     */
    public void testResponseMarshallingWithCycles() throws Exception {
    	ParameterType resultType = new ParameterType(Cycle1.class,null);
    	Cycle1 cycle1 = new Cycle1();
    	Cycle2 cycle2 = new Cycle2();
    	cycle1.setCycle2(cycle2);
    	cycle2.setCycle1(cycle1);
    	InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(cycle1,null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertTrue(actualResponse.isSuccess());
        assertTrue(actualResponse.getResult() == ((Cycle1)actualResponse.getResult()).getCycle2().getCycle1());
        assertNull(actualResponse.getException());

    }



    @Test
    /**
     * test the serialisation and deserialisation of response object where the response is created using a delegate
     * and include test handling for null
     */
    public void testResponseMarshallingWithReturnedDelegateWithNull() throws Exception {
    	ParameterType resultType = new ParameterType(Foo.class,null);
    	Foo foo = new Foo(new FooDelegateImpl("foo"));
    	foo.setBar(null);
    	InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(foo,null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertTrue(actualResponse.isSuccess());
        Object responseObject = removeDelegates(response.getResult());
        assertEquals(responseObject, actualResponse.getResult());
        assertNull(actualResponse.getException());

    }

	@Test
    public void testResponseMarshallingWithVoidReturn() throws IOException {
        InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(null, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        cougarObjectOutput.close();
        InvocationResponse actualResponse = cut.readInvocationResponse(null,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertTrue(actualResponse.isSuccess());
        assertNull(actualResponse.getResult());
        assertNull(actualResponse.getException());
    }

    @Test
    public void testResponseMarshallingWithException1() throws IOException {
        ParameterType resultType = new ParameterType(String.class, null);
        InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(null, new CougarFrameworkException("All went bad"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertFalse(actualResponse.isSuccess());
        assertNotNull(actualResponse.getException());
        assertEquals(response.getException().getFault(), ((CougarException) actualResponse.getException().getCause()).getFault());
    }

    @Test
    public void testResponseMarshallingWithException2() throws IOException {
        final String SPURIOUS_EXCEPTION = "Spurious exception";
        ParameterType resultType = new ParameterType(String.class, null);
        InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(null, new CougarFrameworkException("All went bad", new RandomException(SPURIOUS_EXCEPTION)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertFalse(actualResponse.isSuccess());
        RandomException cause = (RandomException)actualResponse.getException().getCause();
        Assert.assertEquals(SPURIOUS_EXCEPTION, cause.getMessage());
        assertEquals(response.getException().getFault(), actualResponse.getException().getFault());
    }

    @Test
    public void testResponseMarshallingWithException3() throws IOException {

        SimpleApplicationException ex = new SimpleApplicationException(ResponseCode.InternalError, "bang");

        ParameterType resultType = new ParameterType(String.class, null);
        InvocationResponse response = new SocketRMIMarshaller.InvocationResponseImpl(null, new CougarFrameworkException("All went bad", ex));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cut.writeInvocationResponse(response, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        InvocationResponse actualResponse = cut.readInvocationResponse(resultType,
                ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion));

        assertFalse(actualResponse.isSuccess());
        CougarApplicationException actualCause = (CougarApplicationException)actualResponse.getException().getCause();

        Assert.assertEquals(ex.getResponseCode(), actualCause.getResponseCode());

        List<String> collatedExpectedFaultList = new ArrayList<String>();
        for (String[] group : ex.getApplicationFaultMessages()) {
            collatedExpectedFaultList.addAll(Arrays.asList(group));
        }

        List<String> collatedActualFaultList = new ArrayList<String>();
        for (String[] group : actualCause.getApplicationFaultMessages()) {
            collatedActualFaultList.addAll(Arrays.asList(group));
        }
        assertTrue(ImmutableMultiset.copyOf(collatedExpectedFaultList).equals(ImmutableMultiset.copyOf(collatedActualFaultList)));
        Assert.assertEquals(ex.getApplicationFaultNamespace(), actualCause.getApplicationFaultNamespace());
    }

    private IdentityResolver identityResolver = new IdentityResolverImpl();

    @Test
    public void testIdentityChainMarshallsNoIdentities() throws IOException {
        IdentityChain expected = new IdentityChainImpl(new ArrayList<Identity>());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cougarObjectOutput.writeString("127.0.0.1"); // address
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            cougarObjectOutput.writeString(null);
        }
        cut.writeIdentity(expected, cougarObjectOutput, identityResolver);
        cut.writeRequestUUID(new RequestUUIDImpl(), cougarObjectOutput, protocolVersion);
        cut.writeReceivedTime(new Date(), cougarObjectOutput);
        cougarObjectOutput.writeBoolean(false); // traceEnabled
        cut.writeRequestTime(cougarObjectOutput, protocolVersion);
        cut.writeAdditionalParams(null, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        DehydratedExecutionContext ctx = cut.readExecutionContext(ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion), "127.0.0.1", new X509Certificate[0], 0, protocolVersion);
        assertEquals(0, ctx.getIdentityTokens().size());
        assertNull(ctx.getIdentity());
    }

    @Test
    public void testIdentityChainMarshallsOneIdentity() throws IOException {
        final Identity joe = createIdentity("joeBloggs", "password", "fido123");
        IdentityChain expected = new IdentityChainImpl(new ArrayList<Identity>() {{ add(joe); }});
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cougarObjectOutput.writeString("127.0.0.1"); // address
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            cougarObjectOutput.writeString(null);
        }
        cut.writeIdentity(expected, cougarObjectOutput, identityResolver);
        cut.writeRequestUUID(new RequestUUIDImpl(), cougarObjectOutput, protocolVersion);
        cut.writeReceivedTime(new Date(), cougarObjectOutput);
        cougarObjectOutput.writeBoolean(false); // traceEnabled
        cut.writeRequestTime(cougarObjectOutput, protocolVersion);
        cut.writeAdditionalParams(null, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        DehydratedExecutionContext ctx = cut.readExecutionContext(ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion), "127.0.0.1", new X509Certificate[0], 0, protocolVersion);
        assertEquals(1, ctx.getIdentityTokens().size());
        assertNull(ctx.getIdentity());
    }

    @Test
    public void testIdentityChainMarshallsManyIdentities() throws IOException {
        final Identity joe = createIdentity("joeBloggs", "password", "fido123");
        final Identity sam = createIdentity("samSpade", "password", "topcat999");
        IdentityChain expected = new IdentityChainImpl(new ArrayList<Identity>() {{ add(joe); add(sam); }});
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CougarObjectOutput cougarObjectOutput = ioFactory.newCougarObjectOutput(outputStream, protocolVersion);
        cougarObjectOutput.writeString("127.0.0.1"); // address
        if (protocolVersion >= CougarProtocol.TRANSPORT_PROTOCOL_VERSION_START_TLS) {
            cougarObjectOutput.writeString(null);
        }
        cut.writeIdentity(expected, cougarObjectOutput, identityResolver);
        cut.writeRequestUUID(new RequestUUIDImpl(), cougarObjectOutput, protocolVersion);
        cut.writeReceivedTime(new Date(), cougarObjectOutput);
        cougarObjectOutput.writeBoolean(false); // traceEnabled
        cut.writeRequestTime(cougarObjectOutput, protocolVersion);
        cut.writeAdditionalParams(null, cougarObjectOutput, protocolVersion);
        cougarObjectOutput.flush();
        DehydratedExecutionContext ctx = cut.readExecutionContext(ioFactory.newCougarObjectInput(new ByteArrayInputStream(outputStream.toByteArray()), protocolVersion), "127.0.0.1", new X509Certificate[0], 0, protocolVersion);
        assertEquals(2, ctx.getIdentityTokens().size());
        assertNull(ctx.getIdentity());
    }

    @Test
    public void testWriteArgument() throws IOException{
    	Parameter[] parameters = new Parameter[2];
    	parameters[0] = new Parameter("string",new ParameterType(String.class,null),true);
    	parameters[1] = new Parameter("int", new ParameterType(Integer.class,null), false);
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	CougarObjectOutput cos = ioFactory.newCougarObjectOutput(os, protocolVersion);
    	cut.writeArgs(parameters, new Object[] {"abc", 1}, cos);
    	cos.flush();
    	Object[] args = cut.readArgs(parameters, ioFactory.newCougarObjectInput(new ByteArrayInputStream(os.toByteArray()), protocolVersion));
    	assertArrayEquals(new Object[]{"abc",1},args);
    }


    @Test
    public void testAdditionalInputArgs() throws IOException {
    	Parameter[] parameters = new Parameter[2];
    	parameters[0] = new Parameter("int", new ParameterType(Integer.class,null), false);
    	parameters[1] = new Parameter("string",new ParameterType(String.class,null),true);
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	CougarObjectOutput cos = ioFactory.newCougarObjectOutput(os, protocolVersion);
    	cut.writeArgs(parameters, new Object[] {1,"abc"}, cos);
    	cos.flush();
    	parameters = new Parameter[1];
    	parameters[0] = new Parameter("string",new ParameterType(String.class,null),true);
    	Object[] args = cut.readArgs(parameters, ioFactory.newCougarObjectInput(new ByteArrayInputStream(os.toByteArray()), protocolVersion));

    	assertArrayEquals(new Object[]{"abc"}, args);
    }

    @Test
    public void testArgsOutOfOrder() throws IOException {
    	Parameter[] parameters = new Parameter[2];
    	parameters[0] = new Parameter("int", new ParameterType(Integer.class,null), false);
    	parameters[1] = new Parameter("string",new ParameterType(String.class,null),true);
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	CougarObjectOutput cos = ioFactory.newCougarObjectOutput(os, protocolVersion);
    	cut.writeArgs(parameters, new Object[] {1,"abc"}, cos);
    	cos.flush();
    	parameters = new Parameter[2];
    	parameters[0] = new Parameter("string",new ParameterType(String.class,null),true);
    	parameters[1] = new Parameter("int", new ParameterType(Integer.class,null), false);
    	Object[] args = cut.readArgs(parameters, ioFactory.newCougarObjectInput(new ByteArrayInputStream(os.toByteArray()), protocolVersion));

    	assertArrayEquals(new Object[]{"abc",1}, args);
    }


    private Identity createIdentity(String principalName, String credentialName, String credentialValue) {
        Principal principal = new PrincipalImpl(principalName);
        Credential credential = new CredentialImpl(credentialName, credentialValue);
        return new IdentityImpl(principal, credential);
    }

    /**
     * Equals methods in generated idd classes don't handle delegates
     * @param result
     * @return
     * @throws Exception
     */
    private Object removeDelegates(Object result) throws Exception {
    	if (! (result instanceof Transcribable))  {
    		return result;
    	}
		Transcribable transcribable = (Transcribable) result;
		final Object[] objects = new Object[transcribable.getParameters().length];
		final int[] index = new int[1];
		transcribable.transcribe(new TranscriptionOutput(){

			@Override
			public void writeObject(Object obj, Parameter param, boolean client) throws Exception {
				if (obj == null) {
					objects[index[0]++] = null;
				}
				else if (param.getParameterType().getType() == Type.OBJECT) {
					objects[index[0]++] = removeDelegates(obj);
				}
				else if (param.getParameterType().getType() == Type.LIST) {
					if (obj.getClass().isArray()) {
						objects[index[0]] = Array.newInstance(param.getParameterType().getComponentTypes()[0].getImplementationClass(), Array.getLength(obj));
						for (int i=0,limit=Array.getLength(obj); i<limit;i++) {
							Array.set(objects[index[0]], i, removeDelegates(Array.get(obj, i)));
						}
						index[0]++;
					}
					else {
						List list = (List) obj;
						objects[index[0]] = new ArrayList();
						for (Object o : list) {
							((List)objects[index[0]]).add(removeDelegates(o));
						}
						index[0]++;
					}
				}
				else if (param.getParameterType().getType() == Type.SET) {
					Set set = (Set) obj;
					objects[index[0]] = new HashSet();
					for (Object o : set) {
						((Set)objects[index[0]]).add(removeDelegates(o));
					}
					index[0]++;
				}
				else if (param.getParameterType().getType() == Type.MAP) {
					Map<Object,Object> map = (Map)obj;
					objects[index[0]] = new HashMap();
					for (Entry entry : map.entrySet()) {
						((Map)objects[index[0]]).put(removeDelegates(entry.getKey()), removeDelegates(entry.getValue()));
					}
					index[0]++;
				}
				else {
					objects[index[0]++] = obj;
				}

			}}, TranscribableParams.getAll(), false);

		Transcribable newObject = (Transcribable) result.getClass().newInstance();
		index[0] = 0;
		newObject.transcribe(new TranscriptionInput() {

			@Override
			public <T> T readObject(Parameter param, boolean client) throws Exception {
				return (T) objects[index[0]++];

			}}, TranscribableParams.getAll(), false);

		return newObject;
	}


}
