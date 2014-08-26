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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.xml.sax.SAXException;

import javax.management.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;

/**
 *
 *
 */
public class ApplicationChecksums implements DynamicMBean, InitializingBean {

    public static final String FILES_INCLUDED_ATTRIBUTE = "FILES_INCLUDED_IN_CHECKSUM";
    public static final String APPLICATION_CHECKSUM = "ApplicationChecksum";
    public static final String SEP = "::";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String[] algorithms;
    private Map<String, Map<String, String>> fileCheckSumsByAlgorithm;
    private Map<String, String> appCheckSumsByAlgorithm;
    private String filesIncluded;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Calculating application checksums using algorithms: " + Arrays.toString(algorithms));
        try {
            List<File> jars = findNonJdkJars();
            // make sure they're always in the same order
            Collections.sort(jars);
            String[] files = new String[jars.size()];
            Map<String, MessageDigest> appDigests = new HashMap<String, MessageDigest>();
            Map<String, MessageDigest> fileDigests = new HashMap<String, MessageDigest>();
            Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();
            for (String alg : algorithms) {
                fileDigests.put(alg, MessageDigest.getInstance(alg));
                appDigests.put(alg, MessageDigest.getInstance(alg));
                results.put(alg, new HashMap());
            }
            int i = 0;
            for (File j : jars) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Including: " + j);
                }
                files[i++] = j.getCanonicalPath();
                try (FileInputStream fis = new FileInputStream(j)) {

                    byte[] dataBytes = new byte[1024];

                    int nread = 0;

                    while ((nread = fis.read(dataBytes)) != -1) {
                        for (Map.Entry<String, MessageDigest> entry : fileDigests.entrySet()) {
                            final MessageDigest appDigest = appDigests.get(entry.getKey());
                            final MessageDigest fileDigest = entry.getValue();
                            fileDigest.update(dataBytes, 0, nread);
                            appDigest.update(dataBytes, 0, nread);
                        }
                    }

                    for (Map.Entry<String, MessageDigest> entry : fileDigests.entrySet()) {
                        MessageDigest md = entry.getValue();
                        String algorithm = entry.getKey();
                        byte[] mdbytes = md.digest();
                        //convert the byte to hex format
                        StringBuilder sb = new StringBuilder();
                        for (byte mdbyte : mdbytes) {
                            sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
                        }
                        results.get(algorithm).put(j.getName(), sb.toString());
                    }
                }
            }

            Map<String, String> appCheckSums = new HashMap<String, String>();

            for (Map.Entry<String, MessageDigest> entry : appDigests.entrySet()) {
                MessageDigest md = entry.getValue();
                String algorithm = entry.getKey();
                byte[] mdbytes = md.digest();
                //convert the byte to hex format
                StringBuilder sb = new StringBuilder();
                for (byte mdbyte : mdbytes) {
                    sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
                }
                appCheckSums.put(algorithm, sb.toString());
            }

            this.fileCheckSumsByAlgorithm = results;
            this.appCheckSumsByAlgorithm = appCheckSums;
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String s : files) {
                sb.append(sep).append(s);
                sep = "\n";
            }
            filesIncluded = sb.toString();
        } catch (IOException ioe) {
            logger.error("Unable to discover maven artifacts", ioe);
        } catch (SAXException e) {
            logger.error("Unable to discover maven artifacts", e);
        } catch (XPathExpressionException e) {
            logger.error("Unable to discover maven artifacts", e);
        }
    }

    // package private for testing purposes
    List<File> findNonJdkJars() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        Enumeration<URL> manifests = LibraryVersions.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        String javaHome = System.getProperty("java.home");
        List<File> ret = new LinkedList<File>();
        while (manifests.hasMoreElements()) {
            URL u = manifests.nextElement();
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
                File f = new File(file);
                String canonicalPath = f.getCanonicalPath();
                if (!canonicalPath.startsWith(javaHome)) {
                    ret.add(f);
                }
            } else {
                logger.info("Can't include code in checksum which isn't in a JAR: " + u);
            }
        }
        return ret;
    }

    public void setAlgorithms(String algorithms) {
        this.algorithms = algorithms.split(",");
    }

    @ManagedAttribute
    public String[] getAlgorithmsArray() {
        return algorithms;
    }

    public Map<String, Map<String, String>> getFileCheckSumsByAlgorithm() {
        return fileCheckSumsByAlgorithm;
    }

    @Override
    public Object getAttribute(String attribute) {
        if (FILES_INCLUDED_ATTRIBUTE.equals(attribute)) {
            return filesIncluded;
        }

        final int index = attribute.indexOf(SEP);
        if (index == -1) {
            return "UNKNOWN";
        }

        String algorithm = attribute.substring(index + SEP.length());
        if (attribute.startsWith(APPLICATION_CHECKSUM)) {
            return appCheckSumsByAlgorithm.get(algorithm);
        }


        String fileName = attribute.substring(0, index);
        String value = fileCheckSumsByAlgorithm.get(algorithm).get(fileName);
        if (value != null) {
            return value;
        }

        return "UNKNOWN";
    }

    @Override
    public void setAttribute(Attribute attribute) throws
            AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        // not supported
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList ret = new AttributeList();
        for (String s : attributes) {
            ret.add(new Attribute(s, getAttribute(s)));
        }
        return ret;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return attributes;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws
            MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        List<MBeanAttributeInfo> attributes = new ArrayList();
        for (Map.Entry<String, Map<String, String>> entry : fileCheckSumsByAlgorithm.entrySet()) {
            String algorithm = entry.getKey();
            final Map<String, String> fileCheckSums = entry.getValue();
            for (String fileName : fileCheckSums.keySet()) {
                attributes.add(new MBeanAttributeInfo(getKey(algorithm, fileName), "java.lang.String", "", true, false, false));
            }
        }

        for (Map.Entry<String, String> entry : appCheckSumsByAlgorithm.entrySet()) {
            attributes.add(new MBeanAttributeInfo(getKey(entry.getKey(), APPLICATION_CHECKSUM), "java.lang.String", "", true, false, false));
        }

        attributes.add(new MBeanAttributeInfo(FILES_INCLUDED_ATTRIBUTE, "java.lang.String", "", true, false, false));
        return new MBeanInfo(getClass().getName(), "", attributes.toArray(new MBeanAttributeInfo[]{}), new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
    }

    private String getKey(String algorithm, String fileName) {
        return fileName + SEP + algorithm;
    }

    public Map<String, String> getAppCheckSumsByAlgorithm() {
        return appCheckSumsByAlgorithm;
    }
}
