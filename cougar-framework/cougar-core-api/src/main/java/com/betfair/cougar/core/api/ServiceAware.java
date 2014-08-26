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

import java.util.Set;

import com.betfair.cougar.api.Service;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the {@link com.betfair.cougar.api.Service}s hosted in the cougar container
 *
 */
public interface ServiceAware {


	/**
	 * Called by the container to advise the list of services hosted in the container
	 * @param services non modifiable list of services hosted by the container
	 */
	public void setServices(Set<Service> services);
}
