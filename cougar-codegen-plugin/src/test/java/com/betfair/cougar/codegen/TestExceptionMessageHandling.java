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

package com.betfair.cougar.codegen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


/**
 * This unit test is designed to test the (legacy) behaviour when a IDD defines
 * an exception that includes a parameter named 'message'. This test case tries
 * both a legacy approach (that accomodates IDDs that include a message parameter)
 * and a modern approach, where it fails validation.
 */
public class TestExceptionMessageHandling {
    private static final String RSRC = "test-wsdl.xsl";
    private static final String SERVICE_NAME = "MessageInExceptionTypeService";

    private IdlToDSMojo mojo;


    @Before
    public void setup() throws Exception {
        final String baseDir = System.getProperty("user.dir");

        mojo = new IdlToDSMojo();
        mojo.setBaseDir(baseDir);
        mojo.setWsdlXslResource(RSRC);
        mojo.setXsdXslResource(RSRC);

        Field projectField = IdlToDSMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, mock(MavenProject.class));

        Field iddAsResourceField = IdlToDSMojo.class.getDeclaredField("iddAsResource");
        iddAsResourceField.setAccessible(true);
        iddAsResourceField.setBoolean(mojo, true);

        Field logField = AbstractMojo.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(mojo, mock(Log.class));

        Service s = new Service();
        Field f = Service.class.getDeclaredField("serviceName");
        f.setAccessible(true);
        f.set(s, SERVICE_NAME);

        mojo.setServices(new Service[]{s});
    }

    @Test
    public void testCurrentExceptionMessageParamHandling() throws Exception {
        try {
            mojo.execute();
            fail("this should have failed validation!");
        } catch (MojoExecutionException ex) {
            //Success
        }
    }

    @Test
    public void testLegacyExceptionMessageParamHandling() throws Exception {
        Field legacyExceptionParamValidationField = IdlToDSMojo.class.getDeclaredField("legacyExceptionParamValidation");
        legacyExceptionParamValidationField.setAccessible(true);
        legacyExceptionParamValidationField.setBoolean(mojo, true);

        try {
            mojo.execute();
        } catch (MojoExecutionException ex) {
            fail("this should have passed validation!");
        }
    }


}
