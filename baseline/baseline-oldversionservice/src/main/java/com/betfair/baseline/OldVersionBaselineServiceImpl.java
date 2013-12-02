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

package com.betfair.baseline;

import com.betfair.baseline.v1.BaselineService;
import com.betfair.baseline.v1.exception.SimpleException;
import com.betfair.baseline.v1.to.SimpleResponse;
import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.tornjak.monitor.*;

import java.util.Collections;
import java.util.Set;

/**
 */
public class OldVersionBaselineServiceImpl implements BaselineService {
    @Override
    public SimpleResponse testSimpleGet(RequestContext ctx, String message, TimeConstraints timeConstraints) throws SimpleException {
        SimpleResponse simpleResponse = new SimpleResponse();

        simpleResponse.setMessage(message + " emitted by version 1.0.0 of Baseline");
        return simpleResponse;
    }

    @Override
    public void init(ContainerContext cc) {
    }

}
