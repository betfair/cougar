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

package com.betfair.cougar.netutil.nio.marshalling;

import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;
import com.betfair.cougar.transport.impl.SimpleRequestTimeResolver;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class DefaultSocketTimeResolver extends SimpleRequestTimeResolver<Long> {

    public DefaultSocketTimeResolver() {
        super(false);
    }

    public DefaultSocketTimeResolver(boolean clientTimeSynchronizedWithServer) {
        super(clientTimeSynchronizedWithServer);
    }

    @Override
    protected Date readRequestTime(Long input) {
        return new Date(input);
    }
}
