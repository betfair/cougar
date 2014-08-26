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
import java.util.Set;

/**
 * Responsible for getting the media type of requests and responses.
 */
public interface ContentTypeNormaliser {

	/**
	 * Get the MediaType of the content of the HttpServletRequest.
	 * This is determined from the Content-Type header for POST requests.
	 * For POST requests with no Content-Type header, or an invalid value,
	 * a CougarValidationException is thrown
	 * For GET requests this method will return null.
     * @throws CougarValidationException
	 */
	public MediaType getNormalisedRequestMediaType(HttpServletRequest request);

	/**
	 * Get the MediaType for the content of the HttpServletResponse for the request.
	 * MediaType is determined first of all by a MessageConstants.FORMAT_PARAMETER parameter,
	 * or if that is not present, by the request Accept-Types header. If no accept types
	 * have been set on the request then a default value is returned. If an invalid accept type
	 * is set a CougarValidationException is thrown.
     * @throws CougarValidationException
     * @see MessageConstants.FORMAT_PARAMETER
	 */
	public MediaType getNormalisedResponseMediaType(HttpServletRequest request);

	/**
	 * Get the Encoding for the HttpServletRequest and corresponding HttpServletResponse.
	 */
	public String getNormalisedEncoding(HttpServletRequest request);

    /**
     * Adds a set of valid content types and associated media type to this content type normaliser.
     */
    public void addValidContentTypes(final Set<String> validContentType, MediaType normalisedContentType);

    /**
     * Adds a set of valid encodings to this content type normaliser.
     */
    public void addValidEncodings(final Set<String> validEncodings);;
}
