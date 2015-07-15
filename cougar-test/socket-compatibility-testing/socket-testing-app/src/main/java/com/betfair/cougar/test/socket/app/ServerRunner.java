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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ServerRunner extends JarRunner {

    private static final AtomicInteger idGen = new AtomicInteger();
    private final int id = idGen.incrementAndGet();
    private final File workingDir;
    private final CountDownLatch latch = new CountDownLatch(1);
    private List<String[]> serverConfigs = new ArrayList<>();

    public ServerRunner(File jarFile, File baseWorkingDir) throws IOException {
        super(jarFile);
        workingDir = new File(baseWorkingDir,getName());
    }

    @Override
    protected String getName() {
        return "server_"+id;
    }

    public void startServer() throws Exception {
        super.start(workingDir, new String[]{"server"}, false);
        latch.await();
    }

    @Override
    protected void outputReceived(String line) {
        if (line.startsWith("SERVER: ")) {
            String[] params = line.substring(8).split(",");
            serverConfigs.add(params);
        }
        if (line.equals("SERVERS STARTED")) {
            latch.countDown();
        }
    }

    public List<String[]> getServerConfigs() {
        return serverConfigs;
    }

    public void shutdownServer() throws IOException {
        super.stop();
    }
}
