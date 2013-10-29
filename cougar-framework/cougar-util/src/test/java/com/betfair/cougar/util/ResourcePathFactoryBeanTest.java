package com.betfair.cougar.util;

import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.StaticApplicationContext;

import static junit.framework.Assert.assertTrue;


public class ResourcePathFactoryBeanTest {

    private static final String RESOURCE = "cougar_server_cert.jks";

    @Test
    public void simplePositiveCase() {
        StaticApplicationContext c = new StaticApplicationContext();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("path", "classpath:" + RESOURCE);
        c.registerSingleton("myResource", ResourcePathFactoryBean.class, pvs);
        c.refresh();

        // From the spring app context this value should come out as a string
        String path = (String) c.getBean("myResource");
        // You dont want to assert the full path but probably want to check if it has the file name in it
        assertTrue(path.endsWith(RESOURCE));
    }

    @Test(expected = BeanCreationException.class)
    public void simpleNegativeCaseResourceDoesNotExist() {
        StaticApplicationContext c = new StaticApplicationContext();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("path", "classpath:asdf.xml");
        c.registerSingleton("myResource", ResourcePathFactoryBean.class, pvs);
        c.refresh();

        // From the spring app context this value should come out as a string
        c.getBean("myResource");
    }

    @Test(expected = BeanCreationException.class)
    public void nullOrEmptyValueforPath() {
        StaticApplicationContext c = new StaticApplicationContext();
        c.registerSingleton("myResource", ResourcePathFactoryBean.class);
        c.refresh();

        // From the spring app context this value should come out as a string
        c.getBean("myResource");
    }
}
