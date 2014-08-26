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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.spring2.properties.EncryptablePropertyPlaceholderConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropertyConfigurer implements BeanFactoryAware, BeanNameAware, BeanFactoryPostProcessor, Ordered, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(PropertyConfigurer.class);

    public static final String NO_DEFAULT = "MUST_BE_OVERRIDDEN";

	private static final String DEFAULT_CONFIG_HOST_PROPERTY = "betfair.config.host";
    private static final String JMX_PORT_KEY = "jmx.html.port";
    private static final String HOSTNAME_KEY = "system.hostname";
    private static final String NODEID_KEY = "cougar.core.nodeid";

	public static final String HOSTNAME;

	private static Map<String, String> allLoadedProperties = Collections.synchronizedMap(new TreeMap<String, String>());

	private final PropertyPlaceholderConfigurer propertyPlaceholderConfigurer;
	private String configHostProp = DEFAULT_CONFIG_HOST_PROPERTY;
	private Resource defaultConfig;
	private String configOverride;

	static {
    	try {
    	    HOSTNAME = InetAddress.getLocalHost().getHostName();
	    }
	    catch (java.net.UnknownHostException e) {
	        throw new IllegalArgumentException("Unable to generate name of local host", e);
        }
	}
	public PropertyConfigurer() {
		this((StringEncryptor)null);
	}
    public PropertyConfigurer(EncryptorRegistry registry) {
        this(registry.getEncryptor());
    }
	protected PropertyConfigurer(StringEncryptor encryptor) {
		this.propertyPlaceholderConfigurer = encryptor != null ? new EncryptablePropertyPlaceholderConfigurer(encryptor) : new PropertyPlaceholderConfigurer();

		// Ensure that system properties override the spring-set properties.
		propertyPlaceholderConfigurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);

		PropertiesPersister savingPropertiesPersister = new DefaultPropertiesPersister() {

            @Override
            public void load(Properties props, InputStream is) throws IOException {
                props.put(HOSTNAME_KEY, HOSTNAME);
                CougarNodeId.initialiseNodeId(props);
                super.load(props, is);
                for (String propName: props.stringPropertyNames()) {
                    allLoadedProperties.put(propName, System.getProperty(propName, props.getProperty(propName)));
                }
            }};
        propertyPlaceholderConfigurer.setPropertiesPersister(savingPropertiesPersister);
	}

	public static Map<String, String> getAllLoadedProperties() {
	    return allLoadedProperties;
	}

    public void setPlaceholderPrefix(String placeholderPrefix) {
		this.propertyPlaceholderConfigurer.setPlaceholderPrefix(placeholderPrefix);
	}

	public void setConfigHost(String configHost) {
		this.configHostProp = configHost;
	}

	public void setDefaultConfig(Resource defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	public void setConfigOverride(String configOverride) {
		this.configOverride = configOverride;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		propertyPlaceholderConfigurer.setBeanFactory(beanFactory);

	}

	@Override
	public void setBeanName(String beanName) {
		propertyPlaceholderConfigurer.setBeanName(beanName);

	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0) throws BeansException {
		propertyPlaceholderConfigurer.postProcessBeanFactory(arg0);

	}

	@Override
	public int getOrder() {
		return propertyPlaceholderConfigurer.getOrder();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        PropertyLoader pl = new PropertyLoader(defaultConfig, configOverride, LOG);
        propertyPlaceholderConfigurer.setLocations(pl.constructResourceList());
	}

    // Inner class containing the logic for defining the cougar.core.nodeId property
    public static class CougarNodeId{

         // Create a unique node id for the service by combining the host name and jmx port
         // If no jmx port is defined then leave the node id property undefined
        public static void initialiseNodeId(Properties props){
            if(props.containsKey(JMX_PORT_KEY) && !("-1").equals(props.get(JMX_PORT_KEY))){
                props.put(NODEID_KEY, (String)props.get(HOSTNAME_KEY)+props.get(JMX_PORT_KEY));
            }
        }
    }
}
