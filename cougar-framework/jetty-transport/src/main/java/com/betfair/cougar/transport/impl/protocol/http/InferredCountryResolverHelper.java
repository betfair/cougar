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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.api.security.InferredCountryResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
/**
 * A helper class to wire in an implementation inferred country resolver
 * into http command processors, if one is available in the spring context
 */

public class InferredCountryResolverHelper implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;


    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, DefaultExecutionContextResolverFactory> resolverFactories =
                applicationContext.getBeansOfType(DefaultExecutionContextResolverFactory.class);

        Map<String, InferredCountryResolver> inferredCountryResolvers =
                applicationContext.getBeansOfType(InferredCountryResolver.class);

        if(inferredCountryResolvers == null) {
            return;
        }

        // todo: sml: something not right here, you can only have 1 of these, but they're tied to the transport of the protocol..
        if (inferredCountryResolvers.size() > 1) {
            throw new RuntimeException("Invalid configuration. Found more than one inferred country resolvers.");
        }

        if(inferredCountryResolvers.size() > 0) {
            InferredCountryResolver resolver = inferredCountryResolvers.values().iterator().next();

            for ( DefaultExecutionContextResolverFactory factory : resolverFactories.values()) {
                factory.setInferredCountryResolver(resolver);
            }
        }
    }
}
