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

package com.betfair.cougar.transport.activemq.monitoring;

import org.apache.activemq.ActiveMQConnection;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.jms.JMSException;
import java.util.Arrays;

/**
 *
 */
@ManagedResource
public class ManagedActiveMQConnection {
    private ActiveMQConnection connection;

    public ManagedActiveMQConnection() {
    }

    public void setConnection(ActiveMQConnection connection) {
        this.connection = connection;
    }

    // todo: there's probably more to go in here..

    @ManagedAttribute
    public String getClientID() throws JMSException {
        return connection == null ? "No Open Connection" : connection.getClientID();
    }

    @ManagedAttribute
    public String getJMSVersion() throws JMSException {
        return connection == null ? "No Open Connection" : connection.getMetaData().getJMSVersion();
    }

    @ManagedAttribute
    public String getJMSMajorVersion() throws JMSException {
        return connection == null ? "No Open Connection" : String.valueOf(connection.getMetaData().getJMSMajorVersion());
    }

    @ManagedAttribute
    public String getJMSMinorVersion() throws JMSException {
        return connection == null ? "No Open Connection" : String.valueOf(connection.getMetaData().getJMSMinorVersion());
    }

    @ManagedAttribute
    public String getJMSProviderName() throws JMSException {
        return connection == null ? "No Open Connection" : connection.getMetaData().getJMSProviderName();
    }

    @ManagedAttribute
    public String getProviderVersion() throws JMSException {
        return connection == null ? "No Open Connection" : connection.getMetaData().getProviderVersion();
    }

    @ManagedAttribute
    public String getProviderMajorVersion() throws JMSException {
        return connection == null ? "No Open Connection" : String.valueOf(connection.getMetaData().getProviderMajorVersion());
    }

    @ManagedAttribute
    public String getProviderMinorVersion() throws JMSException {
        return connection == null ? "No Open Connection" : String.valueOf(connection.getMetaData().getProviderMinorVersion());
    }



}
