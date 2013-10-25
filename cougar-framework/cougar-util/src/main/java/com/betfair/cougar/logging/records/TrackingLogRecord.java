/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.logging.records;

import java.util.logging.Level;


public class TrackingLogRecord extends CougarLogRecord {
	public enum Action {ENTERING, LEAVING, THROWING}
	
	private Action action;
	private Object[] args;
	
	public TrackingLogRecord(String logName, Action action, String clazz, String method, Object... args) {
		super(	logName, 
				Level.FINER,
				"Tracking log event for %s %s.%s",
				action, clazz, method);

		setSourceClassName(clazz);
		setSourceMethodName(method);
		this.action = action;
		this.args = args;
	}

	@Override
	public String getMessage() {
		// We're actually logging - lets get a better string
		String result;
		if (action == Action.LEAVING) {
			Object retVal = (args == null || args.length == 0) ? "NONE" : args[0];
			result = String.format("Leaving %s.%s with return value: %s", getSourceClassName(), getSourceMethodName(), retVal);
		} else if (action == Action.THROWING) {
			Object thrown = (args == null || args.length == 0) ? "NO EXCEPTION" : args[0];
			result = String.format("Throwing exception from %s.%s Exception was: %s", getSourceClassName(), getSourceMethodName(), thrown);
		} else if (action == Action.ENTERING) {
			StringBuilder msg = new StringBuilder("Entering ")
						.append(getSourceClassName())
						.append(".")
						.append(getSourceMethodName())
						.append(" with args:");
			if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; ++i) {
                    msg.append(" %s");
                }
				result = String.format(msg.toString(), args);
			} else {
				msg.append(" NONE");
				result = msg.toString();
			}
			
		} else {
			throw new IllegalArgumentException("Unknown Action: "+action);
		}
		return result;
	}

	public Action getAction() {
		return action;
	}
	
}
