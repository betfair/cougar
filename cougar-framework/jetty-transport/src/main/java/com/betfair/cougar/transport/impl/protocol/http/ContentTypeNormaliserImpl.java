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

import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.mediatype.MediaTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.HeaderUtils;
import com.betfair.cougar.util.MessageConstants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ContentTypeNormaliserImpl implements ContentTypeNormaliser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeNormaliser.class);
    public static final String DEFAULT_ENCODING = "utf-8";
    private Map<String, MediaType> validContentTypes = new ConcurrentHashMap<String, MediaType>();
    private List<MediaType> allContentTypes = Collections.synchronizedList(new ArrayList<MediaType>());
    private Set<String> validEncodings = Collections.synchronizedSet(new HashSet<String>());
    private String defaultResponseFormat;

    @Override
    public void addValidContentTypes(final Set<String> vct, MediaType normalisedContentType) {
        for (String ct : vct) {
            if (this.validContentTypes.containsKey(ct)) {
                // this content type is already registered. Ensure it's mapped to the same preferred type
                MediaType mType = this.validContentTypes.get(ct);
                if (!mType.equals(normalisedContentType)) {
                    throw new IllegalArgumentException("Content type " + ct + " is already registered with normalised type " + mType +
                            ", cannot re-register to " + normalisedContentType);
                }
            } else {
                this.validContentTypes.put(ct, normalisedContentType);
            }

        }
        allContentTypes.addAll(MediaTypeUtils.getMediaTypes(new ArrayList<String>(vct).toArray(new String[vct.size()])));
    }

    @Override
    public void addValidEncodings(final Set<String> validEncodings) {
        this.validEncodings.addAll(validEncodings);
    }

    @Override
    public MediaType getNormalisedResponseMediaType(HttpServletRequest request) {
        // Negotiate Response format
        MediaType responseMediaType;
        String responseFormat = getResponseFormat(request);
        try {
            List<MediaType> acceptMT = MediaTypeUtils.parseMediaTypes(responseFormat);

            responseMediaType = MediaTypeUtils.getResponseMediaType(allContentTypes, acceptMT);
            if (responseMediaType == null) {
                throw new CougarValidationException(ServerFaultCode.AcceptTypeNotValid, "Could not agree a response media type");
            } else if (responseMediaType.isWildcardType() || responseMediaType.isWildcardSubtype()) {
                throw new CougarServiceException(ServerFaultCode.ResponseContentTypeNotValid,
                        "Service configuration error - response media type must not be a wildcard - " + responseMediaType);
            }

        } catch (IllegalArgumentException e) {
            throw new CougarValidationException(ServerFaultCode.MediaTypeParseFailure, "Unable to parse supplied media types (" + responseFormat + ")",e);
        }
        return responseMediaType;
    }

    private String getResponseFormat(HttpServletRequest request) {
        String paramFormat = request.getParameter(MessageConstants.FORMAT_PARAMETER);
        if (paramFormat == null || paramFormat.length() == 0) {
            paramFormat = HeaderUtils.getAccept(request);
            if (paramFormat == null || paramFormat.length() == 0) {
                // Need to default to something
                paramFormat = defaultResponseFormat;
            }
        } else if (paramFormat.equalsIgnoreCase("xml")) {
            paramFormat = MediaType.APPLICATION_XML;
        } else if (paramFormat.equalsIgnoreCase("json")) {
            paramFormat = MediaType.APPLICATION_JSON;
        } else if (paramFormat.equalsIgnoreCase("bin")) {
            paramFormat = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            throw new CougarValidationException(ServerFaultCode.MediaTypeParseFailure, "Invalid alt param for media type - " + paramFormat);
        }
        return paramFormat;
    }

    @Override
    public MediaType getNormalisedRequestMediaType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (request.getMethod().equals("POST")) {
            if (contentType == null) {
                throw new CougarValidationException(ServerFaultCode.ContentTypeNotValid, "Input content type was not specified for deserialisable response");
            }
            MediaType requestMT;
            try {
                requestMT = MediaType.valueOf(contentType);
            } catch (Exception e) {
                throw new CougarValidationException(ServerFaultCode.MediaTypeParseFailure, "Input content type cannot be parsed: " + contentType,e);
            }
            if (requestMT.isWildcardType() || requestMT.isWildcardSubtype()) {
                throw new CougarValidationException(ServerFaultCode.InvalidInputMediaType, "Input content type may not be wildcard: " + requestMT);
            }
            if (!MediaTypeUtils.isValid(allContentTypes, requestMT)) {
                throw new CougarValidationException(ServerFaultCode.ContentTypeNotValid, "Input content type is not valid: " + requestMT);
            }
            String candidateContentType = requestMT.getType() + "/" + requestMT.getSubtype();
            MediaType normalizedMediaType = validContentTypes.get(candidateContentType);
            if (normalizedMediaType == null) {
                throw new CougarValidationException(ServerFaultCode.FrameworkError, "Input content type " + contentType + " failed to find a normalized type using key " + candidateContentType);
            }
            return normalizedMediaType;
        }
        return null;
    }

    public void setDefaultResponseFormat(String defaultResponseFormat) {
        this.defaultResponseFormat = defaultResponseFormat;
    }

    @Override
    public String getNormalisedEncoding(HttpServletRequest request) {
        return getEncoding(request.getContentType());
    }

    private static String CHARSET = "charset=";

    //EG: Content-Type: text/html; charset=utf-8
    private String getEncoding(final String contentType) {
        if (contentType == null || !contentType.contains(CHARSET)) {
            return DEFAULT_ENCODING;
        }
        String encoding = null;
        try {
            encoding = contentType.substring(contentType.indexOf(CHARSET) + CHARSET.length());
            encoding = encoding.toLowerCase().replaceAll("\"", "");
        } catch (Exception e) {
            //Extraction from the string failed.
        }
        if (!validEncodings.contains(encoding)) {
            LOGGER.warn("Invalid Encoding '{}' - using default - '{}'", encoding, DEFAULT_ENCODING);
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

}