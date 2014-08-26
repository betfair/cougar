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

package com.betfair.cougar.util.geolocation;

import com.betfair.cougar.util.NetworkAddress;

import java.net.InetAddress;
import java.util.ArrayList;


/**
 * A list of network addresses that are suspected to return incorrect geo ip details
 */
public class SuspectNetworkList {

	private ArrayList<NetworkAddress> suspectNetworks = new ArrayList<NetworkAddress>();
    private String strVal;

	/**
	 * @param networkAddresses comma separated list of network address each as ip4Address/netmask, where both ip4Address & netmask
	 * are in dotted quad notation
	 */
	public void setSuspectNetworks(String networkAddresses) {

		if (networkAddresses != null) {
			String[] networks = networkAddresses.split(",");
			for (int i=0; i<networks.length; i++) {
				suspectNetworks.add(NetworkAddress.parse(networks[i]));
			}
		}
        strVal = networkAddresses;
	}

    public String getSuspectNetworks() {
        return strVal;
    }

    /**
	 * test if the given ip address (in dotted quad notation) is in the range of one of the suspect networks
	 * @param address
	 * @return
	 */
	public boolean isSuspect(String address) {
		for (NetworkAddress networkAddress : suspectNetworks) {
			if (networkAddress.isAddressInNetwork(address)) {
				return true;
			}
		}
		return false;
	}

}
