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

package com.betfair.cougar.core.api.monitor;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import com.betfair.tornjak.monitor.MonitorRegistry;
import org.junit.Test;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.Service;

public class InterfaceUtilsTest{

	@Test
	public void testExtendsSimple() {
		Class<Service> service = InterfaceUtils.getInterface(new ExtendsSimple() {
			public void init(ContainerContext cc) {
			}
        });
		assertEquals(ExtendsSimple.class, service);
	}

	@Test
	public void testExtendsMany() {
		Class<Service> service = InterfaceUtils.getInterface(new ExtendsMany());
		assertEquals(ExtendsSimple.class, service);
	}

	public interface ExtendsSimple extends Service {}

	public static class ExtendsMany implements Serializable, ExtendsSimple {
		public void init(ContainerContext cc) {
		}
    }

}
