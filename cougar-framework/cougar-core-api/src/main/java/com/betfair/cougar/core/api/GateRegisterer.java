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

package com.betfair.cougar.core.api;

import org.springframework.util.Assert;

/**
 * A stateless (or even anonymous) bean which registers one or more {@link GateListener}
 * instances with a given {@link CougarStartingGate}. Use these to save listeners having to
 * register themselves programmatically.
 * <p>
 * You can control ordering of listeners in Spring config by having different registerers
 * {@code depend-on} each other.
 * <p>
 * TODO think of a better name than 'registererererer'
 */
public class GateRegisterer {

	/**
	 * Construct with one or more listeners.
	 * <p>
	 * <strong>A note on wiring</strong>: you can pass a constructor-arg with a {@code list} of
	 * listeners, or a single listener, but you can't init with a (comma-separated) array.
	 *
	 * @param gate
	 * @param listeners listeners to register with the gate (see note about wiring, above)
	 */
	public GateRegisterer(CougarStartingGate gate, GateListener... listeners) {

		Assert.notEmpty(listeners, "GateRegisterer has no listeners.");
		Assert.notNull(gate, "GateRegisterer has not had a CougarStartingGate set.");

		for (GateListener listener : listeners) {
			gate.registerStartingListener(listener);
		}
	}
}
