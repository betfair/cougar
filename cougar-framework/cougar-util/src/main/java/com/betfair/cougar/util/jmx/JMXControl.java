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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.jmx.export.MBeanExporter;

/**
 * A utility class which allows code to publish MBeans via Spring at run-time.
 * <p>
 * When using, have to ensure that instance is obtained at right time in Spring lifecycle, to avoid
 * race conditions (publishing beans before MBeanServer started up) or circular dependencies.
 * <p>
 * Look at usage of static util methods in this class for examples of how to obtain an instance
 * at run-time (typically via a Spring
 * {@link ApplicationListener#onApplicationEvent(ApplicationEvent)} event.
 */
public class JMXControl {

	/**
	 * Name of bean in Spring config
	 */
	public static final String BEAN_JMX_CONTROL = "jmxControl";

	private final MBeanExporter exporter;

    public JMXControl(MBeanExporter exporter) {
    	this.exporter = exporter;
    }

    public void registerMBean(String objectName, Object bean) {
    	try {
    		ObjectName oName = new ObjectName(objectName);
    		exporter.registerManagedResource(bean, oName);
    	} catch (MalformedObjectNameException e) {
    		throw new IllegalStateException("Unable to register MBean", e);
    	}
    }

    /**
     * Utility to retrieve a JMXControl bean from the given application context.
     * <p>
     * This is intended to be used by client code that needs to store a reference to the bean, but
     * can't take the bean as an up-front injected dependency, because it would cause circular
     * references. Can call this method after getting the context via a post-initialisation event.
     */
    public static JMXControl getFromContext(ApplicationContext ctx) {
    	// will throw an RTE if it's not there, which we can live with
    	return (JMXControl) ctx.getBean(BEAN_JMX_CONTROL);
    }

    /**
     * Return JMXControl bean from the ApplicationContext *if* the given event is
     * an {@link ApplicationContextEvent} (or null otherwise).
     * <p>
     * A utility method wrapping one way in which we'd call
     * {@link #getFromContext(ApplicationContext)}.
     */
    public static JMXControl getFromEvent(ApplicationEvent event) {
    	if (event instanceof ApplicationContextEvent) {
    		return getFromContext(((ApplicationContextEvent) event).getApplicationContext());
    	}
    	else {
    		return null;
    	}
    }
}
