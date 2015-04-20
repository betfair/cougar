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

package com.betfair.cougar.util.monitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.OnDemandMonitor;
import com.betfair.tornjak.monitor.Status;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.logging.Level;

public class JMXMonitor extends OnDemandMonitor {
    private static Logger LOGGER = LoggerFactory.getLogger(JMXMonitor.class);

    static Logger setLogger(Logger logger) {
        Logger tmp = LOGGER;
        LOGGER = logger;
        return tmp;
    }

    private final MBeanServer mBeanServer;
    private final ObjectName beanName;
    private final String attributeName;
    private final IsHealthyExpression isHealthyExpression;
    private final boolean ignoreIfBeanMissing;
    private final Status failState;

    private Status currentStatus = Status.OK;

    private final String name;

    public interface IsHealthyExpression {
        boolean evaluate(Object value);
    }

    public JMXMonitor(
            MonitorRegistry registry,
            MBeanServer mBeanServer,
            String beanName,
            String attributeName,
            String expectedAttributeValue,
            boolean ignoreIfBeanMissing) throws MalformedObjectNameException {

        this(registry,
                mBeanServer,
                beanName,
                attributeName,
                new IsExpectedAttributeExpression(expectedAttributeValue),
                ignoreIfBeanMissing);
    }

    public JMXMonitor(
            MonitorRegistry registry,
            MBeanServer mBeanServer,
            String beanName,
            String attributeName,
            String expectedAttributeValue,
            boolean ignoreIfBeanMissing,
            Status failState) throws MalformedObjectNameException {

        this(registry,
                mBeanServer,
                beanName,
                attributeName,
                new IsExpectedAttributeExpression(expectedAttributeValue),
                ignoreIfBeanMissing,
                failState);
    }

    public JMXMonitor(
            MonitorRegistry registry,
            MBeanServer mBeanServer,
            String beanName,
            String attributeName,
            IsHealthyExpression isHealthyExpression,
            boolean ignoreIfBeanMissing) throws MalformedObjectNameException {
        this(registry,
                mBeanServer,
                beanName,
                attributeName,
                isHealthyExpression,
                ignoreIfBeanMissing,
                Status.FAIL);
    }

    public JMXMonitor(
            MonitorRegistry registry,
            MBeanServer mBeanServer,
            String beanName,
            String attributeName,
            IsHealthyExpression isHealthyExpression,
            boolean ignoreIfBeanMissing,
            Status failState) throws MalformedObjectNameException {

        if (failState == Status.OK) {
            throw new IllegalArgumentException("Fail state may not be OK");
        }
        this.mBeanServer = mBeanServer;
        this.beanName = new ObjectName(beanName);
        this.attributeName = attributeName;
        this.isHealthyExpression = isHealthyExpression;
        this.ignoreIfBeanMissing = ignoreIfBeanMissing;
        this.failState = failState;

        name = "JMX monitor checking "+attributeName+" on " + beanName;
        LOGGER.info(name + "Registering Monitor: " + name);
        registry.addMonitor(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Status checkStatus() {
        if (isFailState()) {
            if (currentStatus != failState) {
                LOGGER.warn("{} {}. Attribute {} is unavailable or incorrect", beanName, failState, attributeName);
            }
            currentStatus = failState;
        } else {
            if (currentStatus != Status.OK) {
                LOGGER.info("{} recovered. Attribute {} is OK", beanName, attributeName);
            }
            currentStatus = Status.OK;
        }
        return currentStatus;
    }


    private boolean isFailState() {
        try {
            if (!mBeanServer.isRegistered(beanName)) {
                if (ignoreIfBeanMissing) {
                    LOGGER.debug("{} missing - ignoring", beanName);
                    return false;
                } else {
                    LOGGER.debug("{} missing - failing", beanName);
                    return true;
                }
            }
            Object value = mBeanServer.getAttribute(beanName, attributeName);
            LOGGER.debug("retrieved values {} from bean {}", value, beanName);
            return !isHealthyExpression.evaluate(value);
        } catch (Exception e) {
            LOGGER.warn("Exception retrieving MBean value " + attributeName + "  from bean " + beanName, e);
            return true;
        }
    }

    private static class IsExpectedAttributeExpression implements IsHealthyExpression {

        private String expectedAttribute;

        private IsExpectedAttributeExpression(String expectedAttribute) {
            this.expectedAttribute = expectedAttribute;
        }

        @Override
        public boolean evaluate(Object value) {
            return String.valueOf(value).equals(expectedAttribute);
        }

    }
}
