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

package com.betfair.cougar.util.jmx;

import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Exposes the version of all maven built dependencies onto jmx. It's a dynamic bean so the attributes are named &lt;groupId&gt;:&lt;artifactId&gt; with values
 * as the version of the library.
 */
public class LibraryVersions implements DynamicMBean, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, String> artifacts = new HashMap<String, String>();

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            Enumeration<URL> mavenDirs = LibraryVersions.class.getClassLoader().getResources("META-INF/maven");
            findMavenArtifacts(mavenDirs);
        } catch (IOException ioe) {
            logger.error("Unable to discover maven artifacts", ioe);
        } catch (SAXException e) {
            logger.error("Unable to discover maven artifacts", e);
        } catch (XPathExpressionException e) {
            logger.error("Unable to discover maven artifacts", e);
        }
    }

    // package private for testing purposes
    void findMavenArtifacts(Enumeration<URL> mavenDirs) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // don't allow external entity refs in the poms.
        builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        while (mavenDirs.hasMoreElements()) {
            URL u = mavenDirs.nextElement();
            // jar:file:/home/username/.m2/repository/org/slf4j/slf4j-jdk14/1.5.0/slf4j-jdk14-1.5.0.jar!/META-INF/MANIFEST.MF
            // we're not interested if it's not a jar, although how you'd have a MANIFEST.MF which wasn't in a jar?
            if (u.getProtocol().equals("jar")) {
                // file:/home/username/.m2/repository/com/betfair/tornjak/kpi/3.0-SNAPSHOT/kpi-3.0-SNAPSHOT.jar!/META-INF/MANIFEST.MF
                String file = u.getFile();
                // file:/home/username/.m2/repository/com/betfair/tornjak/kpi/3.0-SNAPSHOT/kpi-3.0-SNAPSHOT.jar
                file = file.substring(0, file.indexOf("!"));
                // /home/username/.m2/repository/com/betfair/tornjak/kpi/3.0-SNAPSHOT/kpi-3.0-SNAPSHOT.jar
                file = file.substring(5);
                // remove %20 etc
                file = URLDecoder.decode(file, "UTF-8");
                try (JarFile jarFile = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        try {
                            if (entry.getName().endsWith("pom.xml")) {
                                try (InputStream is = jarFile.getInputStream(entry)) {
                                    Document doc = builder.parse(is);
                                    String groupId = xpath.evaluate("/project/groupId", doc);
                                    if (groupId == null || "".equals(groupId)) {
                                        groupId = xpath.evaluate("/project/parent/groupId", doc);
                                        if (groupId == null || "".equals(groupId)) {
                                            logger.warn("Can't work out groupId for pom: "+file+"!"+entry);
                                            break;
                                        }
                                    }
                                    String artifactId = xpath.evaluate("/project/artifactId", doc);
                                    if (artifactId == null || "".equals(artifactId)) {
                                        artifactId = xpath.evaluate("/project/parent/artifactId", doc);
                                        if (artifactId == null || "".equals(artifactId)) {
                                            logger.warn("Can't work out artifactId for pom: "+file+"!"+entry);
                                            break;
                                        }
                                    }
                                    String version = xpath.evaluate("/project/version", doc);
                                    if (version == null || "".equals(version)) {
                                        version = xpath.evaluate("/project/parent/version", doc);
                                        if (version == null || "".equals(version)) {
                                            logger.warn("Can't work out version for pom: "+file+"!"+entry);
                                            break;
                                        }
                                    }
                                    artifacts.put(groupId + ":" + artifactId, version);
                                }
                            }
                        }
                        catch (IOException e) {
                            logger.error("Can't resolve "+entry.getName());
                            throw e;
                        }
                    }
                }
            }
            else {
                logger.info("Can't examine maven artifact in non jar location: "+u);
            }
        }
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return artifacts.get(attribute);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        // not supported
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList ret = new AttributeList();
        for (String s : attributes) {
            ret.add(new Attribute(s, artifacts.get(s)));
        }
        return ret;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return attributes;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[artifacts.size()];
        int i=0;
        for (String key : artifacts.keySet()) {
            attributes[i++] = new MBeanAttributeInfo(key, "java.lang.String", "", true, false, false);
        }
        return new MBeanInfo(getClass().getName(), "", attributes, new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
    }
}
