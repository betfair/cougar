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
