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

package com.betfair.cougar.test.socket.tester.client;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextImpl;
import com.betfair.cougar.api.security.Credential;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.core.impl.security.IdentityChainImpl;
import com.betfair.cougar.test.socket.tester.client.tests.EchoFailureTest;
import com.betfair.cougar.test.socket.tester.client.tests.EchoSuccessTest;
import com.betfair.cougar.test.socket.tester.client.tests.HeapTest;
import com.betfair.cougar.test.socket.tester.common.*;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.UUIDGeneratorImpl;

import java.io.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Future;

/**
 *
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        System.setProperty("cougar.addressUtils.allowLoopBackIfNoOthers","true");
        {
            ExecutionContext ctx = createExecutionContext();
            Conversion.convert(ctx, ExecutionContext.class, ExecutionContextTO.class);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("prompt")) {
            System.out.print("Enter client args: ");
            System.out.flush();
            args = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");
        }

        Properties runProps = new Properties();
        File propsFile = new File("run.properties");
        if (propsFile.exists()) {
            runProps.load(new FileInputStream(propsFile));
        }

        int serverCount = Integer.parseInt(args[0]);
        Map<String,ServerConfiguration> servers = new HashMap<>(serverCount);
        int argsPerServer = (args.length - 1) / serverCount;

        for (int i=1; i<args.length; i+=argsPerServer) {
            String variant = args[i];
            int port = Integer.parseInt(args[i+1]);
            int minProtocolVersion = Integer.parseInt(args[i+2]);
            int maxProtocolVersion = Integer.parseInt(args[i+3]);
            servers.put(variant, new ServerConfiguration(port, minProtocolVersion, maxProtocolVersion, variant));
        }

        TestRunner runner = new TestRunner(Integer.parseInt(runProps.getProperty("client.concurrency", "10")));

        // 1. Echo Success - here we'll test all the combos of ssl/client auth - DONE
        // 2. Echo Failure (throws exception) - just the test here, once for each server - DONE
        // 3. Connected object - same, just the test, once for each server - DONE

        ServerConfiguration plainServer = servers.get(ServerConfigurations.PLAIN);
        runner.runTest(new EchoSuccessTest(plainServer));
        runner.runTest(new EchoSuccessTest(plainServer, SslRequirement.Supports));
        runner.runTest(new EchoSuccessTest(plainServer, SslRequirement.Requires, false));
        // these just one test per config - one that should succeed
        runner.runTest(new EchoFailureTest(plainServer));
        runner.runTest(new HeapTest(plainServer));

        if (servers.containsKey(ServerConfigurations.SUPPORTS_SSL)) {
            ServerConfiguration supportsSsl = servers.get(ServerConfigurations.SUPPORTS_SSL);
            runner.runTest(new EchoSuccessTest(supportsSsl));
            runner.runTest(new EchoSuccessTest(supportsSsl, SslRequirement.Supports));
            runner.runTest(new EchoSuccessTest(supportsSsl, SslRequirement.Requires));
            runner.runTest(new EchoSuccessTest(supportsSsl, SslRequirement.Supports, ClientAuthRequirement.Wants));
            runner.runTest(new EchoSuccessTest(supportsSsl, SslRequirement.Requires, ClientAuthRequirement.Wants));
            // these just one test per config - one that should succeed
            runner.runTest(new EchoFailureTest(supportsSsl));
            runner.runTest(new HeapTest(supportsSsl));

            ServerConfiguration requiresSsl = servers.get(ServerConfigurations.REQUIRES_SSL);
            runner.runTest(new EchoSuccessTest(requiresSsl, false));
            runner.runTest(new EchoSuccessTest(requiresSsl, SslRequirement.Supports));
            runner.runTest(new EchoSuccessTest(requiresSsl, SslRequirement.Requires));
            runner.runTest(new EchoSuccessTest(requiresSsl, SslRequirement.Supports, ClientAuthRequirement.Wants));
            runner.runTest(new EchoSuccessTest(requiresSsl, SslRequirement.Requires, ClientAuthRequirement.Wants));
            // these just one test per config - one that should succeed
            runner.runTest(new EchoFailureTest(requiresSsl, SslRequirement.Supports));
            runner.runTest(new HeapTest(requiresSsl, SslRequirement.Supports));

            ServerConfiguration wantsClientAuth = servers.get(ServerConfigurations.WANTS_CLIENT_AUTH);
            runner.runTest(new EchoSuccessTest(wantsClientAuth));
            runner.runTest(new EchoSuccessTest(wantsClientAuth, SslRequirement.Supports));
            runner.runTest(new EchoSuccessTest(wantsClientAuth, SslRequirement.Requires));
            runner.runTest(new EchoSuccessTest(wantsClientAuth, SslRequirement.Supports, ClientAuthRequirement.Wants));
            runner.runTest(new EchoSuccessTest(wantsClientAuth, SslRequirement.Requires, ClientAuthRequirement.Wants));
            // these just one test per config - one that should succeed
            runner.runTest(new EchoFailureTest(wantsClientAuth));
            runner.runTest(new HeapTest(wantsClientAuth));

            ServerConfiguration needsClientAuth = servers.get(ServerConfigurations.NEEDS_CLIENT_AUTH);
            runner.runTest(new EchoSuccessTest(needsClientAuth, false));
            runner.runTest(new EchoSuccessTest(needsClientAuth, SslRequirement.Supports, false));
            runner.runTest(new EchoSuccessTest(needsClientAuth, SslRequirement.Requires, false));
            runner.runTest(new EchoSuccessTest(needsClientAuth, SslRequirement.Supports, ClientAuthRequirement.Wants));
            runner.runTest(new EchoSuccessTest(needsClientAuth, SslRequirement.Requires, ClientAuthRequirement.Wants));
            // these just one test per config - one that should succeed
            runner.runTest(new EchoFailureTest(needsClientAuth, SslRequirement.Supports, ClientAuthRequirement.Wants));
            runner.runTest(new HeapTest(needsClientAuth, SslRequirement.Supports, ClientAuthRequirement.Wants));
        }

        PrintWriter pw = new PrintWriter(new FileWriter("results.json"));
        pw.println("{\n  \"results\": [");
        List<Future<TestResult>> results = runner.await(Integer.MAX_VALUE);
        String sep = "";
        for (Future<TestResult> future : results) {
            pw.print(sep);
            sep=",\n";
            TestResult result = future.get();
            pw.print("    { \"name\": " + jsonString(result.getName())+", \"serverVariant\": "+jsonString(result.getServerVariant()));
            pw.print(", \"result\": "+jsonString(result.isSuccess()?"success":"failure"));
            pw.print(", \"out\": "+jsonString(result.getOutput()));
            pw.print(", \"err\": "+jsonString(result.getError()));
            pw.println(" }");
        }
        pw.println("  ]\n}");
        pw.flush();
        pw.close();
    }

    private static String jsonString(String s) {
        if (s==null) {
            return "\"\"";
        }
        StringBuilder buff = new StringBuilder();
        buff.append("\"");
        buff.append(s.replace("\"","\\\"").replace("\n"," ").replace("\t"," "));
        buff.append("\"");
        return buff.toString();
    }

    public static ExecutionContext createExecutionContext() {
        ExecutionContextImpl ctx = new ExecutionContextImpl();
        GeoLocationData geoData = new GeoLocationData();
        ctx.setGeoLocationDetails(geoData);
        List<Identity> identities = new ArrayList<>();
        identities.add(new Identity() {
            @Override
            public Principal getPrincipal() {
                return new SimplePrincipal("fred"); // will get lost
            }

            @Override
            public Credential getCredential() {
                return new SimpleCredential("name", "P4ssw0rd!"); // will get retained
            }
        });
        IdentityChainImpl chain = new IdentityChainImpl(identities);
        ctx.setIdentity(chain);
        ctx.setRequestTime(new Date());
        RequestUUIDImpl.setGenerator(new UUIDGeneratorImpl());
        ctx.setRequestUUID(new RequestUUIDImpl());
        ctx.setTraceLoggingEnabled(false);
        return ctx;
    }


}
