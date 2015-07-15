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

package com.betfair.cougar.test.socket.app;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static com.betfair.cougar.test.socket.app.SocketCompatibilityTestingApp.jsonString;

/**
 *
 */
public class TestCombo implements Runnable {
    private ServerRunner server;
    private ClientRunner client;
    private double runningTime;

    public TestCombo(ServerRunner server, ClientRunner client) {
        this.server = server;
        this.client = client;
    }

    public void run() {
        long startTime = System.nanoTime();
        try {
            client.run(server.getServerConfigs());
        }
        catch (Exception e) {
            e.printStackTrace();
            // todo
        }
        long endTime = System.nanoTime();
        runningTime = (endTime - startTime) / 1000000000.0;
    }

    public double getRunningTime() {
        return runningTime;
    }

    public String getClientVersion() {
        return client.getVersion();
    }

    public String getServerVersion() {
        return server.getVersion();
    }


    public void emitResults(PrintWriter pw, String indent) throws IOException {
        pw.println(" {");
        pw.print(indent + "\"client\": ");
        client.emitInfo(pw, indent + "  ");
        pw.println(",");
        pw.println(indent + "\"serverVersion\": " + jsonString(server.getVersion()) + ",");
        pw.println(indent + "\"tests\": [");
        pw.println(IOUtils.toString(new FileInputStream(client.getResultsJsonFile())));
        pw.println(indent + "]");
        pw.println(indent.substring(2) + "}");
    }

    public String getClientResults() throws IOException {
        return IOUtils.toString(new FileInputStream(client.getResultsJsonFile()));
    }
}
