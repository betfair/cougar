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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class IDLReader {

    private Configuration config;
    private NodeModel dataModel;
    private Log log;

    private File output ;
    private File iDDOutputDir;

    private String packageName;
    private String service;
    private boolean client;
    private boolean server;
    private String interfaceName;
    private String interfaceMajorVersion;
    private String interfaceMajorMinorVersion;

    private Node getRootNode(NodeModel model) {
        NodeList list = model.getNode().getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node n = list.item(i);
            if (n instanceof Element) {
                return n;
            }
        }
        throw new IllegalStateException("Can't find root node!");
    }

    // TODO this arg list is getting out of hand and could be rationalised
    public void init(
            Document iddDoc,
            Document extensionDoc,
            final String service,
            String packageName,
            final String basedir,
            final String genSrcDir,
            final Log log,
            final String outputDir,
            boolean client,
            boolean server)
            throws Exception {

        try {
            output = new File(basedir, genSrcDir);
            if (outputDir != null) {
                iDDOutputDir = new File(basedir+"/"+outputDir);
                if (!iDDOutputDir.exists()) {
                    if (!iDDOutputDir.mkdirs()) {
                        throw new IllegalArgumentException("IDD Output Directory "+iDDOutputDir+" could not be created");
                    }
                }
                if (!iDDOutputDir.isDirectory() || (!iDDOutputDir.canWrite())) {
                    throw new IllegalArgumentException("IDD Output Directory "+iDDOutputDir+" is not a directory or cannot be written to.");
                }
            }
            config = new Configuration();
            config.setClassForTemplateLoading(IDLReader.class, "/templates");

            config.setStrictSyntaxMode(true);
            this.log = log;
            this.packageName = packageName;
            this.service = service;
            this.client = client;
            this.server = server || !client; // server must be true if client if false.

            dataModel = NodeModel.wrap(iddDoc.cloneNode(true));

            if (extensionDoc != null) {
                NodeModel extensionModel = NodeModel.wrap(extensionDoc);
                mergeExtensionsIntoDocument(getRootNode(dataModel),
                        getRootNode(extensionModel));
                removeUndefinedOperations(getRootNode(dataModel),
                        getRootNode(extensionModel));
            }
            if(log.isDebugEnabled()) {
                log.debug(serialize());
            }
        } catch (final Exception e) {
            log.error("Failed to initialise FTL", e);
            throw e;
        }
    }

    public void mangle(DocumentMangler mangler) {
        // We want the root node, not the document
        final Node doc = getRootNode(dataModel);
        mangler.mangleDocument(doc);
    }

    public void validate(Validator validator) throws Exception {
        final XPathFactory factory = XPathFactory.newInstance();

        // We want the root node, not the document
        final Node doc = getRootNode(dataModel);
        final NodeList nodes = (NodeList) factory.newXPath().evaluate(validator.getXPath(), doc, XPathConstants.NODESET);
        if (nodes.getLength() == 0 && validator.nodeMustExist()) {
            throw new ValidationException("Node "+validator.getXPath()+" must exist according to "+validator.getName(), null);
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            validator.validate(nodes.item(i));
        }
    }

    public void runMerge(List<Transformation> definitions) throws Exception {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xp = factory.newXPath();

        // We want the root node, not the document
        final Node doc = getRootNode(dataModel);
        runTransformation(xp, doc, definitions);
    }

    public void writeResult() throws Exception {
        if (iDDOutputDir != null) {
            FileWriter fw = new FileWriter(new File(iDDOutputDir, service+".idd"));
            fw.write(serialize());
            fw.close();
        }
    }

    private void runTransformation(final XPath xp, final Node doc, List<Transformation> transformations)
            throws Exception {
        String serviceName = getAttribute(doc, "name");
        interfaceName = serviceName;
        final String pkg = (packageName!=null)?packageName.replaceAll("\\.","/"):serviceName.toLowerCase();
        // Can't have dots in a package name, so replace with underscores
        interfaceMajorMinorVersion = "v" + getAttribute(doc, "version").replaceAll("\\.", "_");
        // Ensure that the interfaceMajorMinorVersion is only vMajor.Minor, not vMajor.Minor.Revision
        if (interfaceMajorMinorVersion.matches("v\\d+_\\d+_\\d+")) {
            interfaceMajorMinorVersion = interfaceMajorMinorVersion.substring(0, interfaceMajorMinorVersion.lastIndexOf("_"));
        }
        interfaceMajorVersion = interfaceMajorMinorVersion;
        // Ensure that the interfaceMajorVersion is only vMajor, not vMajor.Minor
        if (interfaceMajorVersion.matches("v\\d+_\\d+")) {
            interfaceMajorVersion = interfaceMajorVersion.substring(0, interfaceMajorVersion.lastIndexOf("_"));
        }

        // Prepare the directories. This must be done before the generation loop
        // as otherwise the additive files overwrite each other (jaxb.index)
        for (final Transformation trans : transformations) {
            if (isActive(trans)) {
                prepareDirectory(pkg, interfaceMajorVersion, interfaceMajorMinorVersion, trans);
            }
        }

        for (final Transformation trans : transformations) {
            log.debug(trans.toString());
            log.debug(trans.getNodePath());
            log.debug(trans.getAdditionalNodesParameter());

            if (isActive(trans)) {
                final XPathExpression expr = xp.compile(trans.getNodePath());
                final NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                // find the xpath to give to the template as a parameter
                int parameterColonIndex = trans.getAdditionalNodesParameter() != null ? trans.getAdditionalNodesParameter().indexOf(":") : -1;
                List<NodeModel> parameterNodes = null;
                String parameterName = null;
                if (parameterColonIndex > 0) {
                    parameterName = trans.getAdditionalNodesParameter().substring(0, parameterColonIndex);
                    final XPathExpression paramExpr = xp.compile(trans.getAdditionalNodesParameter().substring(parameterColonIndex+1));
                    parameterNodes = new ArrayList<NodeModel>();
                    NodeList parameterNodeList = (NodeList) paramExpr.evaluate(doc, XPathConstants.NODESET);
                    for (int i=0; i<parameterNodeList.getLength(); i++) {
                        parameterNodes.add(NodeModel.wrap(parameterNodeList.item(i)));
                    }
                }

                String dirName = getDirectory(pkg, interfaceMajorVersion, interfaceMajorMinorVersion, trans);

                log.debug("" + nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    String nodeName = getName(node, trans.isCompositeName());

                    //Determine if an excluder implementation has been specified,
                    //and if it has, visit that node to allow the user implementation
                    //to exclude it
                    if (trans.excluder == null || !trans.excluder.exclude(xp, node)) {
                        writeTemplate(trans, NodeModel.wrap(doc), NodeModel.wrap(node), parameterName, parameterNodes, pkg, serviceName, interfaceMajorVersion, interfaceMajorMinorVersion, nodeName, dirName);
                    }
                }
            } else {
                log.debug(trans.toString() +" is inactive for this run - client: "+client
                        + ", trans.isServer(): "+trans.isServer()
                        + ", trans.isClient(): "+trans.isClient());
            }
        }
    }

    private boolean isActive(Transformation trans) {
        return (server && trans.isServer()) || (client && trans.isClient());
    }

    private void writeTemplate(final Transformation trans, final NodeModel topLevelDoc, final NodeModel data, final String paramName, final List<NodeModel> paramNodes, final String pkg, final String serviceName, final String majorVersion, final String majorMinorVersion, String name, String dirName)
            throws Exception {
        log.debug(trans.toString());
        String nsVersion = majorVersion;
        String namespace = "http://www.betfair.com/servicetypes/"+nsVersion+"/"+serviceName+"/";
        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("interface", topLevelDoc);
        root.put("doc", data);
        root.put("package", pkg.replaceAll("/", "\\."));
        root.put("majorVersion", majorVersion);
        root.put("majorMinorVersion", majorMinorVersion);
        root.put("name", name);
        root.put("namespace", namespace);
        if (paramName != null) {
            if (paramNodes.size() == 1) {
                root.put(paramName, paramNodes.get(0));
            }
            else {
                root.put(paramName, paramNodes);
            }
        }


        final File outputDirectory = new File(output, dirName);
        name = name.trim();
        name = capFirst(name);
        final String fileName = trans.getFileName().replace("${name}", name);
        final String className = fileName.replace(".java", "");
        File outputFile = new File(outputDirectory, fileName);
        FileWriter fw = new FileWriter(outputFile);
        root.put("className", className);

        log.debug("Writing " + name + " using template " + trans.getTemplate() + " to file " + outputFile);
        try {
            // First thing is to deal with the service
            final Template template = config.getTemplate(trans.getTemplate());
            template.process(root, fw);
        } finally {
            fw.close();
            //write the jaxb.index file
            if(trans.isJaxb()) {
                outputFile = new File(outputDirectory, "jaxb.index");
                fw = new FileWriter(outputFile,true);
                fw.append(className).append("\n");
                fw.close();
                outputFile = new File(outputDirectory, "package-info.java");
                fw = new FileWriter(outputFile);
                fw.write("@javax.xml.bind.annotation.XmlAccessorOrder(javax.xml.bind.annotation.XmlAccessOrder.UNDEFINED)\n");
                fw.write("@javax.xml.bind.annotation.XmlSchema(namespace=\""+namespace+"\",elementFormDefault=javax.xml.bind.annotation.XmlNsForm.QUALIFIED)\n");
                fw.write("package "+dirName.replaceAll("/", ".")+";");
                fw.close();
            }
        }
    }

    private String capFirst(String name) {
        final char firstChar = name.charAt(0);
        if(!Character.isUpperCase(firstChar)) {
            name = Character.toUpperCase(firstChar) + name.substring(1);
        }
        return name;
    }

    private String getAttribute(final Node node, final String attr) {
        try {
            return node.getAttributes().getNamedItem(attr).getTextContent();
        } catch (final Exception e) {
            return null;
        }
    }

    private String getName(final Node node, final boolean compositeName) {
        Node currentNode = node;

        String nodeName = getAttribute(currentNode, "name");
        while (nodeName == null) {
            currentNode = currentNode.getParentNode();
            nodeName = getAttribute(currentNode, "name");
        }
        if (compositeName) {
            // we need to go up again to get the second part of the composite name
            currentNode = currentNode.getParentNode();
            String nodeNameStart = getAttribute(currentNode, "name");
            while (nodeNameStart == null) {
                currentNode = currentNode.getParentNode();
                nodeNameStart = getAttribute(currentNode, "name");
            }
            nodeName =
                    Character.toLowerCase(nodeNameStart.charAt(0)) +
                            nodeNameStart.substring(1) +
                            Character.toUpperCase(nodeName.charAt(0)) +
                            nodeName.substring(1);
        }
        return nodeName;

    }

    private void prepareDirectory(final String pkg, final String majorVersion, final String majorMinorVersion,
                                  final Transformation trans) throws IOException {
        // Deal with the output file
        String dirName = trans.getDirectory().replace("${package}", pkg);
        dirName = dirName.replace("${majorVersion}", majorVersion);
        dirName = dirName.replace("${majorMinorVersion}", majorMinorVersion);
        final File outputDirectory = new File(output, dirName);
        log.debug("Preparing directory "+outputDirectory.getCanonicalPath());
        if (!outputDirectory.exists()) {
            boolean success = outputDirectory.mkdirs();
            if(!success) {
                throw new IOException("Could not create directory " + outputDirectory);
            }
        }
        File outputFile = new File(outputDirectory, "jaxb.index");
        FileWriter fw = new FileWriter(outputFile);
        fw.close();
    }

    private String getDirectory(final String pkg, final String majorVersion, final String majorMinorVersion,
                                final Transformation trans) {
        // Deal with the output file
        String dirName = trans.getDirectory().replace("${package}", pkg);
        dirName = dirName.replace("${majorVersion}", majorVersion);
        dirName = dirName.replace("${majorMinorVersion}", majorMinorVersion);
        return dirName;
    }

    /**
     * Weave the extensions defined in the extensions doc into the target.
     */
    private void mergeExtensionsIntoDocument(Node target, Node extensions) throws Exception {
        final XPathFactory factory = XPathFactory.newInstance();
        final NodeList nodes = (NodeList) factory.newXPath().evaluate("//extensions", extensions, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node extensionNode = nodes.item(i);
            String nameBasedXpath =  DomUtils.getNameBasedXPath(extensionNode, false);
            log.debug("Processing extension node: " + nameBasedXpath);

            final NodeList targetNodes = (NodeList) factory.newXPath().evaluate(nameBasedXpath, target, XPathConstants.NODESET);
            if (targetNodes.getLength() != 1) {
                throw new IllegalArgumentException("XPath "+nameBasedXpath+" not found in target");
            }
            Node targetNode = targetNodes.item(0);
            Node importedNode = targetNode.getOwnerDocument().importNode(extensionNode, true);
            targetNode.appendChild(importedNode);
        }
    }

    /**
     * Cycle through the target Node and remove any operations not defined in the extensions document.
     */
    private void removeUndefinedOperations(Node target, Node extensions) throws Exception {
        final XPathFactory factory = XPathFactory.newInstance();
        final NodeList nodes = (NodeList) factory.newXPath().evaluate("//operation", target, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node targetNode = nodes.item(i);
            String nameBasedXpath = DomUtils.getNameBasedXPath(targetNode, true);
            log.debug("Checking operation: " + nameBasedXpath);

            final NodeList targetNodes = (NodeList) factory.newXPath().evaluate(nameBasedXpath, extensions, XPathConstants.NODESET);
            if (targetNodes.getLength() == 0) {
                // This operation is not defined in the extensions doc
                log.debug("Ignoring IDL defined operation: " + getAttribute(targetNode, "name"));
                targetNode.getParentNode().removeChild(targetNode);
            }
        }
    }


    public String serialize() throws Exception {

        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer = tfactory.newTransformer();
        //Setup indenting to "pretty print"
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.transform(new DOMSource(dataModel.getNode()), new StreamResult(out));
        return out.toString();
    }

    public String getInterfaceMajorVersion() {
        return interfaceMajorVersion;
    }

    public String getInterfaceMajorMinorVersion() {
        return interfaceMajorMinorVersion;
    }

    public String getInterfaceName() {
        return interfaceName;
    }
}
