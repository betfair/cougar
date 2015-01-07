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

package com.betfair.cougar.core.impl;

import com.betfair.cougar.CougarVersion;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.OperationKey;

/**
 *
 */
public class CougarInternalOperations {
    private static ServiceVersion COUGAR_VERSION = new ServiceVersion("v"+CougarVersion.getMajorMinorVersion());
    public static String COUGAR_INTERNAL_INTERFACE_NAME = "_CougarInternal";
    public static OperationKey RESOLVE_IDENTITIES = new OperationKey(COUGAR_VERSION, COUGAR_INTERNAL_INTERFACE_NAME,"resolveIdentities", OperationKey.Type.Request);
    public static OperationKey BATCH_CALL = new OperationKey(COUGAR_VERSION, COUGAR_INTERNAL_INTERFACE_NAME,"batchCall", OperationKey.Type.Request);
    public static String COUGAR_IN_PROCESS_NAMESPACE = "_IN_PROCESS";
}
