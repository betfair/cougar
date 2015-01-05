/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.testing.utils.cougar.assertions;


import com.betfair.testing.utils.cougar.misc.DataTypeEnum;
import com.betfair.testing.utils.cougar.misc.ObjectUtil;
import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.ToTextStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.testng.AssertJUnit.*;

/**
 * Straight swap replacement for the JETT assertEquals class, but without all the JETT baggage..
 */
public class AssertionUtils {

    private static long dateTolerance = 2000;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        Document expected = new XMLHelpers().createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<a><b><c>1</c><c>2</c><c>3</c></b><b><c>1</c><c>2</c><c>3</c></b></a>").getBytes())));
        Document actual = new XMLHelpers().createAsDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(("<a><b><c>2</c><c>1</c><c>3</c></b><b><c>3</c><c>2</c><c>1</c></b></a>").getBytes())));
        try {
            multiAssertEquals(expected,actual);
            System.out.println("Failed: should have been different");
        }
        catch (AssertionError e) {
            System.out.println("Passed: were different");
        }
        try {
            multiAssertEquals(expected,actual,"/a/b");
            System.out.println("Passed: weren't different");
        }
        catch (AssertionError e) {
            System.out.println("Failed: shouldn't have been different");
        }
    }

    public static void multiAssertEquals(Document expected, Document actual, String... unorderedXpaths) throws RuntimeException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            for (String x : unorderedXpaths) {
                doDomSorting(expected, xpath, x);
                doDomSorting(actual, xpath, x);
            }
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        jettAssertEquals(null, expected, actual);
    }

    private static void doDomSorting(Document doc, XPath xpath, String x) throws XPathExpressionException, IOException {
        NodeList parentNodes = (NodeList) xpath.evaluate(x,doc, XPathConstants.NODESET);
        for (int i=0; i<parentNodes.getLength(); i++) {
            Node n = parentNodes.item(i);
            List<Node> allKids = new ArrayList<>(n.getChildNodes().getLength());
            for (int j=n.getChildNodes().getLength()-1; j>=0; j--) {
                allKids.add(n.removeChild(n.getFirstChild()));
            }
            final Map<Node,String> kidsToString = new HashMap<>();

            for (Node k : allKids) {
                kidsToString.put(k,toString(k));
            }
            Collections.sort(allKids, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return kidsToString.get(o1).compareTo(kidsToString.get(o2));
                }
            });
            for (Node k : allKids) {
                n.appendChild(k);
            }
        }
    }

    private static String toString(Node k) throws IOException {
        ToTextStream s = new ToTextStream();
        StringWriter sw = new StringWriter();
        s.setWriter(sw);
        s.serialize(k);
        return sw.toString();

    }

    public static void multiAssertEquals(JSONObject expected, JSONObject actual, String... unorderedXpaths) throws RuntimeException {
        try {
            for (String x : unorderedXpaths) {
                doJsonSorting(expected, x);
                doJsonSorting(actual, x);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        jettAssertEquals(null, expected, actual);
    }

    private static void doJsonSorting(JSONObject doc, String x) throws XPathExpressionException, IOException, JSONException {
        JXPathContext ctx = JXPathContext.newContext(doc);
        String parentX = x.substring(0,x.lastIndexOf("/"));
        if ("".equals(parentX)) {
            parentX = "/";
        }
        String childName = x.substring(x.lastIndexOf("/")+1);
        Iterator it = ctx.iterate(parentX);
        while (it.hasNext()) {
            JSONObject p = (JSONObject) it.next();
            JSONArray n = p.getJSONArray(childName);
            List allKids = new ArrayList<>(n.length());
            for (int j=0; j<n.length(); j++) {
                allKids.add(n.get(j));
            }
            Collections.sort(allKids, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            JSONArray newArray = new JSONArray(allKids);
            p.put(childName,newArray);
        }
    }

    public static void multiAssertEquals(Object expected, Object actual) {
        jettAssertEquals(null, expected, actual);
    }

    public static void jettAssertEquals(String message, Object expected, Object actual) {
        if (actual == null) {
            assertNull(message, expected);
        }
        else {
            DataTypeEnum actualObjectType = ObjectUtil.resolveType(actual.getClass());
      		IAssertion asserter = AssertionProcessorFactory.getAssertionProcessor(actualObjectType);
      		asserter.execute(message, expected, actual, null);
        }
    }

    public static void actionFail(String s) {
        fail(s);
    }

    public static void actionPass(String s) {
        // nothing to do
    }

    public static void jettAssertNull(String s, Object actualValue) {
        assertNull(s, actualValue);
    }

    public static void jettAssertTrue(String s, boolean checkBehaviour) {
        assertTrue(s, checkBehaviour);
    }

    public static void jettAssertFalse(String s, boolean b) {
        assertFalse(s, b);
    }

    public static void actionException(Exception e) {
        throw new AssertionError(e);
    }

    public static void jettAssertDatesWithTolerance(String errorMessage, Date expectedValue, Date actualValue) {
        jettAssertDatesWithTolerance(errorMessage, expectedValue, actualValue, getDateTolerance());
    }

    public static void jettAssertDatesWithTolerance(String errorMessage, Date expectedValue, Date actualValue, long tolerance) {
        if (expectedValue == null) {
            assertNull(actualValue);
        }
        else {
            assertNotNull(actualValue);
            long expected = expectedValue.getTime();
            long actual = expectedValue.getTime();
            long diff = Math.abs(expected - actual);
            if (diff > tolerance) {
                if (errorMessage != null) {
                    fail(errorMessage);
                }
                else {
                    fail("Expected: "+expectedValue+" with a tolerance of "+tolerance+"ms, but got: "+actualValue);
                }
            }
        }
    }

    public static long getDateTolerance() {
        return dateTolerance;
    }

    public static long setDateTolerance(long dateTolerance) {
        long ret = AssertionUtils.dateTolerance;
        AssertionUtils.dateTolerance = dateTolerance;
        return ret;
    }
}
