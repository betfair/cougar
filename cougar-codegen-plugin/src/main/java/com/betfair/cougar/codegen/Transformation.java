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

public class Transformation {
    //Enum to determine the output domain - some transformations are applicable to
    //Client, some for the Server, and some transformations are applicable to both
    //When the transformation is run, the POM of the IDL generation caller
    //can have an optional parameter specified to indicate whether its a client or not
    public static enum OutputDomain {
        Client,
        Server,
        Client_and_Server;

        public boolean isClient() {
            return this == Client || this == Client_and_Server;
        }

        public boolean isServer() {
            return this == Server || this == Client_and_Server;
        }
    }

    protected String template;
    protected String nodePath;
    protected String additionalNodesParameter;
    protected String directory;
    protected String fileName;
    protected boolean isJaxb;
    protected boolean compositeName;
    protected OutputDomain outputDomain;
    protected NodeExcluder excluder;


    public Transformation() {
        super();
    }

    public Transformation(	final String template,
    						final String nodePath,
                            final String additionalNodesParameter,
    						final String directory,
    						final String fileName,
    						final boolean isJaxb,
    						final boolean compositeName,
                            final OutputDomain outputDomain,
                            final NodeExcluder excluder) {
        this.template = template;
        this.nodePath = nodePath;
        this.additionalNodesParameter = additionalNodesParameter;
        this.directory = directory;
        this.fileName = fileName;
        this.isJaxb = isJaxb;
        this.compositeName = compositeName;
        this.outputDomain = outputDomain;
        this.excluder = excluder;
    }

    public Transformation(	final String template,
    						final String nodePath,
    						final String directory,
    						final String fileName,
    						final boolean isJaxb,
    						final boolean compositeName,
                            final OutputDomain outputDomain,
                            final NodeExcluder excluder) {
        this(template, nodePath, null, directory, fileName, isJaxb, compositeName, outputDomain, excluder);
    }

    public Transformation(	final String template,
    						final String nodePath,
    						final String directory,
    						final String fileName,
    						final boolean isJaxb,
    						final boolean compositeName,
                            final OutputDomain outputDomain) {
        this(template, nodePath, directory, fileName, isJaxb, compositeName, outputDomain, null);
    }

    public String getTemplate() {
        return template;
    }

    public String getNodePath() {
        return nodePath;
    }

    public String getDirectory() {
        return directory;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isJaxb() {
        return isJaxb;
    }

    public boolean isCompositeName() {
        return compositeName;
    }

    public String getAdditionalNodesParameter() {
        return additionalNodesParameter;
    }

	public boolean isClient() {
		return outputDomain.isClient();
	}

	public boolean isServer() {
		return outputDomain.isServer();
	}

    @Override
    public String toString() {
        return template + ":" +  nodePath +  ":" + directory +  ":" + fileName +  ":" + isJaxb;
    }
}
