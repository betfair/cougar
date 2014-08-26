/*
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

package com.betfair.cougar.transport.impl.protocol.http.rescript;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.ev.SimpleOperationDefinition;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.marshalling.impl.databinding.DataBindingManager;
import com.betfair.cougar.marshalling.impl.databinding.DataBindingMap;
import com.betfair.cougar.marshalling.impl.databinding.json.JSONBindingFactory;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptBody;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptResponse;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class RescriptOperationBindingTest extends TestCase {

    private static final String theString = "I hope its time to go home";

    protected static enum TestEnum { FOO, UNRECOGNIZED_VALUE }

    private HttpServletRequest mockedRequest;

    private RescriptOperationBinding operationSimpleGetBinding;
    private RescriptOperationBinding operationBodyParamBinding;
    private RescriptOperationBinding operationOptionalBodyEnumParamBinding;

    private final OperationKey testSimpleGetKey = new OperationKey(new ServiceVersion("v1.0"), "unitTestService", "testSimpleGet");
    private final OperationKey testBodyParamKey = new OperationKey(new ServiceVersion("v1.0"), "unitTestService", "testBodyParam");
    private final OperationKey testOptionalBodyEnumKey = new OperationKey(new ServiceVersion("v1.0"), "unitTestService", "testBodyEnumParam");

    private final OperationDefinition testSimpleGetDef = new SimpleOperationDefinition(
            testSimpleGetKey,
            new Parameter [] { new Parameter("stringParam", new ParameterType(String.class, null),true),
                               new Parameter("floatParam", new ParameterType(Float.class, null),true),
                               new Parameter("longParam", new ParameterType(Long.class, null), true)},
            new ParameterType(String.class, null));

    private final OperationDefinition testBodyParamDef = new SimpleOperationDefinition(
            testBodyParamKey,
            new Parameter [] { new Parameter("stringBodyParam", new ParameterType(Long.class, null),true)},
            new ParameterType(String.class, null));

    private final OperationDefinition testOptionalBodyEnumDef = new SimpleOperationDefinition(
            testOptionalBodyEnumKey,
            new Parameter [] { new Parameter("enumBodyParam", new ParameterType(TestEnum.class, null),false)},
            new ParameterType(String.class, null));

    private static String enumBodyParamValue;

    @Before
    public void init() {
        Set<String> contentTypeSet = new HashSet<String>();
        contentTypeSet.add("application/json");
        contentTypeSet.add("text/json");

        DataBindingMap dataBindingMap = new DataBindingMap();
        dataBindingMap.setContentTypes(contentTypeSet);
        dataBindingMap.setPreferredContentType("application/json");
        dataBindingMap.setFactory(new JSONBindingFactory());

        DataBindingManager dbm = DataBindingManager.getInstance();
        dbm.addBindingMap(dataBindingMap);

        mockedRequest = mock(HttpServletRequest.class);
        when(mockedRequest.getHeaderNames()).thenReturn(enumerator(new ArrayList<String>().iterator()));

        List<RescriptParamBindingDescriptor> paramBindingDescriptors = new ArrayList<RescriptParamBindingDescriptor>();
        paramBindingDescriptors.add(new RescriptParamBindingDescriptor("stringParam", RescriptParamBindingDescriptor.ParamSource.QUERY));
        paramBindingDescriptors.add(new RescriptParamBindingDescriptor("longParam",   RescriptParamBindingDescriptor.ParamSource.QUERY));
        paramBindingDescriptors.add(new RescriptParamBindingDescriptor("floatParam",  RescriptParamBindingDescriptor.ParamSource.QUERY));

        RescriptOperationBindingDescriptor simpleGetOperationDescriptor = new RescriptOperationBindingDescriptor(testSimpleGetKey, "/simpleGet", "GET", paramBindingDescriptors, RescriptResponse.class);
        operationSimpleGetBinding = new RescriptOperationBinding(simpleGetOperationDescriptor, testSimpleGetDef, true);

        List<RescriptParamBindingDescriptor> simpleGetParamBindings = new ArrayList<RescriptParamBindingDescriptor>();
        simpleGetParamBindings.add(new RescriptParamBindingDescriptor("stringBodyParam", RescriptParamBindingDescriptor.ParamSource.BODY));

        RescriptOperationBindingDescriptor bodyParamOperationDescriptor = new RescriptOperationBindingDescriptor(testBodyParamKey, "/simpleGet", "GET", simpleGetParamBindings, RescriptResponse.class, NoddyRescriptBody.class);
        operationBodyParamBinding = new RescriptOperationBinding(bodyParamOperationDescriptor, testBodyParamDef, true);

        List<RescriptParamBindingDescriptor> optionalEnumBodyParams = new ArrayList<RescriptParamBindingDescriptor>();
        optionalEnumBodyParams.add(new RescriptParamBindingDescriptor("enumBodyParam", RescriptParamBindingDescriptor.ParamSource.BODY));

        RescriptOperationBindingDescriptor optionalBodyEnumOperationDescriptor = new RescriptOperationBindingDescriptor(testOptionalBodyEnumKey, "/simpleGet", "POST", optionalEnumBodyParams, RescriptResponse.class, NoddyRescriptBody.class);
        operationOptionalBodyEnumParamBinding = new RescriptOperationBinding(optionalBodyEnumOperationDescriptor, testOptionalBodyEnumDef, true);
    }

    public static <T> Enumeration<T> enumerator(final Iterator<T> it) {
        return new Enumeration<T>() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public T nextElement() {
                return it.next();
            }
        };
    }

    @Test
    public void testResolveArgsWithValidQueryStringParams() {
        //Start with valid query string parameters
        when(mockedRequest.getParameter("stringParam")).thenReturn("Hello world");
        when(mockedRequest.getParameter("floatParam")).thenReturn("3.21");
        when(mockedRequest.getParameter("longParam")).thenReturn("93828123111");
        when(mockedRequest.getParameter("stringBodyParam")).thenReturn("is it home time yet?");

        Object[] resolvedArguments = operationSimpleGetBinding.resolveArgs(mockedRequest, null, MediaType.APPLICATION_JSON_TYPE, "utf-8");
        assertTrue(resolvedArguments[0] instanceof String);
        int i=0;
        assertEquals("Hello world", (String)resolvedArguments[i++]);
        assertEquals(3.21f, (Float)resolvedArguments[i++]);
        assertEquals(93828123111L, ((Long)resolvedArguments[i++]).longValue());
    }

    @Test
    public void testResolveArgsWithInvalidQueryStringParams() {
        //Start with valid query string parameters
        when(mockedRequest.getParameter("stringParam")).thenReturn("Hello world");
        when(mockedRequest.getParameter("floatParam")).thenReturn("thisaintnevergonnaparse");
        when(mockedRequest.getParameter("longParam")).thenReturn("93828123111");

        try {
            Object[] resolvedArguments = operationSimpleGetBinding.resolveArgs(mockedRequest, null, MediaType.APPLICATION_JSON_TYPE, "utf-8");
            fail("A validation exception should have occurred due to an invalid float argument");
        } catch (CougarMarshallingException expected) {
        }
    }

    @Test(expected=CougarMarshallingException.class)
    public void testResolveBodyWithGetMethod() {
        when(mockedRequest.getMethod()).thenReturn("GET");
        operationBodyParamBinding.resolveArgs(mockedRequest, new ByteArrayInputStream("".getBytes()), null, "utf-8");
    }

    @Test
    public void testResolveBody() {
        InputStream is = new ByteArrayInputStream(("{\"message\":\"" + theString + "\"}").getBytes());
        when(mockedRequest.getMethod()).thenReturn("POST");

        Object[] resolvedArgs = operationBodyParamBinding.resolveArgs(mockedRequest, is, MediaType.APPLICATION_JSON_TYPE, "utf-8");

        assertEquals(resolvedArgs[0], theString);
    }

    @Test
    public void testResolveNonNullOptionalBodyEnum() {
        enumBodyParamValue = "FOO";
        InputStream is = new ByteArrayInputStream(("{\"message\":\"" + "dummy" + "\"}").getBytes());
        when(mockedRequest.getMethod()).thenReturn("POST");
        Object[] resolvedArgs = operationOptionalBodyEnumParamBinding.resolveArgs(mockedRequest, is, MediaType.APPLICATION_JSON_TYPE, "utf-8");
        assertEquals(TestEnum.FOO, resolvedArgs[0]);
    }

    @Test
    public void testResolveNullOptionalBodyEnum() {
        enumBodyParamValue = null;
        InputStream is = new ByteArrayInputStream(("{\"message\":\"" + "dummy" + "\"}").getBytes());
        when(mockedRequest.getMethod()).thenReturn("POST");
        Object[] resolvedArgs = operationOptionalBodyEnumParamBinding.resolveArgs(mockedRequest, is, MediaType.APPLICATION_JSON_TYPE, "utf-8");
        assertEquals(null, resolvedArgs[0]);
    }

    @Test(expected = CougarMarshallingException.class)
    public void testResolveUnrecognizedOptionalBodyEnumWithHardFailure() {
        Boolean originalHardFailureValue = EnumUtils.getHardFailureForThisThread();
        EnumUtils.setHardFailureForThisThread(true);
        try {
            enumBodyParamValue = "GARBAGE";
            InputStream is = new ByteArrayInputStream(("{\"message\":\"" + "dummy" + "\"}").getBytes());
            when(mockedRequest.getMethod()).thenReturn("POST");
            operationOptionalBodyEnumParamBinding.resolveArgs(mockedRequest, is, MediaType.APPLICATION_JSON_TYPE, "utf-8");
        }
        finally {
            if (originalHardFailureValue == null) {
                originalHardFailureValue = true;
            }
            EnumUtils.setHardFailureForThisThread(originalHardFailureValue);
        }
    }

    @Test
    public void testResolveUnrecognizedOptionalBodyEnumWithoutHardFailure() {
        Boolean originalHardFailureValue = EnumUtils.getHardFailureForThisThread();
        EnumUtils.setHardFailureForThisThread(false);
        try {
            enumBodyParamValue = "GARBAGE";
            InputStream is = new ByteArrayInputStream(("{\"message\":\"" + "dummy" + "\"}").getBytes());
            when(mockedRequest.getMethod()).thenReturn("POST");
            Object[] resolvedArgs = operationOptionalBodyEnumParamBinding.resolveArgs(mockedRequest, is, MediaType.APPLICATION_JSON_TYPE, "utf-8");
            assertEquals(TestEnum.UNRECOGNIZED_VALUE, resolvedArgs[0]);
        }
        finally {
            if (originalHardFailureValue == null) {
                originalHardFailureValue = true;
            }
            EnumUtils.setHardFailureForThisThread(originalHardFailureValue);
        }
    }

    public static class NoddyRescriptBody implements RescriptBody {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public Object getValue(String name) {
            if (name.equals("enumBodyParam")) {
                return enumBodyParamValue;
            }
            return theString;
        }
    }

}
