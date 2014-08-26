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

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;

import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyConfigurerTest {

    private static final String JMX_PORT_KEY = "jmx.html.port";
    private static final String HOSTNAME_KEY = "system.hostname";
    private static final String NODEID_KEY = "cougar.core.nodeid";

	private PropertyConfigurer pc;
	private Resource mockResource = mock(Resource.class);

    @Before
    public void setUp()throws Exception {
		pc = new PropertyConfigurer();
		pc.setBeanFactory(mock(BeanFactory.class));
		pc.setBeanName("BeanName");
		pc.setConfigHost("ConfigHost");
		pc.setConfigOverride("ConfigOverride");
		pc.setDefaultConfig(mockResource);
		pc.setPlaceholderPrefix("placeholderPrefix");
	}

    @Test
	public void testOrder() {
		assertEquals(Integer.MAX_VALUE, pc.getOrder());
	}

    @Test
	public void testPostProcess() {
		final ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBeanDefinitionNames()).thenReturn(new String[0]);
		pc.postProcessBeanFactory(beanFactory);
	}

    @Test
	public void testAfterPropertiesSet_NoConfigHost() throws Exception {
		System.getProperties().remove("ConfigHost");
		pc.afterPropertiesSet();
	}

    @Test
	public void testAfterPropertiesSet_ConfigHostSet() throws Exception {
		System.getProperties().setProperty("ConfigHost", "TEST");
		pc.afterPropertiesSet();
	}

    @Test
	public void testRemoteConfigurationFailure() throws Exception {
		System.getProperties().setProperty("ConfigHost", "http://this.url.does.not.and.should.not.exist");
		pc.afterPropertiesSet();
	}

    @Test
    public void testNodeIdInitialisation(){
        Properties props = System.getProperties();
        props.setProperty(HOSTNAME_KEY,"TEST_HOST_NAME");

        // Try to initialize node id without setting the jmx port property - not defined
        PropertyConfigurer.CougarNodeId.initialiseNodeId(props);
        assertNull(props.getProperty(NODEID_KEY));

        // Set jmx port property to ignore value, try to initialize the node id - not defined
        props.setProperty(JMX_PORT_KEY,"-1");
        PropertyConfigurer.CougarNodeId.initialiseNodeId(props);
        assertNull(props.getProperty(NODEID_KEY));

        // Set jmx port property, try to initialize the node id - defined
        props.setProperty(JMX_PORT_KEY,"_WITH_TEST_PORT");
        PropertyConfigurer.CougarNodeId.initialiseNodeId(props);
        assertEquals("TEST_HOST_NAME_WITH_TEST_PORT", props.getProperty(NODEID_KEY));
    }
}
