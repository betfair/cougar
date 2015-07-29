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

package com.betfair.cougar.util.configuration;

import org.slf4j.Logger;
import com.betfair.cougar.logging.records.SimpleLogRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * This helper class is used to facilitate layered property loading.
 */
public class PropertyLoader {
    private static final String DEFAULT_CONFIG_HOST_PROPERTY = "betfair.config.host";
    private static final String DEFAULT_CONFIG_HOST_PROPERTY_VALUE = "/conf/";
    private static final String COUGAR_APPLICATION_PROPERTIES_FILE = "cougar-application.properties";

    private Logger logger;

    private Resource defaultConfig;
    private Resource appProperties;
    private String configOverride;

   public PropertyLoader(Resource defaultConfig, String configOverride) {
        this(defaultConfig, configOverride, null);
    }

    public PropertyLoader(Resource defaultConfig, String configOverride, Logger logger) {
        this.defaultConfig = defaultConfig;
        this.appProperties = new ClassPathResource(COUGAR_APPLICATION_PROPERTIES_FILE);
        this.configOverride = configOverride;
        this.logger = logger;
    }

    /**
     * This will realise the set of resources returned from @see constructResourcesList and
     * as a fully populated properties object, including System properties
     * @return returns a fully populated Properties object with values from the config, the override and the System (in that order of precedence)
     * @throws IOException
     */
    public Properties buildConsolidatedProperties() throws IOException {
        //Cannot use the spring classes here because they a) use the logger early, which
        //scuppers log4j and b) they're designed to do bean value overriding - not to be
        //used directly in this fashion
        Properties properties = new Properties();

        //Read them from the list of resources then mix in System
        for (Resource r : constructResourceList()) {
            try (InputStream is = r.getInputStream()) {
                properties.load(is);
            }
        }
        //System
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            properties.setProperty(propertyName, System.getProperty(propertyName));
        }
        return properties;
    }


    /**
     * @return returns an array of validated Resources for use with overlaid property files
     */
    public Resource[] constructResourceList() {
        String configHost = System.getProperties().getProperty(DEFAULT_CONFIG_HOST_PROPERTY);
        if (configHost == null) {
            log("No config Host defined - assuming " + DEFAULT_CONFIG_HOST_PROPERTY_VALUE);
            configHost = DEFAULT_CONFIG_HOST_PROPERTY_VALUE;
        }
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource configOverrideResource = loader.getResource(configHost + configOverride);

        return handleConfig(defaultConfig, appProperties, configOverrideResource);

    }

    private Resource[] handleConfig(Resource defaultConfig, Resource appProperties, Resource configOverrideResource) {
        Resource[] resourceList;

        /**
         * We need to check to see if the resources exists before we actually configure them as we need to
         * log whether we found the resources or not. If we don't check this, Cougar will fail to start if
         * configuration is missing.
         */
        if(configOverrideResource.exists()){
            if(appProperties.exists()){
                resourceList = new Resource[] { defaultConfig, appProperties, configOverrideResource };
                log("loading properties from  {}, {} and {}", defaultConfig, appProperties,  configOverrideResource);
            }
            else{
                resourceList = new Resource[] { defaultConfig, configOverrideResource };
                log("loading properties from  {} and {}", defaultConfig, configOverrideResource);
            }
        }
        else{
            if(appProperties.exists()){
                resourceList = new Resource[] { defaultConfig, appProperties };
                log("unable to load override file {}, loading properties from {} and {} ", configOverrideResource, defaultConfig, appProperties);
            }
            else{
                resourceList = new Resource[] { defaultConfig };
                log("unable to load override file {}, loading properties from {} ", configOverrideResource, defaultConfig);
            }
        }

        return resourceList;
    }

    private void log(String message, Object... args) {
        if (logger != null) {
            logger.info(message, args);
        } else {
            //This is a last ditch effort to say something useful before the logging
            //superstructure is initialized
            SimpleLogRecord record = new SimpleLogRecord("", Level.INFO, message, args);
            System.out.println(record.getMessage());
        }
    }
}
