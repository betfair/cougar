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

package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.cougar.test.CougarTestCase;
import org.junit.Ignore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;


public class XMLUnMarshallerTest extends CougarTestCase {

	public void testUnmarshall() {
		UnMarshaller unMarshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getUnMarshaller();
		TestClassUnmarshall tc = (TestClassUnmarshall) unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>foo</message></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
		assertEquals("foo", tc.message);

		tc = (TestClassUnmarshall) unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>bar</message></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
		assertEquals("bar", tc.message);
	}

    // This should be fixed in the future, but it's not high enough priority to do in this release.
    @Ignore
    public void ignoredTestUnmarshallExtraField() {
        UnMarshaller unMarshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getUnMarshaller();
        TestClassUnmarshall tc = (TestClassUnmarshall) unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>foo</message><xtra>bar</xtra></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
        assertEquals("foo", tc.message);

        tc = (TestClassUnmarshall) unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>bar</message><xtra>bar</xtra></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
        assertEquals("bar", tc.message);
    }

	public void testUnmarshallWithEnum() {
		UnMarshaller unMarshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getUnMarshaller();
		TestClassUnmarshallEnum tc = (TestClassUnmarshallEnum) unMarshaller.unmarshall(new ByteArrayInputStream("<TestEnum><message>foo</message></TestEnum>".getBytes()),TestClassUnmarshallEnum.class,"UTF-8", false);
		assertEquals(TestUnmarshallEnum.foo, tc.message);

		try {
			unMarshaller.unmarshall(new ByteArrayInputStream("<TestEnum><message>bar</message></TestEnum>".getBytes()),TestClassUnmarshallEnum.class,"UTF-8", false);
		} catch (CougarMarshallingException e) {
			assertTrue(e.getMessage().contains("bar"));
		}
		try {
			unMarshaller.unmarshall(new ByteArrayInputStream("<TestEnum><message></message></TestEnum>".getBytes()),TestClassUnmarshallEnum.class,"UTF-8", false);
			fail();
		} catch (CougarMarshallingException e) {
			assertTrue(e.getMessage().contains("foo"));
		}

	}

	public void testUnmarshallWithInt() {
		UnMarshaller unMarshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getUnMarshaller();
		TestClassUnmarshallInt tc = (TestClassUnmarshallInt) unMarshaller.unmarshall(new ByteArrayInputStream("<TestInt><message>1</message></TestInt>".getBytes()),TestClassUnmarshallInt.class,"UTF-8", false);
		assertEquals(1, tc.message);

		try {
			unMarshaller.unmarshall(new ByteArrayInputStream("<TestInt><message>bar</message></TestInt>".getBytes()),TestClassUnmarshallInt.class,"UTF-8", false);
			fail();
		} catch (CougarMarshallingException e) {
			assertTrue(e.getMessage().contains("bar"));
		}

	}

	public void testUnmarshallBadFormat() {
		UnMarshaller unMarshaller = new XMLDataBindingFactory(new JdkEmbeddedXercesSchemaValidationFailureParser()).getUnMarshaller();
		try {
			unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>foo<message></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
			fail();
		} catch (CougarMarshallingException e) {
			assertTrue(e.getMessage().contains("message"));

		}
		try {
			unMarshaller.unmarshall(new ByteArrayInputStream("<Test><message>foo</message><fish>plaice</fish></Test>".getBytes()),TestClassUnmarshall.class,"UTF-8", false);
			fail();
		} catch (CougarMarshallingException e) {
			assertTrue(e.getMessage().contains("fish"));
		}

	}

	@XmlRootElement(name="Test")
	public static class TestClassUnmarshall {
		private String message;

		@XmlElement
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@XmlRootElement(name="TestEnum")
	public static class TestClassUnmarshallEnum {
		private TestUnmarshallEnum message;

		@XmlElement
		public TestUnmarshallEnum getMessage() {
			return message;
		}

		public void setMessage(TestUnmarshallEnum message) {
			this.message = message;
		}
	}

	@XmlRootElement(name="TestInt")
	public static class TestClassUnmarshallInt {
		private int message;

		@XmlElement
		public int getMessage() {
			return message;
		}

		public void setMessage(int message) {
			this.message = message;
		}
	}

	public enum TestUnmarshallEnum {foo};
}
