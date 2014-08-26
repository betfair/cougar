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

package com.betfair.cougar.core.impl.jmx;

import java.util.List;

import com.betfair.cougar.util.jmx.Exportable;
import com.betfair.cougar.util.jmx.ExportableRegistration;

/**
 * Helper class for registering Exportables from Spring configuration.
 *
 */
public class ExportableRegistrationHelper {

	/**
	 * Register a list of Exportables with the supplied ExportableRegistration implementation
	 * @param exportableRegistration
	 * @param exportables
	 */
	public ExportableRegistrationHelper(ExportableRegistration exportableRegistration,
			List<Exportable> exportables) {
		for (Exportable exportable : exportables) {
			exportableRegistration.registerExportable(exportable);
		}
	}

	/**
	 * Register an Exportable with the supplied ExportableRegistration implementation
	 * @param exportableRegistration
	 * @param exportable
	 */
	public ExportableRegistrationHelper(ExportableRegistration exportableRegistration,
			Exportable exportable) {
		exportableRegistration.registerExportable(exportable);
	}

}
