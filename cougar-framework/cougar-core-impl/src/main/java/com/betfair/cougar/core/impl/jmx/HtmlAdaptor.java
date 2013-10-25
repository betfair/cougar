/*
 * Copyright 2013, The Sporting Exchange Limited
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
import com.betfair.cougar.core.api.jmx.JMXHttpParserReader;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.sun.jdmk.comm.AuthInfo;
import org.springframework.beans.factory.InitializingBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.logging.Level;

public class HtmlAdaptor implements InitializingBean, JMXHttpParserReader {
	private static final CougarLogger logger = CougarLoggingUtils.getLogger(HtmlAdaptor.class);
    private static final String PARSER_NAME = "CoUGAR.internal:name=HtmlAdaptorParser";

    private final TlsHtmlAdaptorServer htmlAdaptor;
	private String name = "CoUGAR:name=HtmlAdaptor";
    private String username;
	private String password;
	private boolean httpExport = true;
	private MBeanServer mBeanServer;
	private HtmlAdaptorParser htmlParser;

    public HtmlAdaptor(final TlsHtmlAdaptorServer htmlAdaptor) {
        this.htmlAdaptor = htmlAdaptor;
    }

    public void afterPropertiesSet() throws Exception {
		if (httpExport) {
			ObjectName adaptorObjectName = new ObjectName(name);
			if(mBeanServer.isRegistered(adaptorObjectName)){
				mBeanServer.unregisterMBean(adaptorObjectName);
			}
	        mBeanServer.registerMBean(htmlAdaptor, adaptorObjectName);
			if (username != null && username.length() > 0 &&
			        password != null && password.length() > 0) {
			    logger.log(Level.INFO, "JMX HTML interface password protected");
	    		htmlAdaptor.addUserAuthenticationInfo(new AuthInfo(username, password));
			} else {
		          logger.log(Level.INFO, "JMX HTML interface password NOT protected");
			}

			ObjectName parserObjectName = new ObjectName(PARSER_NAME);
			if(mBeanServer.isRegistered(parserObjectName)){
				mBeanServer.unregisterMBean(parserObjectName);
			}
			htmlParser = new HtmlAdaptorParser(mBeanServer);
			addCustomParser(new ThreadDumper());
			mBeanServer.registerMBean(htmlParser, parserObjectName);
			htmlAdaptor.setParser(parserObjectName);

            String protocol = htmlAdaptor.isTlsEnabled() ? "https" : "http";
			logger.log(Level.INFO, "Starting JMX HTML interface on "+protocol+"://localhost:%d/", htmlAdaptor.getPort());
			htmlAdaptor.start();

	        // Create a shutdown hook to close the jmx port cleanly
	        Runtime.getRuntime().addShutdownHook(new Thread("JMX HTTP Shutdown Hook") {

				@Override
				public void run() {
					logger.log(Level.INFO, "Gracefully shutting down JMX http server");
					try {
						htmlAdaptor.stop();
					} catch (Exception e) {
						logger.log(Level.WARNING, "Failed to shutdown JMX http server", e);
					}
				}});

		} else {
			logger.log(Level.INFO, "JMX HTML interface not started");
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMBeanServer(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public void setHttpExport(boolean httpExport) {
		this.httpExport = httpExport;
	}

    @Override
    public void addCustomParser(JMXHttpParser parser) {
    	if (httpExport) {
			logger.log(Level.INFO, "JMX HTML custom parser added for path %s", parser.getPath());
    		htmlParser.addCustomParser(parser);
    	} else {
			logger.log(Level.INFO, "No JMX HTML interface - parser ignored");

    	}
    }
}
