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

package com.betfair.cougar.core.impl;

import com.betfair.cougar.util.NetworkAddress;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AddressClassifier {

    private List<NetworkAddress> privateNetworks = new ArrayList<>();
    private List<NetworkAddress> localLoopbacks = new ArrayList<>();

    public void setPrivateAddressRanges(String s) {
        String[] ranges = s.split(",");
        for (String r : ranges) {
            privateNetworks.add(NetworkAddress.parseBlock(r));
        }
    }

    public void setLocalLoopbackRanges(String s) {
        String[] ranges = s.split(",");
        for (String r : ranges) {
            localLoopbacks.add(NetworkAddress.parseBlock(r));
        }
    }

    public boolean isPrivateAddress(String ipAddress) {
        return isIn(ipAddress, privateNetworks);
    }

    public boolean isLocalAddress(String ipAddress) {
        return isIn(ipAddress, localLoopbacks);
    }

    private boolean isIn(String ipAddress, List<NetworkAddress> ranges) {
        for (NetworkAddress na : ranges) {
            if (na.isAddressInNetwork(ipAddress)) {
                return true;
            }
        }
        return false;
    }
}
