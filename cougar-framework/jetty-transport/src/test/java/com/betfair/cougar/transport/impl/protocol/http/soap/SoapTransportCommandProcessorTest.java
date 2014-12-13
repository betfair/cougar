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

package com.betfair.cougar.transport.impl.protocol.http.soap;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.security.*;
import com.betfair.cougar.core.api.OperationBindingDescriptor;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.marshalling.impl.databinding.xml.JdkEmbeddedXercesSchemaValidationFailureParser;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.TransportCommand;
import com.betfair.cougar.transport.api.TransportCommand.CommandStatus;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapIdentityTokenResolver;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapServiceBindingDescriptor;
import com.betfair.cougar.transport.impl.protocol.http.AbstractHttpCommandProcessorTest;
import com.betfair.cougar.transport.impl.protocol.http.ContentTypeNormaliser;
import junit.framework.AssertionFailedError;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SoapTransportCommandProcessorTest extends AbstractHttpCommandProcessorTest<OMElement> {
    public static final String       AZ            = "Azerbaijan";

	private static final String soapEnvStart = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
	private static final String soapEnvFinish = "</soapenv:Envelope>";
	private static final String soapHeaderStart = "<soapenv:Header>";
	private static final String soapHeaderFinish = "</soapenv:Header>";
	private static final String soapBodyStart = "<soapenv:Body>";
	private static final String soapBodyFinish = "</soapenv:Body>";
	private static final String soapFaultStart = "<soapenv:Fault>";
	private static final String soapFaultFinish = "</soapenv:Fault>";
    private static final String nullSoapBody = "<soapenv:Body/>";


	private static final String firstOpIn = "<FirstTestOpRequest xmlns=\"http://www.betfair.com/soaptest\"><FirstOpFirstParam>hello</FirstOpFirstParam></FirstTestOpRequest>";
	private static final String firstOpInDuplicate = "<FirstTestOpRequest xmlns=\"http://www.betfair.com/soaptest\"><FirstOpFirstParam>hello</FirstOpFirstParam><FirstOpFirstParam>goodbye</FirstOpFirstParam></FirstTestOpRequest>";
	private static final String firstOpOut = "<FirstTestOpResponse xmlns=\"http://www.betfair.com/soaptest\"><response>goodbye</response></FirstTestOpResponse>";
	private static final String firstOpError = "<faultcode>soapenv:Client</faultcode><faultstring>TestError-123</faultstring>";
	private static final String firstOpErrorDetail = "<detail><sptst:TestApplicationException xmlns:sptst=\"http://www.betfair.com/soaptest\"><sptst:TheError>The Error Detail</sptst:TheError></sptst:TestApplicationException></detail>";
	private static final String mapOpIn = "<MapOpRequest xmlns=\"http://www.betfair.com/soaptest\"><MapOpFirstParam><entry key=\"1\"><Double>1.0</Double></entry><entry key=\"2\"><Double>2.2</Double></entry><entry key=\"3\"/></MapOpFirstParam></MapOpRequest>";
	private static final String mapOpOut = "<MapOpResponse xmlns=\"http://www.betfair.com/soaptest\"><response><entry key=\"1\"><Double>1.0</Double></entry><entry key=\"2\"/><entry key=\"3\"><Double>3.3</Double></entry></response></MapOpResponse>";
	private static final String listOpIn = "<ListOpRequest xmlns=\"http://www.betfair.com/soaptest\"><ListOpFirstParam><Date>248556211-09-30T12:12:53.297+01:00</Date><Date>248556211-09-30T12:12:53.297Z</Date></ListOpFirstParam></ListOpRequest>";
	private static final String listOpOut = "<ListOpResponse xmlns=\"http://www.betfair.com/soaptest\"><response><Date>248556211-09-30T11:12:53.297Z</Date><Date>248556211-09-30T12:12:53.297Z</Date></response></ListOpResponse>";
	private static final String invalidOpIn = "<InvalidOpRequest xmlns=\"http://www.betfair.com/soaptest\"><InvalidOpFirstParam>INVALID</InvalidOpFirstParam></InvalidOpRequest>";
	private static final String invalidOpError = "<faultcode>soapenv:Client</faultcode><faultstring>DSC-0044</faultstring><detail />";
    private static final String invalidCredentialsError = "<faultcode>soapenv:Client</faultcode><faultstring>DSC-0015</faultstring><detail />";
    private static final String voidResponseOpIn = "<VoidResponseRequest xmlns=\"http://www.betfair.com/soaptest\"><VoidReturnOpFirstParam>TEST1</VoidReturnOpFirstParam></VoidResponseRequest>";
    private static final String duplicateRequestParamIn = "<VoidResponseRequest xmlns=\"http://www.betfair.com/soaptest\"><VoidReturnOpFirstParam>TEST1</VoidReturnOpFirstParam><VoidReturnOpFirstParam>TEST2</VoidReturnOpFirstParam></VoidResponseRequest>";
    private static final String invalidVoidResponseOpIn = "<VoidResponseRequest xmlns=\"http://www.betfair.com/soaptest\"><VoidReturnOpFirstParam>INVALID</VoidReturnOpFirstParam></VoidResponseRequest>";
    private static final String invalidVoidResponseOpOut = "<faultcode>soapenv:Client</faultcode><faultstring>DSC-0044</faultstring><detail />";
    private static final String externalEntityIn = "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe1 SYSTEM \"file:///etc/shadow\" >]> ";
    private static final String externalEntityInWithBody = externalEntityIn+"<VoidResponseRequest xmlns=\"http://www.betfair.com/soaptest\"><VoidReturnOpFirstParam>&foo;</VoidReturnOpFirstParam>";
    private static final String invalidInputFault = "<faultcode>soapenv:Client</faultcode><faultstring>DSC-0044</faultstring><detail />";

	private static final OperationBindingDescriptor[] operationBindings = new OperationBindingDescriptor[] {
		new SoapOperationBindingDescriptor(firstOpKey, "FirstTestOpRequest", "FirstTestOpResponse"),
		new SoapOperationBindingDescriptor(mapOpKey, "MapOpRequest", "MapOpResponse"),
		new SoapOperationBindingDescriptor(listOpKey, "ListOpRequest", "ListOpResponse"),
		new SoapOperationBindingDescriptor(invalidOpKey, "InvalidOpRequest", "InvalidOpResponse"),
        new SoapOperationBindingDescriptor(voidReturnOpKey, "VoidResponseRequest", null)};

	private static final SoapServiceBindingDescriptor serviceBinding = new SoapServiceBindingDescriptor() {
        private ServiceVersion serviceVersion = new ServiceVersion("v1.92");

		@Override
		public String getNamespacePrefix() {
			return "sptst";
		}
		@Override
		public String getNamespaceURI() {
			return "http://www.betfair.com/soaptest";
		}
		@Override
		public String getServiceContextPath() {
			return "/myservice/";
		}
		@Override
		public Protocol getServiceProtocol() {
			return Protocol.SOAP;
		}
		@Override
		public OperationBindingDescriptor[] getOperationBindings() {
			return operationBindings;
		}

        @Override
        public ServiceVersion getServiceVersion() {
            return serviceVersion;
        }

        @Override
        public String getServiceName() {
            return "TestService";
        }

        @Override
        public String getSchemaPath() {
            return "xsd/foo.xsd";
        }
    };

	private SoapTransportCommandProcessor soapCommandProcessor;
    private SoapIdentityTokenResolver identityTokenResolver;
    private HttpCommand command;

    @Before
	public void init() throws Exception {
		super.init();
		soapCommandProcessor = new SoapTransportCommandProcessor(contextResolution, "X-RequestTimeout", new JdkEmbeddedXercesSchemaValidationFailureParser());
		init(soapCommandProcessor);
		ContentTypeNormaliser ctn = mock(ContentTypeNormaliser.class);
		when(ctn.getNormalisedResponseMediaType(any(HttpServletRequest.class))).thenReturn(MediaType.APPLICATION_XML_TYPE);
		soapCommandProcessor.setContentTypeNormaliser(ctn);
        identityTokenResolver = mock(SoapIdentityTokenResolver.class);
        when(identityTokenResolver.resolve(any(SOAPHeader.class), any(X509Certificate[].class))).thenReturn(new ArrayList<IdentityToken>());
        soapCommandProcessor.setValidatorRegistry(validatorRegistry);

		soapCommandProcessor.bind(serviceBinding);
		soapCommandProcessor.onCougarStart();
        command=super.createCommand(identityTokenResolver, Protocol.SOAP);

	}

    @Override
    protected OMElement isCredentialContainer() {
        return any(OMElement.class);
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.SOAP;
    }

    /**
     * Basic test with string parameters
     * @throws Exception
     */
    @Test
    public void testProcess() throws Exception {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(1, ev.getInvokedCount());

        // Assert that we resolved the expected arguments
        Object[] args = ev.getArgs();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("hello", args[0]);

        // Assert that the expected result is sent
        assertNotNull(ev.getObserver());
        ev.getObserver().onResult(new ExecutionResult("goodbye"));
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, firstOpOut, null, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
    }

    /**
     * Basic test with string parameters
     * @throws Exception
     */
    @Test
    public void testProcessRetrieveCredentials() throws Exception {

        // Set up the input
        when(request.getInputStream()).thenReturn(new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(1, ev.getInvokedCount());

        // Assert that we resolved the expected arguments
        Object[] args = ev.getArgs();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("hello", args[0]);

        // Assert that the expected result is sent
        assertNotNull(ev.getObserver());
        ev.getObserver().onResult(new ExecutionResult("goodbye"));
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, firstOpOut, null, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
    }

    /**
     * Test for a SOAP RPC call with a void return
     * @throws Exception
     */
    @Test
    public void testVoid() throws Exception {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, voidResponseOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(1, ev.getInvokedCount());

        // Assert that we resolved the expected arguments
        Object[] args = ev.getArgs();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals(TestEnum.TEST1, args[0]);

        // Assert that the expected result is sent
        assertNotNull(ev.getObserver());
        ev.getObserver().onResult(new ExecutionResult());
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(soapEnvStart+nullSoapBody+soapEnvFinish, testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(voidReturnOpKey);
    }

    /**
     * Tests a void RPC call that throws an exception - we should still get a correctly formed
     * SOAP error body back
     * @throws Exception
     */
    @Test
    public void testProcess_voidWithException() throws Exception {

        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, invalidVoidResponseOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidVoidResponseOpOut, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(null);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testDuplicateRequestParam() throws Exception {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, duplicateRequestParamIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(1, ev.getInvokedCount());

        // Assert that we resolved the expected arguments
        Object[] args = ev.getArgs();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals(TestEnum.TEST1, args[0]);

        // Assert that the expected result is sent
        assertNotNull(ev.getObserver());
        ev.getObserver().onResult(new ExecutionResult());
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(soapEnvStart+nullSoapBody+soapEnvFinish, testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(voidReturnOpKey);
    }



	/**
	 * Tests exceptions
	 * @throws Exception
	 */
	@Test
	public void testProcess_OnException() throws Exception {

		// Set up the input
		when(request.getInputStream()).thenReturn(
				new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");
		faultMessages = new ArrayList<String[]>();
		faultMessages.add(new String[] {"TheError", "The Error Detail"});

		// Resolve the input command
		soapCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());
		assertNotNull(ev.getObserver());

		// Assert that the expected exception is sent
		ev.getObserver().onResult(new ExecutionResult(new CougarServiceException(
					ServerFaultCode.ServiceCheckedException, "Error in App",
					new TestApplicationException(ResponseCode.Forbidden, "TestError-123",faultMessages))));
		assertEquals(CommandStatus.Complete, command.getStatus());
		assertSoapyEquals(buildSoapMessage(null, null, firstOpError, firstOpErrorDetail), testOut.getOutput());
		verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
	}

    /**
     * Tests the detailed fault reporting for the exception
     * @throws Exception
     */
    @Test
    public void testDetailedFaultReporting() throws Exception {
        FaultController.getInstance().setDetailedFaults(true);
        try {
            // Set up the input
            when(request.getInputStream()).thenReturn(
                    new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
            when(request.getScheme()).thenReturn("http");
            faultMessages = new ArrayList<String[]>();
            faultMessages.add(new String[] {"TheError", "The Error Detail"});

            // Resolve the input command
            soapCommandProcessor.process(command);

            // Assert that the expected exception is sent
            TestApplicationException tae = new TestApplicationException(ResponseCode.Forbidden, "TestError-123", faultMessages);
            ev.getObserver().onResult(new ExecutionResult(new CougarServiceException(
                        ServerFaultCode.ServiceCheckedException, "Error in App",
                        tae)));
            assertEquals(CommandStatus.Complete, command.getStatus());

            String message = testOut.getOutput();
            int traceStartPos = message.indexOf("<trace>");
            int traceEndPos = message.indexOf("</trace>");

            int messageStartPos = message.indexOf("<message>");
            int messageEndPos = message.indexOf("</message>");

            assertTrue(traceStartPos != -1);
            assertTrue(traceEndPos != -1);
            assertTrue(messageStartPos != -1);
            assertTrue(messageEndPos != -1);

            assertTrue(traceEndPos > (traceStartPos+7));
            assertTrue(messageEndPos > (messageStartPos+9));

            verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

            verifyTracerCalls(firstOpKey);
        }
        finally {
            FaultController.getInstance().setDetailedFaults(false);
        }
    }


	/**
	 * Tests a client error response is sent for invalid input
	 * @throws Exception
	 */
	@Test
	public void testProcess_InvalidInput() throws Exception {

		when(request.getInputStream()).thenReturn(
				new TestServletInputStream(buildSoapMessage(null, invalidOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		soapCommandProcessor.process(command);
		assertEquals(CommandStatus.Complete, command.getStatus());
		assertSoapyEquals(buildSoapMessage(null, null, invalidOpError, null), testOut.getOutput());
		verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(null);
	}

	/**
	 * Tests Map, Integer and Double parameters in and out
	 * @throws Exception
	 */
	@Test
	public void testProcess_MapIntDouble() throws Exception {

		// Set up the input
		when(request.getInputStream()).thenReturn(
				new TestServletInputStream(buildSoapMessage(null, mapOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		soapCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());

		// Assert that we resolved the expected arguments
		Object[] args = ev.getArgs();
		assertNotNull(args);
		assertEquals(1, args.length);
		Map<Integer, Double> requestMap = (Map<Integer, Double>)args[0];
        assertEquals(3, requestMap.size());

		Double requestEntry = requestMap.get(1);
		assertNotNull(requestEntry);
		assertEquals(1.0, requestEntry, 0.01);
		requestEntry = requestMap.get(2);
		assertNotNull(requestEntry);
		assertEquals(2.2, requestEntry, 0.01);
        requestEntry = requestMap.get(3);
        assertNull(requestEntry);

		// Assert that the expected result is sent
		assertNotNull(ev.getObserver());
		Map<Integer, Double> response = new HashMap<Integer, Double>();
		response.put(1, 1.0);
		response.put(3, 3.3);
        response.put(2, null);
		ev.getObserver().onResult(new ExecutionResult(response));
		assertEquals(CommandStatus.Complete, command.getStatus());
		assertSoapyEquals(buildSoapMessage(null, mapOpOut, null, null), testOut.getOutput());
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(mapOpKey);
	}

	/**
	 * Tests List and Date parameters in and out
	 * @throws Exception
	 */
	@Test
	public void testProcess_ListDate() throws Exception {
		// Set up the input
		when(request.getInputStream()).thenReturn(
				new TestServletInputStream(buildSoapMessage(null, listOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

		// Resolve the input command
		soapCommandProcessor.process(command);
		assertEquals(1, ev.getInvokedCount());

		DateTimeFormatter xmlFormat = ISODateTimeFormat.dateTimeParser();

		// Assert that we resolved the expected arguments
		Object[] args = ev.getArgs();
		assertNotNull(args);
		assertEquals(1, args.length);
		List<Date> listArg = (List<Date>)args[0];
		assertEquals(2, listArg.size());
		assertEquals(xmlFormat.parseDateTime("248556211-09-30T12:12:53.297+01:00").toDate(), listArg.get(0));
		assertEquals(xmlFormat.parseDateTime("248556211-09-30T12:12:53.297Z").toDate(), listArg.get(1));

		// Assert that the expected result is sent
		assertNotNull(ev.getObserver());
		List<Date> response = new ArrayList<Date>();
		response.add(xmlFormat.parseDateTime("248556211-09-30T12:12:53.297+01:00").toDate());
		response.add(xmlFormat.parseDateTime("248556211-09-30T12:12:53.297Z").toDate());

		ev.getObserver().onResult(new ExecutionResult(response));
		assertEquals(CommandStatus.Complete, command.getStatus());
		assertSoapyEquals(buildSoapMessage(null, listOpOut, null, null), testOut.getOutput());
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                                            any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(listOpKey);
	}


    /**
     * US53541
     * Test for attempted XML Entity Injection
     * @throws Exception
     */
    @Test
    public void xmlEntityInjection() throws Exception {

        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, externalEntityIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidInputFault, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        //verifyTracerCalls(); // todo: #81: put this back
    }
    /**
     * US53541
     * Test for attempted XML Entity Injection
     * @throws Exception
     */
    @Test
    public void xmlEntityInjectionWithBody() throws Exception {

        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, externalEntityInWithBody, null, null)));
        when(request.getScheme()).thenReturn("http");

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidInputFault, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        //verifyTracerCalls(); // todo: #81: put this back
    }

    @Test
    public void validationDisabled() throws Exception {
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpInDuplicate, null, null)));
        when(request.getScheme()).thenReturn("http");

        soapCommandProcessor.setSchemaValidationEnabled(false);
        soapCommandProcessor.process(command);
        ev.getObserver().onResult(new ExecutionResult("goodbye"));
        assertEquals(CommandStatus.Complete, command.getStatus());
        // note we don't check we got the right result
        // cougar is just non-deterministic if you have validation disabled
        assertSoapyEquals(buildSoapMessage(null, firstOpOut, null, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
    }

    @Test
    public void validationEnabledValidInput() throws Exception {
        // verify the schema is actually valid before we continue
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("xsd/foo.xsd")));

        soapCommandProcessor.setSchemaValidationEnabled(true);

        testProcess();
    }

    @Test
    public void validationEnabledInvalidInput() throws Exception {
        // verify the schema is actually valid before we continue
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("xsd/foo.xsd")));

        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpInDuplicate, null, null)));
        when(request.getScheme()).thenReturn("http");

        soapCommandProcessor.setSchemaValidationEnabled(true);
        soapCommandProcessor.process(command);

        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidInputFault, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), any(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        //verifyTracerCalls(); // todo: #81: put this back
    }


    /**
     * Tests a client error response is sent for invalid input
     * @throws Exception
     */
    @Test
    public void testProcess_IOException() throws Exception {
        when(request.getScheme()).thenReturn("http");
        ServletInputStream is = mock(ServletInputStream.class);
        when(request.getInputStream()).thenReturn(is);
        when(is.read()).thenThrow(new IOException("i/o error"));
        when(is.read((byte[])any())).thenThrow(new IOException("i/o error"));
        when(is.read((byte[])any(),anyInt(),anyInt())).thenThrow(new IOException("i/o error"));

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidOpError, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        //verifyTracerCalls(); // todo: #81: put this back
    }


    /**
     * Tests a client error response is sent for invalid input
     * @throws Exception
     */
    @Test
    public void testProcess_TooMuchData() throws Exception {
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");
        soapCommandProcessor.setMaxPostBodyLength(10L);

        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, null, invalidOpError, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        //verifyTracerCalls(); // todo: #81: put this back
    }

    @Test
    public void createCommandResolver_NoTimeout() throws IOException {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        CommandResolver<HttpCommand> cr = soapCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertNull(constraints.getExpiryTime());
    }

    @Test
    public void createCommandResolver_WithTimeout() throws IOException {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        when(request.getHeader("X-RequestTimeout")).thenReturn("10000");
        when(context.getRequestTime()).thenReturn(new Date());
        CommandResolver<HttpCommand> cr = soapCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertNotNull(constraints.getExpiryTime());
    }

    @Test
    public void createCommandResolver_WithTimeoutAndOldRequestTime() throws IOException {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new TestServletInputStream(buildSoapMessage(null, firstOpIn, null, null)));
        when(request.getScheme()).thenReturn("http");

        // resolve the command
        when(request.getHeader("X-RequestTimeout")).thenReturn("10000");
        when(context.getRequestTime()).thenReturn(new Date(System.currentTimeMillis() - 10001));
        CommandResolver<HttpCommand> cr = soapCommandProcessor.createCommandResolver(command, tracer);
        Iterable<ExecutionCommand> executionCommands = cr.resolveExecutionCommands();

        // check the output
        ExecutionCommand executionCommand = executionCommands.iterator().next();
        TimeConstraints constraints = executionCommand.getTimeConstraints();
        assertTrue(constraints.getExpiryTime() < System.currentTimeMillis());
    }

    /**
     * DE5417
     * @throws Exception
     */
    @Test
    public void testCredentialsNotFirstHeaderElement() throws Exception {

        // Set up the input
        when(request.getInputStream()).thenReturn(
                new AbstractHttpCommandProcessorTest.TestServletInputStream("<soapenv:Envelope xmlns:a=\"http://www.w3.org/2005/08/addressing\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sec=\"http://www.betfair.com/security/\" xmlns:usac=\"http://www.betfair.com/servicetypes/v1/USAccount/\">\n" +
                        "   <soapenv:Header>\n" +
                        "      <a:Action>retrieveAccount</a:Action>\n" +
                        "      <sec:Credentials>\n" +
                        "         <sec:X-Application>1001</sec:X-Application>\n" +
                        "         <sec:X-Authentication>vpIt3Zu38ZWppNBKYnbX7Uhno9zOwAydXvIwknGEdTc=</sec:X-Authentication>\n" +
                        "      </sec:Credentials>\n" +
                        "      \n" +
                        "\n" +
                        "      <a:MessageID>urn:uuid:2c4364e6-81e2-4ade-a507-620fd4d75b06</a:MessageID>\n" +
                        "\n" +
                        "       <a:ReplyTo><a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address></a:ReplyTo>\n" +
                        "\n" +
                        "        <VsDebuggerCausalityData xmlns=\"http://schemas.microsoft.com/vstudio/diagnostics/servicemodelsink\">uIDPozjxUVEEdSJJhzx3knx8ugoAAAAAi/9uTcBBVUWS2bFa1TmSWXHtuzkndd9Gj4MOMliiOqcACQAA</VsDebuggerCausalityData>\n" +
                        "\n" +
                        "        <a:To soapenv:mustUnderstand=\"1\">http://localhost/SomeService/v1.0</a:To>\n" +
                        "   </soapenv:Header>\n" +
                        "   <soapenv:Body>\n" +
                        firstOpIn +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>"));
        when(request.getScheme()).thenReturn("http");
        ArgumentMatcher<OMElement> credsMatcher = new ArgumentMatcher<OMElement>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof OMElement)) {
                    return false;
                }
                OMElement element = (OMElement) argument;
                List<IdentityToken> allTokensFound = new ArrayList<IdentityToken>();
                Iterator it = element.getChildElements();
                while (it.hasNext()) {
                    OMElement next = (OMElement) it.next();
                    allTokensFound.add(new IdentityToken(next.getLocalName(), next.getText().trim()));
                }

                if (!allTokensFound.remove(new IdentityToken("X-Application","1001"))) {
                    return false;
                }
                if (!allTokensFound.remove(new IdentityToken("X-Authentication","vpIt3Zu38ZWppNBKYnbX7Uhno9zOwAydXvIwknGEdTc="))) {
                    return false;
                }
                return allTokensFound.isEmpty();
            }
        };


        // Resolve the input command
        soapCommandProcessor.process(command);
        assertEquals(1, ev.getInvokedCount());


        // Assert that we resolved the expected arguments
        Object[] args = ev.getArgs();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("hello", args[0]);

        // Assert that the expected result is sent
        assertNotNull(ev.getObserver());
        ev.getObserver().onResult(new ExecutionResult("goodbye"));
        assertEquals(TransportCommand.CommandStatus.Complete, command.getStatus());
        assertSoapyEquals(buildSoapMessage(null, firstOpOut, null, null), testOut.getOutput());
        verify(response).setContentType(MediaType.TEXT_XML);
        verify(logger).logAccess(eq(command), isA(ExecutionContext.class), anyLong(), anyLong(),
                any(MediaType.class), any(MediaType.class), any(ResponseCode.class));

        verifyTracerCalls(firstOpKey);
    }

    private String buildSoapMessage(String header, String body, String fault, String faultDetail) {
		StringBuffer result = new StringBuffer(soapEnvStart);
		if (header != null) {
			result.append(soapHeaderStart + header + soapHeaderFinish);
		}
		if (body != null || fault != null) {
			result.append(soapBodyStart);
			if (body != null) {
				result.append(body);
			}
			if (fault != null) {
				result.append(soapFaultStart + fault);
				if (faultDetail != null) {
					result.append(faultDetail);
				}
				result.append(soapFaultFinish);
			}
			result.append(soapBodyFinish);
		}
		result.append(soapEnvFinish);
		return result.toString();
	}

	private void assertSoapyEquals(String expected, String actual) throws Exception{
		XMLStreamReader expectedParser = XMLInputFactory.newInstance()
			.createXMLStreamReader(
				new TestServletInputStream(expected));
		StAXSOAPModelBuilder expectedBuilder = new StAXSOAPModelBuilder(expectedParser);
		XMLStreamReader actualParser = XMLInputFactory.newInstance()
		.createXMLStreamReader(
			new TestServletInputStream(expected));
		StAXSOAPModelBuilder actualBuilder = new StAXSOAPModelBuilder(actualParser);
		assertHeaders(expectedBuilder.getSOAPEnvelope().getHeader(), actualBuilder.getSOAPEnvelope().getHeader());
		assertBody(expectedBuilder.getSOAPEnvelope().getBody(), actualBuilder.getSOAPEnvelope().getBody());
        try {
            xmlTestCase.assertXMLEqual(expected, actual);
        }
        catch (AssertionFailedError afe) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected:\n" + expected + "\n");
            sb.append("Actual:\n"+actual+"\n");
            sb.append(afe.getMessage());
            throw new AssertionFailedError(sb.toString());
        }
	}

	private void assertHeaders(SOAPHeader expectedHeader, SOAPHeader actualHeader) {
		if (expectedHeader == null) {
			assertNull(actualHeader);
		} else {
			assertElement(expectedHeader, actualHeader);
		}
	}

	private void assertBody(SOAPBody expectedBody, SOAPBody actualBody) {
		if (expectedBody == null) {
			assertNull(actualBody);
		} else {
			assertElement(expectedBody, actualBody);
		}
	}

	private void assertElement(OMElement expectedElement, OMElement actualElement) {
		Iterator expectedIt = expectedElement.getChildElements();
		Iterator actualIt = actualElement.getChildElements();
		assertEquals(expectedElement.getLocalName(), actualElement.getLocalName());
		if (expectedElement.getNamespace() == null) {
			assertNull(actualElement.getNamespace());
		} else {
			assertEquals(expectedElement.getNamespace().getNamespaceURI(), actualElement.getNamespace().getNamespaceURI());
		}
		assertEquals(expectedElement.getText(), actualElement.getText());
		assertEquals(expectedElement.getType(), actualElement.getType());
		assertAttributes(expectedElement, actualElement);
		while (expectedIt.hasNext()) {
			assertTrue(actualIt.hasNext());
			OMElement expectedChildElement = (OMElement)expectedIt.next();
			OMElement actualChildElement = (OMElement)actualIt.next();
			assertElement(expectedChildElement, actualChildElement);
		}
		assertFalse(actualIt.hasNext());
	}

	private void assertAttributes(OMElement expectedElement, OMElement actualElement) {
		Iterator expectedIt = expectedElement.getAllAttributes();
		Iterator actualIt = actualElement.getAllAttributes();
		while (expectedIt.hasNext()) {
			assertTrue(actualIt.hasNext());
			OMAttribute expectedAttribute = (OMAttribute)expectedIt.next();
			OMAttribute actualAttribute = (OMAttribute)actualIt.next();
			assertEquals(expectedAttribute.getAttributeType(), actualAttribute.getAttributeType());
			assertEquals(expectedAttribute.getAttributeValue(), actualAttribute.getAttributeValue());
			if (expectedAttribute.getNamespace() == null) {
				assertNull(actualAttribute.getNamespace());
			} else {
				assertEquals(expectedAttribute.getNamespace().getNamespaceURI(), actualAttribute.getNamespace().getNamespaceURI());
			}
		}
		assertFalse(actualIt.hasNext());
	}


}

