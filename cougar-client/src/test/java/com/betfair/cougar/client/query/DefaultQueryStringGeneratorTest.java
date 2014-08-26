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

package com.betfair.cougar.client.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultQueryStringGeneratorTest {
	DefaultQueryStringGeneratorImpl qsg;
	Map<String, Object> queryParmMap;
    private TestMarshaller testMarshaller;

    private interface TestMarshaller {
        public String marshall(Object input);
    }

    @Before
	public void setup() {
        testMarshaller = Mockito.mock(TestMarshaller.class);
        Marshaller marshaller = new Marshaller() {

            @Override
            public void marshall(OutputStream outputStream, Object result, String encoding, boolean client) {
                try {
                    outputStream.write(testMarshaller.marshall(result).getBytes());
                } catch (IOException ex) {
                    fail("exception marshalling");
                }
            }

            @Override
            public String getFormat() {
                return "";
            }
        };

		qsg = new DefaultQueryStringGeneratorImpl(marshaller);

		queryParmMap = new LinkedHashMap<String, Object>();
	}

	@Test
	public void testNoQueryString() {
		String result = qsg.generate(queryParmMap);
		assertEquals("", result);
	}

	@Test
	public void testOneArgument() {
		queryParmMap.put("name", "value");
        when(testMarshaller.marshall(eq("value"))).thenReturn("value");
		String result = qsg.generate(queryParmMap);
		assertEquals("?name=value", result);
	}

	@Test
	public void testTwoArguments() {
		queryParmMap.put("name", "value");
		queryParmMap.put("name2", "value2");

        when(testMarshaller.marshall(eq("value"))).thenReturn("value");
        when(testMarshaller.marshall(eq("value2"))).thenReturn("value2");

		String result = qsg.generate(queryParmMap);
		assertEquals("?name=value&name2=value2", result);
	}

    @Test
    public void testDate() throws UnsupportedEncodingException {
        Date epochStart = new Date(0);
        queryParmMap.put("param", epochStart);

        String epochInJson = "1970-01-01T00:00:00.000Z";
        when(testMarshaller.marshall(eq(epochStart))).thenReturn(epochInJson);

        String result = qsg.generate(queryParmMap);
        assertEquals("?param="+ URLEncoder.encode(epochInJson, "utf-8"), result);
    }

	@Test
	public void testEmptyKey() {
		queryParmMap.put("", "value");
		try {
			qsg.generate(queryParmMap);
			fail();

		} catch (IllegalArgumentException e) {
			// pass
		}
	}

}
