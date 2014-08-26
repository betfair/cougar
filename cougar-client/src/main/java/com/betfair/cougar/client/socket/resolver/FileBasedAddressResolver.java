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

package com.betfair.cougar.client.socket.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * A network address resolver, that resolves server endpoints
 * based on a config file
 */
@ManagedResource
public class FileBasedAddressResolver implements NetworkAddressResolver {
    private static Logger logger = LoggerFactory.getLogger(FileBasedAddressResolver.class);

    private String configFileLocation;
    private volatile boolean enabled;

    public FileBasedAddressResolver(boolean enabled, String configFileLocation) {
        this.configFileLocation = configFileLocation;
        setEnabled(enabled);
    }

    @ManagedAttribute
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            logger.info("Loading network address configuration from : " + configFileLocation);
            if (configFileLocation == null || configFileLocation.trim().length() == 0) {
                throw new IllegalArgumentException("Host address configuration file location is undefined ");
            }

            File configFile = new File(configFileLocation);
            if (!configFile.exists() || !configFile.canRead()) {
                throw new IllegalArgumentException("Unable to locate or read host address configuration file : "
                        + configFileLocation);
            }
        }
    }

    @ManagedAttribute
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    @ManagedOperation
    public Set<String> resolve(String host) throws UnknownHostException {
        if (enabled) {
            final Map<String, Set<String>> resolvedAddresses = loadAddresses();

            Set<String> result = resolvedAddresses.get(host);

            if (result == null || result.isEmpty()) {
                throw new UnknownHostException(host);
            }

            return result;
        }
        else {
            return Collections.EMPTY_SET;
        }
    }

    private Map<String, Set<String>> loadAddresses() {
        Map<String, Set<String>> resolvedAddresses = new HashMap<String, Set<String>>();
        if (configFileLocation == null) {
            return resolvedAddresses;
        }
        File configFile = new File(configFileLocation);
        if (!configFile.exists() || !configFile.canRead()) {
            throw new IllegalArgumentException("Unable to locate or open host address configuration file : "
                    + configFileLocation);
        }

        FileInputStream fStream = null;
        try {
            fStream = new FileInputStream(configFile);
            Properties properties = new Properties();
            properties.load(fStream);
            for (Map.Entry hostEntry : properties.entrySet()) {
                String endpoint = String.valueOf(hostEntry.getKey());
                String addressListString = String.valueOf(hostEntry.getValue());
                String[] addresses = addressListString.split(",");
                Set<String> result = new HashSet<String>(addresses.length);
                for (String address : addresses) {
                    String addr = address.trim();
                    if(addr.length() > 0) {
                        result.add(addr);
                    }
                }
                resolvedAddresses.put(endpoint, result);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved hosts : " + resolvedAddresses);
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to load host address configuration file : "
                    + configFileLocation, ex);
        } finally {
            if (fStream != null) {
                try {
                    fStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return resolvedAddresses;
    }
}
