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

import java.io.*;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static com.betfair.cougar.test.socket.app.SocketCompatibilityTestingApp.jsonString;

/**
 *
 */
public abstract class JarRunner {
    private final File jarFile;
    private String version;
    private Process backgroundProcess;
    private String[] outputs;
    private File runDir;

    protected JarRunner(File jarFile) throws IOException {
        this.jarFile = jarFile;
        init();
    }

    private void init() throws IOException {
        JarFile jar = new JarFile(jarFile);
        ZipEntry entry = jar.getEntry("META-INF/maven/com.betfair.cougar/socket-tester/pom.properties");
        Properties props = new Properties();
        props.load(jar.getInputStream(entry));
        version = props.getProperty("version");
        jar.close();
    }

    protected void start(File dir, String[] extraArgs, boolean waitUntilExit) throws Exception {
        dir.mkdirs();
        runDir = dir;
        // java -jar <jarfile>
        String[] baseCmd = new String[]{System.getProperty("java.home")+"/bin/java","-jar",jarFile.getAbsolutePath()};
        String[] cmd = new String[baseCmd.length+extraArgs.length];
        System.arraycopy(baseCmd,0,cmd,0,baseCmd.length);
        System.arraycopy(extraArgs,0,cmd,baseCmd.length,extraArgs.length);

        System.out.print("Running: java -jar "+cmd[2].substring(cmd[2].lastIndexOf("/")+1));
        for (int i=3; i<cmd.length; i++) {
            System.out.print(" "+cmd[i]);
        }
        System.out.println();

        Process p = Runtime.getRuntime().exec(cmd,new String[0],dir);
        startOutputStreaming(dir, p.getInputStream(), "stdout");
        startOutputStreaming(dir, p.getErrorStream(), "stderr");
        if (waitUntilExit) {
            p.waitFor();
            outputs = getOutputs();
        }
        else {
            backgroundProcess = p;
        }
    }

    private void startOutputStreaming(final File dir, final InputStream is, final String name) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter fw = new PrintWriter(new File(dir, name+".log"));
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        fw.println(line);
                        fw.flush();
                        outputReceived(line);
                    }
                    is.close();
                    fw.close();
                }
                catch (IOException ioe) {
                    System.err.println("Error copying "+name+" for "+getName());
                    ioe.printStackTrace();
                }
            }
        },name+"-copier-"+getName());
        t.setDaemon(true);
        t.start();
    }

    protected void outputReceived(String line) {

    }

    protected abstract String getName();

    private String[] getOutputs() throws IOException {
        return new String[] {
            IOUtils.toString(new FileReader(new File(runDir, "stdout.log"))), IOUtils.toString(new FileReader(new File(runDir, "stderr.log")))
        };
    }

    protected void stop() throws IOException {
        if (backgroundProcess != null) {
            outputs = getOutputs();
            backgroundProcess.destroy();
        }
    }

    public String getVersion() {
        return version;
    }

    public void emitInfo(PrintWriter pw, String indent) throws IOException {
        pw.print(" {");
        pw.println(indent + "'version': " + jsonString(version));
        pw.println(indent + "'output': " + jsonString(outputs[0]));
        pw.println(indent + "'error': " + jsonString(outputs[1]));
        pw.print(indent.substring(2) + "}");
    }
}
