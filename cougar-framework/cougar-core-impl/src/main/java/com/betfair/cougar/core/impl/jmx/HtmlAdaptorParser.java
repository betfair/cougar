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

import com.betfair.cougar.core.api.jmx.JMXHttpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jdmk.comm.HtmlParser;
import org.apache.commons.lang.StringEscapeUtils;

import javax.management.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class HtmlAdaptorParser implements HtmlParser, DynamicMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlAdaptorParser.class);
    private static final String ADMIN_QUERY = "/administration/";

    private List<JMXHttpParser> parsers = new ArrayList<JMXHttpParser>();
    private MBeanServer mbs;

    public HtmlAdaptorParser(MBeanServer server) {
        this.mbs = server;
    }

    @Override
    public String parsePage(String initialPage) {
        return initialPage;
    }

    void addCustomParser(JMXHttpParser parser) {
        parsers.add(parser);
    }
    @Override
    public String parseRequest(String request) {
        try {
            LOGGER.debug("Parsing JMX/HTTP request {}", request);
            if(!isValid(request)) {
                LOGGER.warn("XSS attempt detected for request: "+request);
                return "<h1>Illegal request</h1>";
            }

            if (request.startsWith(ADMIN_QUERY)) {
                int paramIndex = request.indexOf('?');
                String adminPage;
                if (paramIndex == -1) {
                    adminPage = request.substring(ADMIN_QUERY.length());
                } else {
                    adminPage = request.substring(ADMIN_QUERY.length(), paramIndex);
                }
                Map<String, String> params = getURIParams(request);

                if (adminPage.equals("")) {
                    StringBuilder sb = new StringBuilder();
                    for (JMXHttpParser p: parsers) {
                        sb.append("<a href='").append(p.getPath()).append("'>").append(p.getPath()).append("</a><br/>");
                    }
                    return sb.toString();
                }
                else if (adminPage.equals("batchquery.jsp")) {
                    String objectName = params.get("on");
                    String attributeName = params.get("an");
                    String separator = "~";
                    String time = params.get("t");
                    StringBuilder buf = new StringBuilder();
                    if (time != null) {
                        buf.append("Time");
                        buf.append(separator);
                        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        buf.append(fmt.format(new Date()));
                        buf.append(separator);
                    }
                    if (objectName != null) {
                    	if ("System".equalsIgnoreCase(objectName)) {
                            buf.append("System Properties");
                            Properties p = System.getProperties();
                            for (Map.Entry me: p.entrySet()) {
                                if (attributeName == null
                                        ||"ALL".equals(attributeName)
                                        || attributeName.equals(me.getKey())) {
                                    buf.append(separator);
                                    buf.append(me.getKey());
                                    buf.append(separator);
                                    buf.append(me.getValue());
                                }
                            }
                        } else {
                            query(buf, objectName, attributeName, separator);
                        }
                    }
                    return buf.toString();
                }

                // Loop through the available JmxHttpParsers to see if one matches
                for (JMXHttpParser p: parsers) {
                    if (adminPage.equals(p.getPath())) {
                        return p.process(params);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve Bean information", e);
            return null;
        }

    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClass().getName(), "HTML JMX request parser", null, null, null, null);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if (actionName.equals("parseRequest")) {
            return parseRequest((String) params[0]);
        } else if (actionName.equals("parsePage")) {
            return parsePage((String) params[0]);
        }
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    private Map<String, String> getURIParams(String uri) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        int qmi = uri.indexOf('?');
        if (qmi >= 0) {
            decodeParams(uri.substring(qmi + 1), params);
        }
        return params;
    }

    private void decodeParams(String params, Map<String, String> p) throws Exception {
        StringTokenizer st = new StringTokenizer(params, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
            } else {
                p.put(decodePercent(e).trim(), "true");
            }

        }
    }

    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     * @param str a string
     * @return altered string
     */
    private String decodePercent(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        str = sb.toString();
        if (str.contains("%")) {
            str = decodePercent(str);
        }
        return str;
    }

    /**
     * Validates request string for not containing malicious characters.
     *
     * @param request a request string
     * @return true - if request string is valid, false - otherwise
     */
    private boolean isValid(String request) {

        String tmp = decodePercent(request);
        if(tmp.indexOf('<') != -1 ||
           tmp.indexOf('>') != -1 ) {
            return false;
        }

        return true;
    }

    private void query(StringBuilder buf, String son, String attrName, String separator) {
        try {
            ObjectName on = new ObjectName(son);
            if (on.isPattern()) {
                Set<ObjectInstance> res = mbs.queryMBeans(on, null);
                if (res != null && res.size() > 0) {
                    Iterator<ObjectInstance> j = res.iterator();
                    while (j.hasNext()) {
                        on = j.next().getObjectName();
                        appendMBean(mbs, on, attrName, separator, buf);
                        if (j.hasNext()) {
                            buf.append("|");
                        }
                    }
                }
            } else {
                appendMBean(mbs, on, attrName, separator, buf);
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve Bean information for bean "+son, e);

        }
    }

    private void runOperation(StringBuilder buf, String son, String operation, String separator) {
        try {
            ObjectName on = new ObjectName(son);
            Object result = mbs.invoke(on, operation, new Object[0], new String[0]);
            buf.append(on.toString())
            		.append(separator)
            		.append(operation)
            		.append(separator)
            		.append(result)
            		.append(separator);
        } catch (Exception e) {
        	LOGGER.debug("Unable to run operation {} on bean {} ", operation, separator);

        }
    }

    private void appendMBean(MBeanServer server, ObjectName on, String attrName, String separator, StringBuilder buf) {
        StringBuilder local = new StringBuilder();
        try {

            MBeanInfo info = server.getMBeanInfo(on);
            local.append(on.toString());
            MBeanAttributeInfo[] attr = info.getAttributes();
            for (int i = 0; i < attr.length; i++) {
                if ((attrName == null || attrName.equals(attr[i].getName())) && attr[i].isReadable()) {
                    local.append(separator);
                    local.append(attr[i].getName());
                    local.append(separator);
                    local.append(server.getAttribute(on, attr[i].getName()));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve Bean information for bean "+on, e);
            return;
        }
        buf.append(local);
    }
}
