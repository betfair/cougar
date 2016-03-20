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

package com.betfair.cougar.core.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.betfair.cougar.core.api.CougarSpringCtxFactory;
import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.impl.logging.CougarLog4JBootstrap;
import com.betfair.cougar.core.impl.logging.LogBootstrap;
import com.betfair.cougar.core.impl.logging.NullLogBootstrap;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.util.configuration.PropertyLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * A factory that creates and starts cougar application server. Can be used to start cougar application server as a
 * stand-alone java application or to start it as an embedded component, e.g. from a servlet-based web application.
 * <p/>
 * Examples:
 * To start cougar as a standalone app just call:
 * new CougarSpringCtxFactoryImpl().create(null);
 * <p/>
 * To integrate cougar with existing servlet-based web application, create custom ServletContextListener and call:
 * WebApplicationContext parentCtx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
 * new CougarSpringCtxFactoryImpl().create(parentCtx);
 */
public class CougarSpringCtxFactoryImpl implements CougarSpringCtxFactory {
    public static final String LOGGING_BOOTSTRAP_CLASS_PROPERTY = "cougar.core.log.bootstrap.class";

    public static final Class DEFAULT_COUGAR_LOG_INIT_CLASS = CougarLog4JBootstrap.class;

    private static final String CONFIG_PREFIX = "conf";

    public ClassPathXmlApplicationContext create() {
        return create(null);
    }

    @Override
    public ClassPathXmlApplicationContext create(ApplicationContext parentCtx) {
        logInitialisation(System.getProperty(LOGGING_BOOTSTRAP_CLASS_PROPERTY));
        ClassPathXmlApplicationContext context=null;
        try {

            List<String> configs = getConfigs();

            if(parentCtx==null){
                context= new ClassPathXmlApplicationContext(configs.toArray(new String[configs.size()]));
            }else{
               context=new ClassPathXmlApplicationContext(configs.toArray(new String[configs.size()]), parentCtx);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(CougarSpringCtxFactoryImpl.class).error("",e);
            throw new PanicInTheCougar(e);
        }
        return context;
    }

    protected List<String> getConfigs() throws IOException {
        List<String> configs = new ArrayList<String>();

        Enumeration<URL> bootstraps = Main.class.getClassLoader().getResources(CONFIG_PREFIX + "/cougar-bootstrap-spring.xml");
        while (bootstraps.hasMoreElements()) {
            configs.add(bootstraps.nextElement().toExternalForm());
        }

        URL core = Main.class.getClassLoader().getResource(CONFIG_PREFIX + "/cougar-core-spring.xml");
        if (core == null) {
            throw new IllegalStateException("Cannot find Cougar Core definition");
        }
        configs.add(core.toExternalForm());

        Enumeration<URL> modules = Main.class.getClassLoader().getResources(CONFIG_PREFIX + "/cougar-module-spring.xml");
        while (modules.hasMoreElements()) {
            configs.add(modules.nextElement().toExternalForm());
        }

        Enumeration<URL> applications = Main.class.getClassLoader().getResources(CONFIG_PREFIX + "/cougar-application-spring.xml");
        while (applications.hasMoreElements()) {
            configs.add(applications.nextElement().toExternalForm());
        }
        return configs;
    }

    public void logInitialisation(String loggingBootstrapClassName) {
        Class logBootstrapClass = establishLogInitialisationClass(loggingBootstrapClassName);
        runLogInitialisation(logBootstrapClass);
    }

    public void runLogInitialisation(Class logBootstrapClass) {

        try {
            //Construct a set of resources to attempt to load initial log config from
            Resource defaultConfig = new ClassPathResource(CONFIG_PREFIX + "/cougar-core-defaults.properties");
            PropertyLoader pl = new PropertyLoader(defaultConfig, "overrides.properties");
            //Build a merged properties file that contains the above as well as System properties
            Properties properties = pl.buildConsolidatedProperties();

            LogBootstrap logBootstrap = (LogBootstrap)logBootstrapClass.newInstance();
            //fire it up with the full set of merged properties
            logBootstrap.init(properties);
        } catch (Exception ex) {
            System.err.println("An error occurred initialising the logger. Ensure the value of property [" +
                    LOGGING_BOOTSTRAP_CLASS_PROPERTY +
                    "] points to a class that the implements LogBootstrap interface or is set to \"none\"");
            ex.printStackTrace();
            throw new PanicInTheCougar(ex);
        }

    }

    public Class establishLogInitialisationClass(String loggingBootstrapClassName) {
        Class logBootstrapClass;

        //Decision tree about the log initialisation is as follows:
        //1. If the log name is "none" then do no initialisation whatsoever
        //2. If there is a specific class name then attempt to load that. If that fails, write something to STDERR
        //3. Otherwise load the log4j log init

        if (loggingBootstrapClassName != null) {
            if (loggingBootstrapClassName.equals("none")) { //If this condition fails, consumer is indicating they'll use their own log impl
                logBootstrapClass = NullLogBootstrap.class;
            } else {
                try {
                    logBootstrapClass = Class.forName(loggingBootstrapClassName);
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("Unable to resolve logging initialisation class: [" +
                            loggingBootstrapClassName + "] falling back to default log4j initialisation for logging");
                    logBootstrapClass = DEFAULT_COUGAR_LOG_INIT_CLASS;
                }
            }
        } else {
            logBootstrapClass = DEFAULT_COUGAR_LOG_INIT_CLASS;
        }
        return logBootstrapClass;
    }
}
