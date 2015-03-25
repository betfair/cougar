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

import com.betfair.cougar.codegen.except.PluginException;
import com.betfair.cougar.codegen.resolver.DefaultSchemaCatalogSource;
import com.betfair.cougar.codegen.resolver.InterceptingResolver;
import com.betfair.cougar.codegen.resolver.SchemaCatalogSource;
import com.betfair.cougar.codegen.resource.ResourceLoader;
import com.betfair.cougar.transformations.CougarTransformations;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;


/**
 * A plugin which is responsible for generating Cougar-based services. This encompasses a number
 * of code- and file-generation steps, as well as validation. The intention is for this mojo to
 * do everything needed, keeping the plugins/plugin section of a Cougar service's pom as simple as
 * possible.
 * <p>
 * <h2>NOTE: idd dependencies</h2>
 * IDDs can be read from the file system or as resources. The IDD is expected to be named after the
 * service (see {@link #services} param), suffixed with {@code .xml} for the service and
 * {@code -Extensions.xml} for the extensions definition. If you're using an IDD file then it should
 *  be in {@code /src/main/resources}. Switch between the two modes using the {@link #iddAsResource}
 *  flag.
 * <p>
 * A gotcha exists when accessing IDDs as resources.
 * Since the IDDs are not required at run-time, it would make sense to include the relevant IDD
 * project (jar) as a plugin dependency (ie. in {@code project/plugins/plugin/dependencies} as opposed to a
 * project dependency of {@code project/dependencies}). You can do this <em>unless</em> your
 * service is built as part of a larger project tree, in which multiple services are built. Maven
 * resolves dependencies for the plugin once, so you can't have projectA relying on projectA-idd
 * and project B relying on projectB-idd respectively - you end up with both projects relying on
 * (say) projectA-idd. To work around this, you have to include the IDD as part of the project
 * dependencies.
 * <p>
 * TODO If there's an easy way to fix this, we should do so (maven-savvy volunteers welcome)
 *
 * @goal process
 *
 * @phase generate-sources
 * @requiresDependencyResolution
 */
public class IdlToDSMojo extends AbstractMojo {

    private static final String RESOURCES_DIR = "src/main/resources";


	// =============================================================================================
	//	Mojo params
	// =============================================================================================

	//	this contains only those params which are mandatory or which need to be initialised by
	//	maven
	//
	//	it's not that hard to turn other class members into params, but if users don't need them
	//	and don't know about them, then the simpler things remain.
    //
    //  all access to these MUST be via the associated getters to enable subclasses to work

    /**
     * We may need the runtime classpath to access the idds if we're doing resource-based loading.
     * @parameter default-value="${project.runtimeClasspathElements}"
     * @readonly
     */
    private List<String> runtimeClassPath;

    protected List<String> getRuntimeClassPath() {
        return runtimeClassPath;
    }

	/**
     * name of service.
     * @parameter
     * @required
     */
    private Service[] services;

    protected Service[] getServices() {
        return services;
    }

    /**
     * the base directory of the project
     * @parameter default-value="${basedir}"
     */
    private String baseDir ;

    protected String getBaseDir() {
        return baseDir;
    }

    /**
     * Either {@code mvn -o} or in settings.xml
     *
     *  @parameter expression="${settings.offline}"
     */
    private boolean offline;

    protected boolean isOffline() {
        return offline;
    }

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    protected MavenProject getProject() {
        return project;
    }

    /**
     * If set to true, generate a client version of generated code.
     * @parameter
     */

    private boolean client;

    protected boolean isClient() {
        return client;
    }

    /**
     * If set to true, generate a server version of generated code.
     * @parameter
     */

    private boolean server;

    protected boolean isServer() {
        return server;
    }

    /**
     * Read IDDs and files from the file system (as opposed to as resources). Use as transition
     * for existing services, and also as a way to make writing/testing new services a bit simpler.
     *
     * @parameter
     */
    private boolean iddAsResource = false;

    protected boolean isIddAsResource() {
        return iddAsResource;
    }


    /**
     * Legacy mode exception validation. Allows for an exception parameter to be called message. Note
     * this is a nuisance as it obfuscates conventional exception class behaviour, so this method
     * of operation should be phased out
     * @parameter
     */
    private boolean legacyExceptionParamValidation = false;

    protected boolean isLegacyExceptionParamValidation() { return legacyExceptionParamValidation; }

    // =============================================================================================
	//	POJO stuff
	// =============================================================================================

    /**
     * Location of generated sources (relative to baseDir)
     *
     * This could be a property, but as noted above, not making it one until we need to
     */
    private String generatedSourceDir = "/target/generated-sources/java";

    /**
     * Location (resource) of the wsdl style sheet to be used
     */
    private String wsdlXslResource = "wsdl-xsl/wsdl.xsl";

    /**
     * Location (resource) of the xsd style sheet to be used
     */
    private String xsdXslResource = "xsd-xsl/xsd.xsl";

     /**
     * File location of the on-disk iddstripper.csl, relative to base directory
     */
    private String iddStripperXslResource = "bsidl/iddstripper.xsl";

    /**
     * File location of the on-disk wsdl.xsl, relative to base directory
     */
    private String iddStripperXslFile = "target/wrk/iddstripper.xsl";

    /**
     * File location of the on-disk wsdl.xsl, relative to base directory
     */
    private String wsdlXslFile = "target/wrk/wsdl.xsl";

    /**
     * File location of the on-disk wsdl.xsl, relative to base directory
     */
    private String xsdXslFile = "target/wrk/xsd.xsl";

    /**
     * Location for storing our catalog file and schemas for validation.
     */
    private String schemaDir = "target/wrk/schemas";

    /**
     * Actual file pointing to wsdl.xsl. Initialised by {@link #prepWsdlXsl()}.
     */
    private File wsdlXsl;

    /**
     * Actual file pointing to xsd.xsl. Initialised by {@link #prepXsdXsl()}.
     */
    private File xsdXsl;

    /**
     * Actual file pointing to iddstripper.xsl. Initialised by {@link #prepIddStripperXsl()}.
     */
    private File iddStripperXsl;

    /**
     * Actual file pointing to the catalog.xml used for validation.
     * Initialised by {@link #unwrapSchemas()}.
     */
    private File catalogFile = null;

	private InterceptingResolver resolver;

	private ResourceLoader resourceLoader;

	private XPathExpression namespaceExpr;


    public void execute()
        throws MojoExecutionException
    {
        getLog().info("Starting Cougar code generation");
        if (isOffline()) {
            getLog().warn("Maven in offline mode, plugin is NOT validating IDDs against schemas");
        }
        else {
        	getLog().debug("Unbundling schemas for validation");
        	catalogFile = unwrapSchemas();
        }
        initResourceLoader();
        initResolver();	// needs the resource loader

        // load wsdl.xsl (as resource) and write (as file) to a working directory
        prepWsdlXsl();

        // load xsd.xsl (as resource) and write (as file) to a working directory
        prepXsdXsl();

        // load iddstripper.xsl (as resource) and write (as file) to a working directory
        prepIddStripperXsl();


        try {
            getLog().debug("Starting IDL to Java");

            for (Service service : getServices() ) {
                processService(service);
            }

            // this replaces the functionality of build-helper-maven-plugin
            addSource();
        }
        catch (Exception e) {
        	getLog().error(e);
            throw new MojoExecutionException("Failed processing IDL: " + e, e);
        }

        getLog().info("Completed Cougar code generation");
    }

    private void prepIddStripperXsl() throws MojoExecutionException {
        try {
            iddStripperXsl = new File(getBaseDir(), iddStripperXslFile);
            initOutputDir(iddStripperXsl.getParentFile());
            writeIDDStylesheet(iddStripperXsl);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write local copy of IDD stripper stylesheet: " + e, e);
        }

    }

    /**
     * Read a wsdl.xsl (stylesheet) from a resource, and write it to a working directory.
     *
     * @return the on-disk wsdl.xsl file
     */
    private void prepWsdlXsl() throws MojoExecutionException {
        try {
            wsdlXsl = new File(getBaseDir(), wsdlXslFile);
            initOutputDir(wsdlXsl.getParentFile());
            writeWsdlStylesheet(wsdlXsl);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write local copy of WSDL stylesheet: " + e, e);
        }
    }

    /**
     * Read a wsdl.xsl (stylesheet) from a resource, and write it to a working directory.
     *
     * @return the on-disk wsdl.xsl file
     */
    private void prepXsdXsl() throws MojoExecutionException {
        try {
            xsdXsl = new File(getBaseDir(), xsdXslFile);
            initOutputDir(xsdXsl.getParentFile());
            writeXsdStylesheet(xsdXsl);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write local copy of XSD stylesheet: " + e, e);
        }
    }

    /**
     * Various steps needing to be done for each IDD
     */
    private void processService(Service service) throws Exception {

        getLog().info("  Service: " + service.getServiceName());

        Document iddDoc = parseIddFile(service.getServiceName());

        // 1. validate
        if (!isOffline()) {
            getLog().debug("Validating XML..");
            new XmlValidator(resolver).validate(iddDoc);
        }

        // 2. generate outputs
        generateJavaCode(service, iddDoc);
    }

    private void generateExposedIDD(Document iddDoc, String serviceName, String version) throws Exception {

        File iddFile = new File(getBaseDir(), "target/generated-resources/idd/" + serviceName+"_"+version.replace("_",".") + "_Exposed.idd");
        getLog().debug("Writing to idd file " + iddFile);
        initOutputDir(iddFile.getParentFile());

        ExposedIDDGenerator.transform(iddDoc, iddStripperXsl, iddFile);


    }

    /**
     * Find, open and parse the IDD implied by the specified service name. Reads either an explicit
     * file or else a resource based on {@link #iddAsResource} flag.
     * <p>
     * The implied name is simply the service name + ".xml".
     */
    private Document parseIddFile(String serviceName) {

		String iddFileName = serviceName + ".xml";

    	if (isIddAsResource()) {
    		InputStream is = resourceLoader.getResourceAsStream(iddFileName);
    		if (is == null) {
    			throw new RuntimeException("Cannot open IDD resource named '" + iddFileName + "'");
    		}
    		return XmlUtil.parse(is, resolver);
    	}
    	else {
    		File iddFile = new File( new File(getBaseDir(), RESOURCES_DIR), iddFileName);
    		if (!iddFile.exists()) {
    			throw new RuntimeException("Cannot open IDD file named '" + iddFileName + "'");
    		}
    		return XmlUtil.parse(iddFile, resolver);
    	}
	}

        /**
     * Find, open and parse the IDD implied by the specified service name. Reads either an explicit
     * file or else a resource based on {@link #iddAsResource} flag.
     * <p>
     * The implied name is simply the service name + ".xml".
     */
    private Document parseIddFromString(String iddContent) {

    	return XmlUtil.parse(new ByteArrayInputStream(iddContent.getBytes()), resolver);

	}

	/**
     * Find, open and parse the extensions xml file or null if it doesn't exist. Reads from
     * file or resource based on {@link #iddAsResource} flag.
     * <p>
     * Name of extensions file should be ServiceName-Extensions.xml.
     */
    private Document parseExtensionFile(String serviceName) {

		String extensionFileName = serviceName + "-Extensions.xml";

    	if (isIddAsResource()) {
    		InputStream is = resourceLoader.getResourceAsStream(extensionFileName);
    		if (is != null) {
    			return XmlUtil.parse(is, resolver);
    		}
    		else {
    			return null;
    		}
    	}
    	else {
    		File extensionsFile = new File( new File(getBaseDir(), RESOURCES_DIR), extensionFileName);
    		if (extensionsFile.exists()) {
        		return XmlUtil.parse(extensionsFile, resolver);
    		}
    		else {
    			return null;
    		}
    	}
    }

	/**
     * The original concept of the IDLReader (other devs) has gone away a bit, so there could be
     * some refactoring around this.
     */
	private void generateJavaCode(Service service, Document iddDoc) throws Exception {

		IDLReader reader = new IDLReader();

		Document extensionDoc = parseExtensionFile(service.getServiceName());

		String packageName = derivePackageName(service, iddDoc);

        reader.init(iddDoc, extensionDoc, service.getServiceName(), packageName, getBaseDir(),
        				generatedSourceDir, getLog(), service.getOutputDir(), isClient(), isServer());

        runMerge(reader);

        // also create the stripped down, combined version of the IDD doc
        getLog().debug("Generating combined IDD sans comments...");
        Document combinedIDDDoc = parseIddFromString(reader.serialize());
        // WARNING: this absolutely has to be run after a call to reader.runMerge (called by runMerge above) as otherwise the version will be null...
        generateExposedIDD(combinedIDDDoc, reader.getInterfaceName(), reader.getInterfaceMajorMinorVersion());

        // generate WSDL/XSD
        getLog().debug("Generating wsdl...");
        generateWsdl(iddDoc, reader.getInterfaceName(), reader.getInterfaceMajorMinorVersion());
        getLog().debug("Generating xsd...");
        generateXsd(iddDoc, reader.getInterfaceName(), reader.getInterfaceMajorMinorVersion());
	}

    /**
	 * Package name comes from explicit plugin param (if set), else the namespace definition, else
	 * skream and die.
	 * <p>
	 * Having the plugin override allows backwards compatibility as well as being useful for
	 * fiddling and tweaking.
	 */
	private String derivePackageName(Service service, Document iddDoc) {

		String packageName = service.getPackageName();
		if (packageName == null) {
			packageName = readNamespaceAttr(iddDoc);
			if (packageName == null) {
				throw new PluginException("Cannot find a package name "
								+ "(not specified in plugin and no namespace in IDD");
			}
		}
		return packageName;
	}

	private void generateWsdl(Document iddDoc, String serviceName, String version) throws Exception {

	    File wsdlFile = new File(getBaseDir(), "target/generated-resources/wsdl/" + serviceName +"_"+version.replace("_",".")+ ".wsdl");
	    getLog().debug("Writing to wsdl file " + wsdlFile);
	    initOutputDir(wsdlFile.getParentFile());

	    new XmlGenerator().transform(iddDoc, wsdlXsl, wsdlFile);
    }

	private void generateXsd(Document iddDoc, String serviceName, String version) throws Exception {

	    File xsdFile = new File(getBaseDir(), "target/generated-resources/xsd/" + serviceName +"_"+version.replace("_",".")+ ".xsd");
	    getLog().debug("Writing to xsd file " + xsdFile);
	    initOutputDir(xsdFile.getParentFile());

	    new XmlGenerator().transform(iddDoc, xsdXsl, xsdFile);
    }

    private void writeWsdlStylesheet(File xslFile) throws Exception {

	    if (wsdlXslResource == null) {
	        throw new MojoExecutionException("wsdl resource not specified");
	    }

	    FileUtil.resourceToFile(wsdlXslResource, xslFile, getClass());

        getLog().debug("Wrote wsdl stylesheet from resource " + wsdlXslResource + " to " + xslFile);
    }

    private void writeXsdStylesheet(File xslFile) throws Exception {

	    if (xsdXslResource == null) {
	        throw new MojoExecutionException("xsd resource not specified");
	    }

	    FileUtil.resourceToFile(xsdXslResource, xslFile, getClass());

        getLog().debug("Wrote xsd stylesheet from resource " + xsdXslResource + " to " + xslFile);
    }
    private void writeIDDStylesheet(File xslFile) throws Exception {

	    if (iddStripperXslResource == null) {
	        throw new MojoExecutionException("wsdl resource not specified");
	    }

	    FileUtil.resourceToFile(iddStripperXslResource, xslFile, getClass());

        getLog().debug("Wrote IDD stylesheet from resource " + iddStripperXslResource + " to " + xslFile);
    }

    private File unwrapSchemas() {

    	File dir = new File(getBaseDir(), schemaDir);
    	dir.mkdirs();
    	return getCatalogSource().getCatalog(dir, getLog());
	}

    protected Transformations getTransformations() {
        return new CougarTransformations(legacyExceptionParamValidation);
    }

    @SuppressWarnings("unchecked")
	private void runMerge(IDLReader reader) throws Exception {
        Transformations transformations = getTransformations();

        // First let's mangle the document if need be.
        if (transformations.getManglers() != null) {
        	getLog().debug("mangling IDL using "+transformations.getManglers().size()+" manglers");
        	for(DocumentMangler m : transformations.getManglers()) {
                getLog().debug(m.getName());
           	 	reader.mangle(m);
            }
        }

        if (transformations.getPreValidations() != null) {
        	getLog().debug("Pre validating IDL using "+transformations.getPreValidations().size()+" pre validations");
            for(Validator v : transformations.getPreValidations()) {
                getLog().debug(v.getName());
                reader.validate(v);
            }

        }

        for(Transformation t : transformations.getTransformations()) {
            getLog().debug(t.toString());
        }
        reader.runMerge(transformations.getTransformations());

        reader.writeResult();




	}

    /**
     * Set up and validate the creation of the specified output directory
     */
    private void initOutputDir(File outputDir) {

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IllegalArgumentException("Output Directory "+outputDir+" could not be created");
            }
        }
        if (!outputDir.isDirectory() || (!outputDir.canWrite())) {
            throw new IllegalArgumentException("Output Directory "+outputDir+" is not a directory or cannot be written to.");
        }
    }

    /**
     * Add the generated-sources directory to the classpath.
     * <p>
     * This one-liner is nicked from build-helper-maven-plugin v1.4, AddSourceMojo.java.
     */
    private void addSource() {

    	// TODO this should be shared between here and IDLReader
    	File generatedSources = new File(getBaseDir(), generatedSourceDir);

        this.getProject().addCompileSourceRoot( generatedSources.getAbsolutePath() );
        this.getLog().debug( "Source directory " + generatedSources + " added." );
	}

    private void initResolver() {

    	// catalogs aren't needed if we're offline because we don't validate
    	String[] catalogs = isOffline() ? new String[0] : new String[] { catalogFile.getAbsolutePath() };

    	resolver = new InterceptingResolver(getLog(), (isIddAsResource() ? resourceLoader : null), catalogs);
    }

    private void initResourceLoader() throws MojoExecutionException {

    	try {
        	if (isIddAsResource()) {
        		// we need this classLoader because it's the only way to get to the project dependencies
        		resourceLoader = new ResourceLoader(getRuntimeClassPath());
        	}
        	else {
        		resourceLoader = new ResourceLoader();
        	}
    	}
    	catch (Exception e) {
    		throw new MojoExecutionException("Error initialising classloader: " + e, e);
    	}
    }

    private XPathExpression initNamespaceAttrExpression() {

		XPathFactory xfactory = XPathFactory.newInstance();
		XPath xpath = xfactory.newXPath();
		try {
			return xpath.compile("/interface/@namespace");
		} catch (XPathExpressionException e) {
			throw new PluginException("Error compiling namespace XPath expression: " + e, e);
		}
    }

    /**
     * Retrieve 'namespace' attr of interface definition or null if not found
     */
    private String readNamespaceAttr(Document doc) {

    	// lazy loading is mostly pointless but it keeps things together
    	if (namespaceExpr == null) {
    		namespaceExpr = initNamespaceAttrExpression();
    	}

		String s;
		try {
			s = namespaceExpr.evaluate(doc);
		} catch (XPathExpressionException e) {
			throw new PluginException("Error evaluating namespace XPath expression: " + e, e);
		}
		// xpath returns an empty string if not found, null is cleaner for callers
		return (s == null || s.length() == 0) ? null : s;
    }

    /**
     * For tests
     */
    void setBaseDir(String s) {
        this.baseDir = s;
    }

    /**
     * For tests
     */
    void setWsdlXslResource(String s) {
        this.wsdlXslResource = s;
    }

    /**
     * For tests
     */
    void setXsdXslResource(String xsdXslResource) {
        this.xsdXslResource = xsdXslResource;
    }

    /**
     * For tests
     */
    void setServices(Service[] services) {//NOSONAR
        this.services = services;
    }

    protected SchemaCatalogSource getCatalogSource() {
        return new DefaultSchemaCatalogSource();
    }
}

