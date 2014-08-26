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

package com.betfair.cougar.transport.impl.protocol.http;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.util.MessageConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentTypeNormaliserImplTest {

	private ContentTypeNormaliserImpl ctn;
	private HttpServletRequest request;

	@Before
	public void init() {
		ctn = new ContentTypeNormaliserImpl();
        ctn.setDefaultResponseFormat(MediaType.APPLICATION_XML);
		Set<String> validContentTypes = new HashSet<String>();
		validContentTypes.add("application/xml");
		validContentTypes.add("application/xml+soap");
		ctn.addValidContentTypes(validContentTypes, MediaType.APPLICATION_XML_TYPE);
        validContentTypes = new HashSet<String>();
		validContentTypes.add("application/json");
		ctn.addValidContentTypes(validContentTypes, MediaType.APPLICATION_JSON_TYPE);
		validContentTypes = new HashSet<String>();
		validContentTypes.add("text/*");
		ctn.addValidContentTypes(validContentTypes, MediaType.WILDCARD_TYPE);
		Set<String> validEncodings = new HashSet<String>();
		validEncodings.add("utf-8");
		ctn.addValidEncodings(validEncodings);
		request = mock(HttpServletRequest.class);
	}

	@Test
	public void testGetNormalisedEncoding() {
		when(request.getCharacterEncoding()).thenReturn("utf-8");
		String encoding = ctn.getNormalisedEncoding(request);
		assertNotNull(encoding);
		assertEquals("utf-8", encoding);
	}

	@Test
	public void testGetNormalisedEncoding_Null() {
		String encoding = ctn.getNormalisedEncoding(request);
		assertNotNull(encoding);
		assertEquals("utf-8", encoding);
	}

	@Test
	public void testGetNormalisedEncoding_Invalid() {
		when(request.getCharacterEncoding()).thenReturn("utf-16");
		String encoding = ctn.getNormalisedEncoding(request);
		assertNotNull(encoding);
		assertEquals("utf-8", encoding);
	}

    	@Test
	public void testGetNormalisedEncoding_JustPlainWrong() {
		when(request.getContentType()).thenReturn("text/html; charset=");
		String encoding = ctn.getNormalisedEncoding(request);
		assertNotNull(encoding);
		assertEquals("utf-8", encoding);

        when(request.getContentType()).thenReturn("text/html; charset=MadeUp");
		encoding = ctn.getNormalisedEncoding(request);
		assertNotNull(encoding);
		assertEquals("utf-8", encoding);

	}

	@Test
	public void testGetNormalisedRequestMediaType() {
		when(request.getMethod()).thenReturn("POST");
		when(request.getContentType()).thenReturn("application/xml");
		MediaType mediaType = ctn.getNormalisedRequestMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedRequestMediaType_Invalid() {
		when(request.getMethod()).thenReturn("POST");
		when(request.getContentType()).thenReturn("application/text");
		try {
			ctn.getNormalisedRequestMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.ContentTypeNotValid, cve.getServerFaultCode());
			assertEquals(ResponseCode.UnsupportedMediaType, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedRequestMediaType_ParseFailure() {
		when(request.getMethod()).thenReturn("POST");
		when(request.getContentType()).thenReturn("soxml");
		try {
			ctn.getNormalisedRequestMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.MediaTypeParseFailure, cve.getServerFaultCode());
			assertEquals(ResponseCode.UnsupportedMediaType, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedRequestMediaType_Wildcard() {
		when(request.getMethod()).thenReturn("POST");
		when(request.getContentType()).thenReturn("application/*");
		try {
			ctn.getNormalisedRequestMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.InvalidInputMediaType, cve.getServerFaultCode());
			assertEquals(ResponseCode.UnsupportedMediaType, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedResponseMediaType_Default() {
		when(request.getParameter(MessageConstants.FORMAT_PARAMETER)).thenReturn(null);
		MediaType mediaType = ctn.getNormalisedResponseMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedResponseMediaType_DifferentDefault() {
        ctn.setDefaultResponseFormat("application/json");
		when(request.getParameter(MessageConstants.FORMAT_PARAMETER)).thenReturn(null);
		MediaType mediaType = ctn.getNormalisedResponseMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedResponseMediaType_Query() {
		when(request.getParameter(MessageConstants.FORMAT_PARAMETER)).thenReturn("xml");
		MediaType mediaType = ctn.getNormalisedResponseMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedResponseMediaType_QueryParseFailure() {
		when(request.getParameter(MessageConstants.FORMAT_PARAMETER)).thenReturn("soxml");
		try {
			ctn.getNormalisedResponseMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.MediaTypeParseFailure, cve.getServerFaultCode());
			assertEquals(ResponseCode.UnsupportedMediaType, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedResponseMediaType_Header() {
		when(request.getHeader(MessageConstants.ACCEPT_HEADER)).thenReturn("application/xml");
		MediaType mediaType = ctn.getNormalisedResponseMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedResponseMediaType_HeaderNotAcceptable() {
		when(request.getHeader(MessageConstants.ACCEPT_HEADER)).thenReturn("application/text");
		try {
			ctn.getNormalisedResponseMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.AcceptTypeNotValid, cve.getServerFaultCode());
			assertEquals(ResponseCode.MediaTypeNotAcceptable, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedResponseMediaType_HeaderParseFailure() {
		when(request.getHeader(MessageConstants.ACCEPT_HEADER)).thenReturn("soxml");
		try {
			ctn.getNormalisedResponseMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarValidationException cve) {
			assertEquals(ServerFaultCode.MediaTypeParseFailure, cve.getServerFaultCode());
			assertEquals(ResponseCode.UnsupportedMediaType, cve.getResponseCode());
		}
	}

	@Test
	public void testGetNormalisedResponseMediaType_WildcardSuccess() {
		when(request.getHeader(MessageConstants.ACCEPT_HEADER)).thenReturn("application/*");
		MediaType mediaType = ctn.getNormalisedResponseMediaType(request);
		assertNotNull(mediaType);
		assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
	}

	@Test
	public void testGetNormalisedResponseMediaType_WildcardResponseType() {
		when(request.getHeader(MessageConstants.ACCEPT_HEADER)).thenReturn("text/*");
		try {
			ctn.getNormalisedResponseMediaType(request);
			fail("CougarValidationException should have been thrown");
		} catch (CougarServiceException cve) {
			assertEquals(ServerFaultCode.ResponseContentTypeNotValid, cve.getServerFaultCode());
			assertEquals(ResponseCode.InternalError, cve.getResponseCode());
		}
	}
}
