/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.server;

import com.betfair.cougar.netutil.nio.CougarProtocol;
import com.betfair.cougar.test.socket.tester.common.ClientAuthRequirement;
import com.betfair.cougar.test.socket.tester.common.ServerConfigurations;
import com.betfair.cougar.test.socket.tester.common.SslRequirement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ServerMain {
    public static void main(String[] args) throws IOException {
        System.setProperty("cougar.addressUtils.allowLoopBackIfNoOthers","true");
        // server starts and exposes on 1+ ports.
        // exports to test app the list of ports, with the min protocol version each supports
        // clients then connect to each port they support

        // intention is to start all found servers at the same time (if we can), and then in parallel
        // run each client against each server, so we should have n clients and n servers and n runs of size n (n^2) time complexity

        final List<ServerInstance> servers = new ArrayList<>(5);
        servers.add(new ServerInstance(ServerConfigurations.PLAIN));
        servers.add(new ServerInstance(ServerConfigurations.SUPPORTS_SSL, SslRequirement.Supports));

        servers.add(new ServerInstance(ServerConfigurations.REQUIRES_SSL, SslRequirement.Requires));
        servers.add(new ServerInstance(ServerConfigurations.WANTS_CLIENT_AUTH, SslRequirement.Supports, ClientAuthRequirement.Wants));
        servers.add(new ServerInstance(ServerConfigurations.NEEDS_CLIENT_AUTH, SslRequirement.Supports, ClientAuthRequirement.Needs));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (ServerInstance server : servers) {
                    server.shutdown();
                }
            }
        }, "ServerShutdownThread"));

        String sep = "";
        StringBuilder debugBuffer = new StringBuilder();
        for (ServerInstance server : servers) {
            System.out.println("SERVER: "+server.getName()+","+server.getPort()+","+ CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED+","+CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
            debugBuffer.append(sep).append(server.getName()).append(" ").append(server.getPort()).append(" ").append(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MIN_SUPPORTED).append(" ").append(CougarProtocol.TRANSPORT_PROTOCOL_VERSION_MAX_SUPPORTED);
            sep = " ";
        }
        System.out.println("SERVERS STARTED");
        System.out.println(servers.size()+ " "+debugBuffer);
    }

}
