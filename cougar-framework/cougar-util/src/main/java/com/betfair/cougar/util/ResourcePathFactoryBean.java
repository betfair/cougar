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

package com.betfair.cougar.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * This class is typically used if a library class takes in a "String" which is the file system path of the resource
 * and you don't want to configure/supply the full path.
 *
 * Basically this brings the "Resource" abstraction to the libraries which occasionally use "String" and treat it
 * as a file system path
 *
 * Will not work for "non URL" based resources
 *
 * ex: Cougar HttpClient -> com.betfair.cougar.client.HttpClientExecutable
 */
public class ResourcePathFactoryBean implements FactoryBean {

    private Resource path;

    @Override
    public Object getObject() throws Exception {
        return path.getURL().getPath();
    }

    @Override
    public Class getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Required
    public void setPath(Resource path) {
        this.path = path;
    }
}
