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

package com.betfair.cougar.transport.impl.protocol.http;

import java.util.Map;

import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.transport.api.TransportCommandProcessorFactory;
import com.betfair.cougar.transport.api.protocol.http.HttpCommandProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.betfair.cougar.core.api.exception.PanicInTheCougar;

public class HttpCommandProcessorFactory implements TransportCommandProcessorFactory<HttpCommandProcessor>, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private Map<Protocol, String> commandProcessorNames;

    /**
     * Returns the command processor assocated with the supplied protocol
     * @param @see Protocol
     * @return returns you the command processor for the supplied protocol
     * @throws PanicInTheCougar if there is no command processor for the requested protocol
     */
	@Override
	public HttpCommandProcessor getCommandProcessor(Protocol protocol) {
		String commandProcessorName = commandProcessorNames.get(protocol);

        if (commandProcessorName == null) {
			throw new PanicInTheCougar("No HTTP Command Processor has been configured for protocol " + protocol);
        }

        HttpCommandProcessor commandProcessor = (HttpCommandProcessor)applicationContext.getBean(commandProcessorName);

        if (commandProcessor == null) {
			throw new PanicInTheCougar("No HTTP Command Processor has been configured for the name " + commandProcessorName);
        }

		return commandProcessor;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setCommandProcessorNames(Map<Protocol, String> commandProcessorNames) {
		this.commandProcessorNames = commandProcessorNames;
	}

}
