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

package com.betfair.cougar.api;


import com.betfair.tornjak.monitor.MonitorRegistry;

/**
 * Contextual information relating to an instance of Cougar.
 */
public interface ContainerContext {

    /**
     * Retrieves information regarding all service registered with this instance of Cougar.
     * @return
     */
    ServiceInfo[] getRegisteredServices();

	public void registerExtensionLoggerClass(Class<? extends LogExtension> clazz, int numFieldsLogged);
	public void registerConnectedObjectExtensionLoggerClass(Class<? extends LogExtension> clazz, int numFieldsLogged);

    public MonitorRegistry getMonitorRegistry();

}
