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

package com.betfair.cougar.core.impl.jmx;

import com.betfair.cougar.util.configuration.PropertyConfigurer;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@ManagedResource
public class ApplicationProperties {

	private volatile Map<String, String> properties = null;

    private Map<String, String> getProperties() {
        if (properties == null) {
            properties = Collections.synchronizedMap(
                    new HashMap<String, String>(PropertyConfigurer.getAllLoadedProperties()));
        }
        return properties;
    }

	@ManagedOperation(description="Lists all application properties")
	public String listProperties() {
        StringBuffer stringBuffer = new StringBuffer();

        for (Entry<String, String> entry : getProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.toLowerCase().contains("password")) {
                value = "*****";
            }

            stringBuffer.append(key).append("=").append(value).append("<br>");
        }
        return stringBuffer.toString();
	}

    @ManagedOperation(description="Returns application property for key")
    public String getProperty(String key) {
        return getProperties().get(key);
    }
}
