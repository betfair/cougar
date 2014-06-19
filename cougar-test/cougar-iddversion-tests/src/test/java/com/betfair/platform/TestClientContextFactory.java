/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.betfair.cougar.logging.CougarLoggingUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.betfair.cougar.core.api.exception.PanicInTheCougar;
import com.betfair.cougar.core.impl.CougarSpringCtxFactoryImpl;
import org.slf4j.LoggerFactory;

public class TestClientContextFactory extends CougarSpringCtxFactoryImpl{

	public ClassPathXmlApplicationContext create(String config) {
        logInitialisation(System.getProperty(LOGGING_BOOTSTRAP_CLASS_PROPERTY));
        try {
            List<String> configs = getConfigs();
            Iterator<String> it = configs.iterator();
            while (it.hasNext()) {
                String next = it.next();
                if (next.contains("socket-transport")) {
                    it.remove();
                }
            }

            Enumeration<URL> appConfis = TestClientContextFactory.class.getClassLoader().getResources(config);
            while (appConfis.hasMoreElements()) {
                configs.add(appConfis.nextElement().toExternalForm());
            }


            return new ClassPathXmlApplicationContext(configs.toArray(new String[configs.size()]));
        } catch (Exception e) {
            LoggerFactory.getLogger(CougarSpringCtxFactoryImpl.class).error("",e);
            throw new PanicInTheCougar(e);
        }
	}

}
