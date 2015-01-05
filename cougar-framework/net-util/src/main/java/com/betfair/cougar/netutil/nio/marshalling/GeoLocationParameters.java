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

import java.util.List;

/**
 *
 */
public class GeoLocationParameters {
    private final String remoteAddress;
    private final List<String> addressList;
    private final String inferredCountry;

    public GeoLocationParameters(String remoteAddress, List<String> addressList, String inferredCountry) {
        this.remoteAddress = remoteAddress;
        this.addressList = addressList;
        this.inferredCountry = inferredCountry;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public String getInferredCountry() {
        return inferredCountry;
    }
}
