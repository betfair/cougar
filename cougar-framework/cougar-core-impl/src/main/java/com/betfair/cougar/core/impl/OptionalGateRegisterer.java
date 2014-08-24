package com.betfair.cougar.core.impl;

import com.betfair.cougar.core.api.CougarStartingGate;
import com.betfair.cougar.core.api.GateListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;

/**
 * Allows registering of gate listeners from code which may be used with a standalone-ev, which doesn't have a
 * starting gate.
 * @see com.betfair.cougar.core.api.GateRegisterer
 */
public class OptionalGateRegisterer implements BeanFactoryAware, InitializingBean {

    private final String gateBeanName;
    private final GateListener[] listeners;
    private BeanFactory beanFactory;

    public OptionalGateRegisterer(String gateBeanName, GateListener... listeners) {

        Assert.notEmpty(listeners, "OptionalGateRegisterer has no listeners.");
        Assert.notNull(gateBeanName, "OptionalGateRegister has not had a gateBeanName set.");

        this.gateBeanName = gateBeanName;
        this.listeners = listeners;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            CougarStartingGate gate = (CougarStartingGate) beanFactory.getBean(gateBeanName);
            for (GateListener listener : listeners) {
                gate.registerStartingListener(listener);
            }
        }
        catch (NoSuchBeanDefinitionException e1) {
            // ignore
        }
    }
}
