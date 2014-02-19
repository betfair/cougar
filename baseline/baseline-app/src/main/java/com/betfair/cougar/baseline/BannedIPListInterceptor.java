/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.baseline;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.ExecutionRequirement;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Proof of concept interceptor which will reject a request based on the resolvedIPAddress
 * if the address is in our banned list.
 */
public class BannedIPListInterceptor implements ExecutionPreProcessor {
    private List<String> banList = new ArrayList<String>();

    public BannedIPListInterceptor(String list) {
        this.banList.addAll(Arrays.asList(list.split(",")));
    }

    @Override
    public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
        if (key.getType() == OperationKey.Type.Request) {
            GeoLocationDetails geoDetails = ctx.getLocation();
            if (geoDetails == null) {
                return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                        new CougarServiceException(ServerFaultCode.SecurityException, "Geo location details were not provided"));
            } else if (banList.contains(geoDetails.getResolvedAddresses().get(0))) {
                return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION,
                        new CougarServiceException(ServerFaultCode.SecurityException, "The IP Address [" + geoDetails.getRemoteAddr() + "] is not permitted to access to this service"));
            }
        }
        return new InterceptorResult(InterceptorState.CONTINUE);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public ExecutionRequirement getExecutionRequirement() {
        return ExecutionRequirement.EXACTLY_ONCE;
    }
}
