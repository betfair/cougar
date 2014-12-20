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

import java.io.IOException;
import java.io.InputStream;

import com.betfair.cougar.logging.CougarLoggingUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.betfair.baseline.v2.BaselineSyncClient;
import com.betfair.cougar.client.socket.ExecutionVenueNioClient;
import org.slf4j.LoggerFactory;

public class BinaryProtocolIDDVersionTest extends TestSuite{

	private ClassPathXmlApplicationContext springContext;
	private DefaultHttpClient httpClient;


	@BeforeClass
	public void startCougarClient() throws ClientProtocolException, IOException, InterruptedException {
        initSystemProperties();
		httpClient = new DefaultHttpClient();
		setServerHealth(OK);
		CougarLoggingUtils.setTraceLogger(null); //because trace log is static and multiple spring contexts will try to set it
		TestClientContextFactory context = new TestClientContextFactory();
		springContext = (ClassPathXmlApplicationContext) context.create("conf/binary-client-spring.xml");
		BaselineSyncClient baselineClient = (BaselineSyncClient) springContext.getBean("baselineClient");
        //TODO get a better way to make sure there is an open session already before starting the test
        Thread.sleep(5000);
		super.setBaselineClient(baselineClient);
	}


	@AfterClass
	public void stopCougarClient() {
		((ExecutionVenueNioClient)springContext.getBean("socketTransport")).stop();
		springContext.getBeanFactory().destroySingletons();
		springContext.stop();
	}

	private void setServerHealth(String health) throws ClientProtocolException, IOException, InterruptedException {
		HttpPost post = new HttpPost("http://localhost:8080/www/cougarBaseline/v2.0/setHealthStatusInfo");
		post.setEntity(new StringEntity(health));
		post.setHeader("Content-type", "application/json");
		releaseConnection(httpClient.execute(post));

		HttpGet get = new HttpGet("http://localhost:8080/www/healthcheck/v2.0/detailed?&alt=xml");
		releaseConnection(httpClient.execute(get));
		Thread.sleep(1000);

	}

	private void releaseConnection(HttpResponse response) throws IllegalStateException, IOException {
		response.getStatusLine();
		InputStream is = response.getEntity().getContent();
		byte[] bytes = new byte[128];
		while (is.read(bytes) > 0);
	}

    @Override
    protected String getExpectedValidValueRemovedErrorCode() {
        return "DSC-0044";
    }

    private static String OK = "{ \"message\" : " +
	"								{ \"initialiseHealthStatusObject\" : \"true\", " +
	"								  \"DBConnectionStatusDetail\" : \"OK\", " +
	"								  \"cacheAccessStatusDetail\" : \"OK\", " +
	"								  \"serviceStatusDetail\" : \"OK\" } }";


}
