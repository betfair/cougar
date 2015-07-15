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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 *
 */
public class SocketCompatibilityTestingApp {

    public static void main(String[] args) throws Exception{

        Parser parser = new PosixParser();
        Options options = new Options();
        options.addOption("r","repo",true,"Repository type to search: local|central");
        options.addOption("c","client-concurrency",true,"Max threads to allow each client tester to run tests, defaults to 10");
        options.addOption("t","test-concurrency",true,"Max client testers to run concurrently, defaults to 5");
        options.addOption("m","max-time",true,"Max time (in minutes) to allow tests to complete, defaults to 10");
        options.addOption("v","version",false,"Print version and exit");
        options.addOption("h","help",false,"This help text");
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption("h")) {
            System.out.println(options);
            System.exit(0);
        }
        if (commandLine.hasOption("v")) {
            System.out.println("How the hell should I know?");
            System.exit(0);
        }
        // 1. Find all testers in given repos
        List<RepoSearcher> repoSearchers = new ArrayList<>();
        for (String repo : commandLine.getOptionValues("r")) {
            if ("local".equals(repo.toLowerCase())) {
                repoSearchers.add(new LocalRepoSearcher());
            }
            else if ("central".equals(repo.toLowerCase())) {
                repoSearchers.add(new CentralRepoSearcher());
            }
            else {
                System.err.println("Unrecognized repo: "+repo);
                System.err.println(options);
                System.exit(1);
            }
        }
        int clientConcurrency = 10;
        if (commandLine.hasOption("c")) {
            try {
                clientConcurrency = Integer.parseInt(commandLine.getOptionValue("c"));
            }
            catch (NumberFormatException nfe) {
                System.err.println("client-concurrency is not a valid integer: '"+commandLine.getOptionValue("c")+"'");
                System.exit(1);
            }
        }
        int testConcurrency = 5;
        if (commandLine.hasOption("t")) {
            try {
                testConcurrency = Integer.parseInt(commandLine.getOptionValue("t"));
            }
            catch (NumberFormatException nfe) {
                System.err.println("test-concurrency is not a valid integer: '"+commandLine.getOptionValue("t")+"'");
                System.exit(1);
            }
        }
        int maxMinutes = 10;
        if (commandLine.hasOption("m")) {
            try {
                maxMinutes = Integer.parseInt(commandLine.getOptionValue("m"));
            }
            catch (NumberFormatException nfe) {
                System.err.println("max-time is not a valid integer: '"+commandLine.getOptionValue("m")+"'");
                System.exit(1);
            }
        }

        Properties clientProps = new Properties();
        clientProps.setProperty("client.concurrency",String.valueOf(clientConcurrency));

        File baseRunDir = new File(System.getProperty("user.dir") + "/run");
        baseRunDir.mkdirs();

        File tmpDir = new File(baseRunDir, "jars");
        tmpDir.mkdirs();

        List<ServerRunner> serverRunners = new ArrayList<>();
        List<ClientRunner> clientRunners = new ArrayList<>();
        for (RepoSearcher searcher : repoSearchers) {
            List<File> jars = searcher.findAndCache(tmpDir);
            for (File f : jars) {
                ServerRunner serverRunner = new ServerRunner(f, baseRunDir);
                System.out.println("Found tester: "+serverRunner.getVersion());
                serverRunners.add(serverRunner);
                clientRunners.add(new ClientRunner(f, baseRunDir, clientProps));
            }
        }

        // 2. Start servers and collect ports
        System.out.println();
        System.out.println("Starting "+serverRunners.size()+" servers...");
        for (ServerRunner server : serverRunners) {
            server.startServer();
        }
        System.out.println();


        List<TestCombo> tests = new ArrayList<>(serverRunners.size() * clientRunners.size());
        for (ServerRunner server : serverRunners) {
            for (ClientRunner client : clientRunners) {
                tests.add(new TestCombo(server,client));
            }
        }

        System.out.println("Enqueued "+tests.size()+" test combos to run...");

        long startTime = System.currentTimeMillis();
        // 3. Run every client against every server, collecting results
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue(serverRunners.size() * clientRunners.size());
        ThreadPoolExecutor service = new ThreadPoolExecutor(testConcurrency,testConcurrency,5000, TimeUnit.MILLISECONDS,workQueue);
        service.prestartAllCoreThreads();
        workQueue.addAll(tests);
        while (!workQueue.isEmpty()) {
            Thread.sleep(1000);
        }
        service.shutdown();
        service.awaitTermination(maxMinutes,TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        long totalTimeSecs = Math.round((endTime - startTime) / 1000.0);
        for (ServerRunner server : serverRunners) {
            server.shutdownServer();
        }

        System.out.println();
        System.out.println("=======");
        System.out.println("Results");
        System.out.println("-------");
        // print a summary
        int totalTests = 0;
        int totalSuccess = 0;
        for (TestCombo combo : tests) {
            String clientVer = combo.getClientVersion();
            String serverVer = combo.getServerVersion();
            String results = combo.getClientResults();
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            JsonNode node = mapper.reader().readTree(results);
            JsonNode resultsArray = node.get("results");
            int numTests = resultsArray.size();
            int numSuccess = 0;
            for (int i=0; i<numTests; i++) {
                if ("success".equals(resultsArray.get(i).get("result").asText())) {
                    numSuccess++;
                }
            }
            totalSuccess += numSuccess;
            totalTests += numTests;
            System.out.println(clientVer+"/"+serverVer+": "+numSuccess+"/"+numTests+" succeeded - took "+String.format("%2f",combo.getRunningTime())+" seconds");
        }
        System.out.println("-------");
        System.out.println("Overall: "+totalSuccess+"/"+totalTests+" succeeded - took "+totalTimeSecs+" seconds");

        FileWriter out = new FileWriter("results.json");
        PrintWriter pw = new PrintWriter(out);

        // 4. Output full results
        pw.println("{\n  \"results\": [");
        for (TestCombo combo : tests) {
            combo.emitResults(pw, "    ");
        }
        pw.println("  ],");
        pw.println("  \"servers\": [");
        for (ServerRunner server : serverRunners) {
            server.emitInfo(pw, "    ");
        }
        pw.println("  ],");
        pw.close();
    }

    public static String jsonString(String s) {
        StringBuilder buff = new StringBuilder();
        buff.append("\"");
        buff.append(s.replace("\"","\\\"").replace("\n"," ").replace("\t"," "));
        buff.append("\"");
        return buff.toString();
    }
}
