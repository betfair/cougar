/*
 * Copyright 2013, The Sporting Exchange Limited
 * Copyright 2015, Simon Matić Langford
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

import com.betfair.cougar.logging.CougarLoggingUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.betfair.baseline.v2.BaselineSyncClient;
import org.slf4j.LoggerFactory;

public class RescriptProtocolIDDVersionTest extends TestSuite {

	private ClassPathXmlApplicationContext springContext;


	@BeforeClass
	public void startCougarClient() {
        initSystemProperties();
		CougarLoggingUtils.setTraceLogger(null);
		TestClientContextFactory context = new TestClientContextFactory();
		springContext = (ClassPathXmlApplicationContext) context.create("conf/http-client-spring.xml");
		BaselineSyncClient baselineClient = (BaselineSyncClient) springContext.getBean("baselineClient");
		super.setBaselineClient(baselineClient);
	}


	@AfterClass
	public void stopCougarClient() {
		springContext.getBeanFactory().destroySingletons();
		springContext.stop();
	}

    @Override
    protected String getExpectedValidValueRemovedErrorCode() {
        return "DSC-0044";
    }


	public static void main(String[] args) {
		new RescriptProtocolIDDVersionTest().startCougarClient();
	}
}
