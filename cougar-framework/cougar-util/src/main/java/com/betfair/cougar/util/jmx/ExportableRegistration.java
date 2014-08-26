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

package com.betfair.cougar.util.jmx;

/**
 * Interface for registering JMX Exportable classes.
 * The ExportableRegistration implementation should be aware of a JMXControl,
 * and will call export() on all registered Exportables as soon as the
 * JMXControl becomes available.
 *
 */
public interface ExportableRegistration {

	/**
	 * Register an Exportable class, so that it may be exported to JMX
	 * as soon as the JMXControl is available.
	 * @param exportable class that can be exported to JMX
	 */
	public void registerExportable(Exportable exportable);
}
