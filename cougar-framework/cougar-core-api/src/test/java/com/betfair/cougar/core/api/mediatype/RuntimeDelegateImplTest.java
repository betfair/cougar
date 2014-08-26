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

package com.betfair.cougar.core.api.mediatype;


import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

import com.betfair.cougar.core.api.mediatype.RuntimeDelegateImpl;
import com.betfair.cougar.core.api.mediatype.MediaTypeHeaderProvider;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Unit test for RuntimeDelegateImpl class
 */
public class RuntimeDelegateImplTest extends TestCase {

	public void testCreateHeaderDelegateClassOfT() {
		Assert.assertEquals(MediaTypeHeaderProvider.class, new RuntimeDelegateImpl().createHeaderDelegate(MediaType.class).getClass());
		Assert.assertNull(new RuntimeDelegateImpl().createHeaderDelegate(CacheControl.class));
		Assert.assertNull(new RuntimeDelegateImpl().createHeaderDelegate(String.class));
	}

	public void testCreateEndpointApplicationClassOfT() {
		try {
			new RuntimeDelegateImpl().createEndpoint(null, null);
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			// Ok
		}
	}

	public void testCreateResponseBuilder() {
		try {
			new RuntimeDelegateImpl().createResponseBuilder();
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			// Ok
		}
	}

	public void testCreateUriBuilder() {
		try {
			new RuntimeDelegateImpl().createUriBuilder();
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			// Ok
		}

	}

	public void testCreateVariantListBuilder() {
		try {
			new RuntimeDelegateImpl().createVariantListBuilder();
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			// Ok
		}

	}

}
