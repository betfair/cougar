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

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.core.api.ev.ConnectedResponse;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.netutil.nio.NioLogger;
import com.betfair.cougar.netutil.nio.TerminateSubscription;
import com.betfair.cougar.netutil.nio.HandlerListener;
import org.apache.mina.common.IoSession;

/**
 *
 */
public interface ServerConnectedObjectManager extends HandlerListener {

    public void setNioLogger(NioLogger nioLogger);

    public void addSubscription(final SocketTransportCommandProcessor commandProcessor, final SocketTransportRPCCommand command, final ConnectedResponse result, final OperationDefinition operationDefinition, final DehydratedExecutionContext context, final LogExtension connectedObjectLogExtension);

    void terminateSubscription(IoSession session, TerminateSubscription subscription);
}
