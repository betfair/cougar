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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ClientRunner extends JarRunner {

    private static final AtomicInteger idGen = new AtomicInteger();
    private final int id = idGen.incrementAndGet();
    private final File workingDir;
    private String[] outputs;
    private Properties runProps;

    public ClientRunner(File jarFile, File baseWorkingDir, Properties runProps) throws IOException {
        super(jarFile);
        this.runProps = runProps;
        workingDir = new File(baseWorkingDir,getName());
    }

    @Override
    protected String getName() {
        return "client_"+id;
    }

    public void run(List<String[]> serverConfigs) throws Exception {
        List<String> args = new LinkedList<>();
        args.add("client");
        args.add(String.valueOf(serverConfigs.size()));
        for (String[] sArgs : serverConfigs) {
            for (String s : sArgs) {
                args.add(s);
            }
        }
        workingDir.mkdirs();
        runProps.store(new FileOutputStream(new File(workingDir,"run.properties")),"Run properties");
        super.start(workingDir, args.toArray(new String[args.size()]), true);
    }

    public File getResultsJsonFile() {
        return new File(workingDir, "results.json");
    }


}
