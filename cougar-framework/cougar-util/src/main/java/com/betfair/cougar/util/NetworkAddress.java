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

package com.betfair.cougar.util;


/**
 * Represents an IP network (as opposed to an IP Address) and is able to identify if an IP address is a member of the network
 */
public class NetworkAddress {

	/**
	 * The address of this network
	 */
	private final byte[] network;

	/**
	 * mask to apply to an address to obtain the network identifier
	 */
	private final byte[] netmask;

	/**
	 * For private use only, clients should use the static factory method
	 */
	private NetworkAddress(byte[] network, byte[] netmask) {//NOSONAR
		this.network = network;
		this.netmask = netmask;
	}

    // for testing
    byte[] getNetwork() {
        return network;
    }

    // for testing
    byte[] getNetmask() {
        return netmask;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (byte b : network) {
            sb.append(sep);
            sb.append(toInt(b));
            sep = ".";
        }
        sep = "/";
        for (byte b : netmask) {
            sb.append(sep);
            sb.append(toInt(b));
            sep = ".";
        }
        return sb.toString();
    }

    /**
	 *
	 * @param address
	 * @return true if address is a member of this network
	 */
	public boolean isAddressInNetwork(String address) {
		boolean inNetwork = false;
		if (address != null) {
			byte[] addressBytes = parseDottedQuad(address);

			byte[] networkPart = new byte[4];
			for (int i=0; i<4; i++) {
				networkPart[i] = (byte) (addressBytes[i] & netmask[i]);
			}

			inNetwork = network[0] == networkPart[0] &&
						network[1] == networkPart[1] &&
						network[2] == networkPart[2] &&
						network[3] == networkPart[3] ;

		}
		return inNetwork;
	}

	/**
	 * parse a network address identifier, consisting of an ip4Address/netMask, where both ip4Address and netMask are
	 * presented in dotted quad notation.  e.g. 92.6.4.0/255.255.255.0
	 * @return
	 */
	public static NetworkAddress parse(String networkAddress) {
		NetworkAddress address = null;
		if (networkAddress != null) {
			String[] split = networkAddress.split("/");
			if (split.length == 2) {
				byte[] network = parseDottedQuad(split[0]);
				byte[] netmask = parseDottedQuad(split[1]);
				address = toNetworkAddress(network, netmask);
			}
			else {
				throw new IllegalArgumentException("Network address must be ip4Address/netMask");
			}
		}
		return address;
	}

    private static NetworkAddress toNetworkAddress(byte[] network, byte[] netmask) {

        byte[] maskedNetworkAddress = new byte[4];
        for (int i=0; i<4; i++) {
            maskedNetworkAddress[i] = (byte) (network[i] & netmask[i]);
        }
        return new NetworkAddress(maskedNetworkAddress, netmask);
    }

	/**
	 * parse a CIDR network block identifier, consisting of an ip4Address/prefixSize, where both ip4Address and netMask are
	 * presented in dotted quad notation.  e.g. 192.0.2.0/24
	 * @return
     * @see {http://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing#IPv4_CIDR_blocks}
	 */
	public static NetworkAddress parseBlock(String networkAddress) {
		NetworkAddress address = null;
		if (networkAddress != null) {
			String[] split = networkAddress.split("/");
			if (split.length == 2) {
				byte[] network = parseDottedQuad(split[0]);
                byte bits = Byte.parseByte(split[1]);

				address = toNetworkAddress(network, toNetworkMask(bits));


			}
			else {
				throw new IllegalArgumentException("Network address must be ip4Address/prefixSize");
			}
		}
		return address;
	}

    /**
     * @see {http://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing#IPv4_CIDR_blocks}
     */
    private static byte[] toNetworkMask(byte bits) {
        // lookup table is faster, and we only have 33 unique values to return
        switch (bits) {
            case  0: return new byte[] { toByte(  0), toByte(  0), toByte(  0), toByte(  0) };
            case  1: return new byte[] { toByte(128), toByte(  0), toByte(  0), toByte(  0) };
            case  2: return new byte[] { toByte(192), toByte(  0), toByte(  0), toByte(  0) };
            case  3: return new byte[] { toByte(224), toByte(  0), toByte(  0), toByte(  0) };
            case  4: return new byte[] { toByte(240), toByte(  0), toByte(  0), toByte(  0) };
            case  5: return new byte[] { toByte(248), toByte(  0), toByte(  0), toByte(  0) };
            case  6: return new byte[] { toByte(252), toByte(  0), toByte(  0), toByte(  0) };
            case  7: return new byte[] { toByte(254), toByte(  0), toByte(  0), toByte(  0) };
            case  8: return new byte[] { toByte(255), toByte(  0), toByte(  0), toByte(  0) };
            case  9: return new byte[] { toByte(255), toByte(128), toByte(  0), toByte(  0) };
            case 10: return new byte[] { toByte(255), toByte(192), toByte(  0), toByte(  0) };
            case 11: return new byte[] { toByte(255), toByte(224), toByte(  0), toByte(  0) };
            case 12: return new byte[] { toByte(255), toByte(240), toByte(  0), toByte(  0) };
            case 13: return new byte[] { toByte(255), toByte(248), toByte(  0), toByte(  0) };
            case 14: return new byte[] { toByte(255), toByte(252), toByte(  0), toByte(  0) };
            case 15: return new byte[] { toByte(255), toByte(254), toByte(  0), toByte(  0) };
            case 16: return new byte[] { toByte(255), toByte(255), toByte(  0), toByte(  0) };
            case 17: return new byte[] { toByte(255), toByte(255), toByte(128), toByte(  0) };
            case 18: return new byte[] { toByte(255), toByte(255), toByte(192), toByte(  0) };
            case 19: return new byte[] { toByte(255), toByte(255), toByte(224), toByte(  0) };
            case 20: return new byte[] { toByte(255), toByte(255), toByte(240), toByte(  0) };
            case 21: return new byte[] { toByte(255), toByte(255), toByte(248), toByte(  0) };
            case 22: return new byte[] { toByte(255), toByte(255), toByte(252), toByte(  0) };
            case 23: return new byte[] { toByte(255), toByte(255), toByte(254), toByte(  0) };
            case 24: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(  0) };
            case 25: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(128) };
            case 26: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(192) };
            case 27: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(224) };
            case 28: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(240) };
            case 29: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(248) };
            case 30: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(252) };
            case 31: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(254) };
            case 32: return new byte[] { toByte(255), toByte(255), toByte(255), toByte(255) };
        }
        throw new IllegalArgumentException("Invalid prefix size: "+bits);
    }

    static byte toByte(int unsigned) {
        if (unsigned < 0 || unsigned > 255) {
            throw new IllegalStateException("Out of unsigned byte range: "+unsigned);
        }
        return  (byte) (unsigned > 127 ? unsigned - 256 : unsigned); //a pox on java and it's signed bytes
    }

    static int toInt(byte signed) {
        if (signed < 0) {
            return signed + 256;
        }
        return signed;
    }

    /**
	 * Verify if a given string is a valid dotted quad notation IP Address
	 * @param networkAddress The address string
	 * @return true if its valid, false otherwise
	 */
	public static boolean isValidIPAddress(String networkAddress) {
		if (networkAddress != null) {
			String[] split = networkAddress.split("\\.");
			if (split.length == 4) {
				int[] octets = new int[4];
				for (int i=0; i<4; i++) {
					try {
						octets[i] = Integer.parseInt(split[i]);
					} catch (NumberFormatException e) {
						return false;
					}
					if (octets[i] < 0 || octets[i] > 255) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * parse ip4 address as dotted quad notation into bytes
	 * @param address
	 * @return
	 */
	private static byte[] parseDottedQuad(String address) {
		String[] splitString = address.split("\\.");

		if (splitString.length == 4) {

			int[] ints = new int[4];
			byte[] bytes = new byte[4];
			for (int i=0; i<4; i++) {
				ints[i] = Integer.parseInt(splitString[i]);
				if (ints[i] < 0 || ints[i] > 255) {
					throw new IllegalArgumentException("Invalid ip4Address or netmask");
				}
				bytes[i] = toByte(ints[i]); //a pox on java and it's signed bytes
			}

			return bytes;
		}
		else {
			throw new IllegalArgumentException("Address must be in dotted quad notation");
		}
	}

}
