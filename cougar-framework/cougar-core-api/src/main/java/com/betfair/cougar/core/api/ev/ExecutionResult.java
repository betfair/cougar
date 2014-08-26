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

package com.betfair.cougar.core.api.ev;

import com.betfair.cougar.api.fault.CougarApplicationException;
import com.betfair.cougar.core.api.exception.*;
import com.betfair.cougar.core.api.exception.ServerFaultCode;

/**
 * Used to wrap the result of an execution event
 */
public class ExecutionResult {
    private CougarException fault;
    private Subscription subscription;

    public enum ResultType {
        Success,
        Fault,
        Subscription
    }

    private Object result;
    private ResultType resultType;

    //Use for void
    public ExecutionResult() {
        this.resultType = ResultType.Success;
    }

    public ExecutionResult(Object result) {
        // todo: perhaps this shouldn't be done here, and instead should go in the service wrapper?
        //       then we wouldn't have anything to do with service in the core ev..
        if (result instanceof CougarApplicationException) {
            this.resultType = ResultType.Fault;
            fault = new CougarServiceException(ServerFaultCode.ServiceCheckedException, "", (CougarApplicationException)result);
        } else if (result instanceof CougarException) {
            this.resultType = ResultType.Fault;
            fault = (CougarException)result;
        } else if (result instanceof Subscription) {
            this.resultType = ResultType.Subscription;
            subscription = (Subscription)result;
        } else {
            this.result = result;
            this.resultType = ResultType.Success;
        }
    }

    public Object getResult() {
        return result;
    }

    public CougarException getFault() {
        return fault;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public boolean isFault() {
        return ResultType.Fault == resultType;
    }
}
