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

import java.net.UnknownHostException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


public class NetworkAddressTest {

	@Test
	public void testInNetwork() throws UnknownHostException {
		NetworkAddress networkAddress = NetworkAddress.parse("64.12.96.0/255.255.224.0");

		assertTrue("should be in network", networkAddress.isAddressInNetwork("64.12.96.0"));
		assertTrue("should be in network", networkAddress.isAddressInNetwork("64.12.96.1"));
		assertTrue("should be in network", networkAddress.isAddressInNetwork("64.12.127.254"));
		assertTrue("should be in network", networkAddress.isAddressInNetwork("64.12.127.255"));
	}

	@Test
	public void testNotInNetwork() throws UnknownHostException {
		NetworkAddress networkAddress = NetworkAddress.parse("64.12.96.0/255.255.224.0");

		assertFalse("should not be in network", networkAddress.isAddressInNetwork("64.12.64.0"));
		assertFalse("should not be in network", networkAddress.isAddressInNetwork("64.12.255.254"));
	}

	@Test
	public void testInvalidFormat() {

		try {
			NetworkAddress networkAddress = NetworkAddress.parse("64.12.96.0|255.255.224.0");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}
	}

	@Test
	public void testInvalidAddressFormat() {

		try {
			NetworkAddress networkAddress = NetworkAddress.parse("12.96.0/255.255.224.0");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}
	}


	@Test
	public void testInvalidAddressValue() {
		try {
			NetworkAddress networkAddress = NetworkAddress.parse("256.12.96.0/255.255.224.0");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}

		try {
			NetworkAddress networkAddress = NetworkAddress.parse("255.12.96.-1/255.255.224.0");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}

	}

	@Test
	public void testInvalidMaskValue() {
		try {
			NetworkAddress networkAddress = NetworkAddress.parse("255.12.96.0/256.255.224.0");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}
		try {
			NetworkAddress networkAddress = NetworkAddress.parse("255.12.96.0/255.255.224.-1");
			Assert.fail("expected Illegal arguement exception");
		}
		catch (IllegalArgumentException e) {
			//expected
		}

	}

	@Test
	public void testIPAddressFormatValidation() {
		assertFalse("Invalid characters in IP address", NetworkAddress.isValidIPAddress("10.10.1.a"));
		assertFalse("Invalid characters in IP address", NetworkAddress.isValidIPAddress("10.10.1.10, 10.10.1.10"));
		assertFalse("Invalid characters in IP address", NetworkAddress.isValidIPAddress("10.10.1.10 10.10.1.10"));
		assertFalse("Invalid characters in IP address", NetworkAddress.isValidIPAddress("10.10.1.10/10.10.1.10"));
		assertFalse("Out of range values in IP address", NetworkAddress.isValidIPAddress("10.10.1.-1"));
		assertFalse("Out of range values in IP address", NetworkAddress.isValidIPAddress("10.258.1.10"));
		assertFalse("Null IP address", NetworkAddress.isValidIPAddress(null));
		assertFalse("Empty String IP address", NetworkAddress.isValidIPAddress(""));
		assertTrue("Valid IP address", NetworkAddress.isValidIPAddress("192.168.10.1"));
	}

    @Test
    public void validCidrNotation() {
        // examples from http://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
        NetworkAddress address = NetworkAddress.parseBlock("10.10.1.32/27");
        assertEquals("10.10.1.32/255.255.255.224", address.toString());
        assertTrue(address.isAddressInNetwork("10.10.1.44"));
        assertFalse(address.isAddressInNetwork("10.10.1.90"));

        address = NetworkAddress.parseBlock("192.168.100.0/24");
        assertEquals("192.168.100.0/255.255.255.0", address.toString());
    }

    private byte[] address(int a, int b, int c, int d) {
        return new byte[] { NetworkAddress.toByte(a), NetworkAddress.toByte(b), NetworkAddress.toByte(c), NetworkAddress.toByte(d) };
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCidrNotationNonNumeric() {
        NetworkAddress.parseBlock("a.b.c.d/a");
    }
    @Test(expected = IllegalArgumentException.class)
    public void invalidCidrNotationInvalidIpV4() {
        NetworkAddress.parseBlock("10.267.10.10/");
    }
    @Test(expected = IllegalArgumentException.class)
    public void invalidCidrNotationInvalidPrefixSize() {
        NetworkAddress.parseBlock("10.10.10.10/33");
    }
    @Test(expected = IllegalArgumentException.class)
    public void invalidCidrNotationInvalidPrefixSizeNegative() {
        NetworkAddress.parseBlock("10.10.10.10/-1");
    }

}
