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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;

import com.betfair.cougar.test.CougarTestCase;

public class HtmlAdaptorParserTest extends CougarTestCase {
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    public void testgetMBeanInfo() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        MBeanInfo info = parser.getMBeanInfo();
        assertTrue(info.toString(), info.getDescription().equals("HTML JMX request parser"));
    }

    public void testInvokeParseRequestInvalid() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        assertNull(parser.invoke("parseRequest", new Object[] {"/foo/"}, null));
    }

    public void testInvokeParsePage() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        assertEquals("RESPONSE", parser.invoke("parsePage", new Object[] {"RESPONSE"}, null));
    }

    public void testInvokeParseOther() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        assertNull(parser.invoke("foo", null, null));
    }

    public void testInvokeParseRequestMalformed() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);

        assertNull(parser.invoke("parseRequest", new Object[] {"/administration/eeep"}, null));
    }

    public void testInvokeParseRequestBatchQueryWildcard() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang:type=GarbageCollector,name=*"}, null);
        String[] gcNames = getGCNames(result);
        assertTrue(result, gcNames.length > 1);
        for (String name: gcNames) {
            assertTrue(result, result.contains("~Name~"+name));
            assertTrue(result, result.contains("java.lang:type=GarbageCollector,name="+name));
        }
        assertTrue(result, result.contains("~Valid~true"));
    }

    public void testInvokeParseRequestBatchQueryWildcardInvalid() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang:type=Gar*"}, null);
        assertEquals("", result);
    }

    public void testInvokeParseRequestBatchQueryWildcardSingleAttr() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang:type=GarbageCollector,name=*&an=Name"}, null);
        String[] gcNames = getGCNames(result);
        assertTrue(result, gcNames.length > 1);
        for (String name: gcNames) {
            assertTrue(result, result.contains("~Name~"+name));
            assertTrue(result, result.contains("java.lang:type=GarbageCollector,name="+name));
        }
        assertFalse(result, result.contains("~Valid~true"));
    }

    public void testInvokeParseRequestBatchQuery() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang:type=Runtime&"}, null);
        assertTrue(result, result.contains("~BootClassPathSupported~true~"));
        assertTrue(result, result.contains("~ClassPath~"));
        assertTrue(result, result.startsWith("java.lang:type=Runtime"));
    }

    public void testInvokeParseRequestBatchQuerySingleAttr() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang:type=Runtime&an=BootClassPathSupported"}, null);
        assertTrue(result, result.contains("~BootClassPathSupported~true"));
        assertFalse(result, result.contains("~ClassPath~"));
        assertTrue(result, result.startsWith("java.lang:type=Runtime"));
    }

    public void testInvokeParseRequestBatchQuerySysProps() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=System&"}, null);
        assertTrue(result, result.contains("java.version~"));
        assertTrue(result, result.contains("user.language~"));
        assertTrue(result, result.startsWith("System Properties~"));
    }

    public void testInvokeParseRequestBatchQuerySingleSysProp() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=System&an=java.version"}, null);
        assertTrue(result, result.contains("~java.version~"));
        assertFalse(result, result.contains("user.language~"));
        assertTrue(result, result.startsWith("System Properties~"));
    }

    public void testInvokeParseRequestBatchQueryWithTime() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?on=java.lang%3Atype=Runtime&t=is+ignored"}, null);
        assertTrue(result, result.contains("~BootClassPathSupported~true~"));
        assertTrue(result, result.contains("~ClassPath~"));
        assertTrue(result, result.startsWith("Time~"));
        assertTrue(result, result.contains("~java.lang:type=Runtime~"));
    }

    public void testInvokeParseRequestBatchQueryTimeOnly() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/administration/batchquery.jsp?t"}, null);
        assertTrue(result, result.startsWith("Time~"));
        assertTrue(result, result.length()==29);
    }

    public void testXssDefence() throws Exception {
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        String result = (String)parser.invoke("parseRequest", new Object[] {"/<script>alert('LOL')</script>"}, null);
        assertTrue(result, result.equals("<h1>Illegal request</h1>"));

        result = (String)parser.invoke("parseRequest", new Object[] {"/%3Cscript%3Ealert%28%27LOL%27%29%3C/script%3E"}, null);
        assertTrue(result, result.equals("<h1>Illegal request</h1>"));
    }

    public void testAttributes() throws Exception {
        // Attributes are not implemented
        HtmlAdaptorParser parser = new HtmlAdaptorParser(mBeanServer);
        AttributeList list = new AttributeList();
        list.add(new Attribute("foo", "bar"));
        list.add(new Attribute("bar", "foo"));
        parser.setAttribute((Attribute)list.get(0));
        parser.setAttributes(list);
        assertNull(parser.getAttribute("foo"));
        assertNull(parser.getAttributes(new String[] {"foo", "bar"}));
    }

    private String[] getGCNames(String request) {
        Pattern p = Pattern.compile("~Name~([^~\\|]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(request);
        List<String> gcNames = new ArrayList<String>();
        while (m.find()) {
            gcNames.add(m.group(1));
        }
        return gcNames.toArray(new String[gcNames.size()]);
    }

    public static String getMethodName()
    {
      final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
      return ste[2].getMethodName();
    }

}