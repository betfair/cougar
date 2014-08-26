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

package com.betfair.cougar.netutil.nio.marshalling;

import java.util.HashMap;
import java.util.Map;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.transcription.Parameter;

/**
 * receives parameter names and parameter values from a client and matches them to the expected parameters
 * of the server.
 * <li>parameter supplied by the client that are not expected by the server are ignored</li>
 * <li>optional parameters expected by the server but not supplied by the client will have a value of <b>null</b>
 * <li>mandatory parameters expected by the server by not supplied by the client will generate an error
 *
 */
public class ArgumentMatcher {



	/**
	 *
	 * @param expectedArgs - arguments the server is expecting, as defined in the idd
	 * @param availableArgNames - names of arguments supplied by the client
	 * @param availableArgValues - values of arguments supplied by the client
	 * @throws CougarValidationException if a mandatory parameter is not supplied
	 * @return
	 */
	public static Object[] getArgumentValues(Parameter[] expectedArgs, String[] availableArgNames, Object[] availableArgValues) {
		Map<String, Object> availableArgs = new HashMap<String, Object>(availableArgNames.length);
		for (int i=0;i<availableArgNames.length;i++) {
			availableArgs.put(availableArgNames[i], availableArgValues[i]);
		}
		Object[] expectedArgValues = new Object[expectedArgs.length];
		for (int i=0;i<expectedArgs.length;i++) {
			Object argValue = availableArgs.get(expectedArgs[i].getName());
			expectedArgValues[i] = argValue;
		}
		return expectedArgValues;
	}

}
