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

package com.betfair.cougar.transport.socket;

import com.betfair.cougar.core.api.RequestTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.transport.api.protocol.CougarObjectInput;
import org.apache.mina.common.IoSession;

import java.util.concurrent.atomic.AtomicReference;

public class SocketTransportCommandImpl implements SocketTransportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketTransportCommandImpl.class);

	private final CougarObjectInput input;
    private final String remoteAddress;
	private AtomicReference<CommandStatus> status = new AtomicReference<CommandStatus>(CommandStatus.InProgress);

    private RequestTimer timer = new RequestTimer();
    private IoSession session;

    public SocketTransportCommandImpl(CougarObjectInput input, String remoteAddress, IoSession session) {
		this.input = input;
        this.remoteAddress = remoteAddress;
        this.session = session;
	}

    @Override
	public CommandStatus getStatus() {
		return status.get();
	}

    @Override
    public RequestTimer getTimer() {
        return timer;
    }

    @Override
	public void onComplete() {
		status.set(CommandStatus.Complete);
	}

	@Override
	public CougarObjectInput getInput() {
		return input;
	}

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    public IoSession getSession() {
        return session;
    }
}
