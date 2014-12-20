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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@ManagedResource
public class HotSwitchableAddressResolver implements InitializingBean, ApplicationContextAware, NetworkAddressResolver {
    private volatile String resolver;
    private Map<String, NetworkAddressResolver> resolvers = new HashMap();
    private volatile NetworkAddressResolver currentResolver;
    private ApplicationContext applicationContext;
    private String defaultResolverName;

    public HotSwitchableAddressResolver(String defaultResolverName) {
        this.defaultResolverName = defaultResolverName;
    }

    private void setCurrentResolver(String resolver) {
        NetworkAddressResolver newResolver = resolvers.get(resolver);
        if (newResolver != null) {
            this.resolver = resolver;
            this.currentResolver = newResolver;
        } else {
            throw new RuntimeException("Unable to locate resolver with name " + resolver);
        }
    }

    @ManagedAttribute
    public String getResolver() {
        return resolver;
    }

    @ManagedAttribute
    public void setResolver(String resolver) {
        setCurrentResolver(resolver);
    }

    @Override
    @ManagedOperation
    public Set<String> resolve(String host) throws UnknownHostException {
        return currentResolver.resolve(host);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, NetworkAddressResolver> resolversInCtx = applicationContext.getBeansOfType(NetworkAddressResolver.class);
        if (resolversInCtx == null) {
            return;
        }

        for (Map.Entry<String, NetworkAddressResolver> entry : resolversInCtx.entrySet()) {
            resolvers.put(entry.getKey(), entry.getValue());
        }
        setCurrentResolver(defaultResolverName);
    }
}
