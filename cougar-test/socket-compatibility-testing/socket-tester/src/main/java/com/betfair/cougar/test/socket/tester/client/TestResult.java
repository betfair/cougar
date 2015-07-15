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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public class TestResult {
    private boolean success;
    private String output;
    private String error;
    private String name;
    private String serverVariant;

    public TestResult(String name, String serverVariant) {
        this.name = name;
        this.serverVariant = serverVariant;
        success = true;
    }

    public String getName() {
        return name;
    }

    public String getServerVariant() {
        return serverVariant;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setOutput(Exception error) {
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        this.output = sw.toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        setSuccess(false);
        this.error = error;
    }

    public void setError(Exception error) {
        setSuccess(false);
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        this.error = sw.toString();
    }
}
