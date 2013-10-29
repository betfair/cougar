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
