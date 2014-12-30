/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.cougar.core.api.client;

import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.exception.CougarClientException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Exception Factory class, which is used to re-create exceptions post marshalling
 * Will be extended by a generated Interface implementation.  The extending class will
 * register one (or more) ExceptionInstantiators, which are stored in a map backed by
 * the BSIDL defined Prefix attribute of the exception type stanza.
 * todo: this should move into client package
 */
public abstract class AbstractExceptionFactory implements ExceptionFactory {

    protected static interface ExceptionInstantiator {
        enum ExceptionType { Checked, Unchecked };
        Exception createException(ResponseCode responseCode, String prefix, String reason, Map<String, String> exceptionParams);
    }

    private Map<String, ExceptionInstantiator> exceptionInstantiatorMap = new HashMap<String, ExceptionInstantiator>();


    public void registerExceptionInstantiator(String exceptionPrefix, ExceptionInstantiator instantiator) {
        exceptionInstantiatorMap.put(exceptionPrefix, instantiator);
    }

    public AbstractExceptionFactory() {
        //Registers the CougarException exception instantiator...
        registerExceptionInstantiator(ServerFaultCode.COUGAR_EXCEPTION_PREFIX, new ExceptionInstantiator() {
            @Override
            public Exception createException(ResponseCode responseCode, String prefix, String reason, Map<String,String> exceptionParams) {
                ServerFaultCode sfc = ServerFaultCode.getByDetailCode(prefix);
                return new CougarClientException(sfc, reason);
            }
        });
    }

    @Override
    public Exception parseException(ResponseCode responseCode, String prefix, String reason, List<String[]> params) {
        String exceptionPrefix = extractExceptionPrefix(prefix);
        if (!exceptionInstantiatorMap.containsKey(exceptionPrefix)) {
            throw new IllegalArgumentException("Unknown exception prefix [" + prefix + "] encountered!");
        }
        return exceptionInstantiatorMap.get(exceptionPrefix).createException(responseCode, prefix, reason, parseExceptionParamList(params));
    }

    protected Map<String, String> parseExceptionParamList(List<String[]> exceptionParams) {
        Map<String, String> exceptionParamMap = new HashMap<String, String>();
        if (exceptionParams != null && exceptionParams.size() > 0) {
            for (String[] tuple : exceptionParams) {
                exceptionParamMap.put(tuple[0], tuple[1]);
            }
        }
        return exceptionParamMap;
    }

    public String extractExceptionPrefix(String prefix) {
        String[] prefixLits = prefix.split("-");
        if (prefixLits.length != 2) {
            throw new IllegalArgumentException("Invalid exception prefix [" + prefix + "] encountered");
        }
        return prefixLits[0];
    }
}
