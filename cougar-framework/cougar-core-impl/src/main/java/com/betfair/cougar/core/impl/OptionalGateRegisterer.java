/*
 * Copyright 2014, Simon Matić Langford
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
