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

package com.betfair.testing.utils.cougar;

import com.betfair.testing.utils.cougar.enums.CougarMessageContentTypeEnum;
import com.betfair.testing.utils.cougar.manager.LogTailer;


/**
 * New interface for writing integration tests via.
 */
public interface CougarTestingInvoker {


    CougarTestingInvoker setService(String serviceName);

    CougarTestingInvoker setService(String serviceName, String path);

    CougarTestingInvoker setVersion(String version);

    CougarTestingInvoker setOperation(String operation);

    CougarTestingInvoker setOperation(String operation, String operationPath);

    CougarTestingInvoker addHeaderParam(String key, String value);

    CougarTestingInvoker addQueryParam(String key, String value);

    CougarTestingInvoker makeMatrixRescriptCalls(CougarMessageContentTypeEnum... mediaTypes);

    CougarTestingInvoker setExpectedResponse(CougarMessageContentTypeEnum mediaType, String response);

    CougarTestingInvoker setExpectedHttpResponse(int code, String text);

    CougarTestingInvoker verify();

    LogTailer.LogLine[] getRequestLogEntries();

    CougarTestingInvoker setSoapBody(String body);

    CougarTestingInvoker makeSoapCall();

    CougarTestingInvoker addJsonRpcMethodCall(String id, String method, String body);
    CougarTestingInvoker addJsonRpcExpectedResponse(String body);

    CougarTestingInvoker makeJsonRpcCalls();
}
