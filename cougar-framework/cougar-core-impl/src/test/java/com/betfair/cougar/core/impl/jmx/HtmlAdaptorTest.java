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

package com.betfair.cougar.core.impl.jmx;

import org.junit.After;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;


public class HtmlAdaptorTest  {
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    TlsHtmlAdaptorServer tlsHtmlAdaptorServer = new TlsHtmlAdaptorServer(
            null, "password", "password", 9999, "JKS", true, true);

    @After
    public void tearDown() throws Exception {
        try {
            mBeanServer.unregisterMBean(new ObjectName("CoUGAR:name=HtmlAdaptor"));
        } catch (Exception e) { /* Don't care */ }
        try {
            mBeanServer.unregisterMBean(new ObjectName("CoUGAR.internal:name=HtmlAdaptorParser"));
        } catch (Exception e) { /* Don't care */ }
    }


    @Test
    public void testAdaptorBadName() throws Exception {
        final MBeanServer mBeanServer = mock(MBeanServer.class);

        HtmlAdaptor adaptor = new HtmlAdaptor(tlsHtmlAdaptorServer);
        adaptor.setName("foo");
        adaptor.setMBeanServer(mBeanServer);

        try {
            adaptor.afterPropertiesSet();
            fail();
        } catch (MalformedObjectNameException e) {
            // OK
        }
    }



}
