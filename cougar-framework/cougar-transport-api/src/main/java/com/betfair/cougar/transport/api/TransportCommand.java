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

package com.betfair.cougar.transport.api;

import com.betfair.cougar.core.api.RequestTimer;

/**
 * An invocation from the Transport to process a unit of work.
 * The interface is intended to be extended to provide transport specific
 * information that can be resolved to an ExecutionCommand.
 */
public interface TransportCommand {

	enum CommandStatus {InProgress, TimedOut, Complete };

	/**
	 * Notify that the unit of work has been completed
	 */
	public void onComplete();

	/**
	 * Returns the current status of this command
	 * @return
	 */
	public CommandStatus getStatus();

        /**
     * Gets a RequestTimer that was started when this command was first received.
     * @return
     */
    public RequestTimer getTimer();
}
