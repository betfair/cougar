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

import java.util.ArrayList;
import java.util.List;

import com.betfair.cougar.CougarUtilTestCase;
import com.betfair.cougar.api.Validatable;

public class ValidationUtilsTest extends CougarUtilTestCase {
	public void testValidateNull() {
		ValidationUtils.validateMandatory(null);
	}

	public void testValidateValidatable() {
		ValidationUtils.validateMandatory(new MyValidatable());
	}

	public void testValidateValidatableFail() {
		IllegalArgumentException e = new IllegalArgumentException();
		try {
			ValidationUtils.validateMandatory(new MyValidatable(e));
			fail();
		} catch (IllegalArgumentException thrown) {
			assertTrue(thrown == e);
		}
	}

	public void testValidateSimpleList() {
		List<String> list = new ArrayList<String>();
		list.add("foo");
		list.add("bar");
		ValidationUtils.validateMandatory(list);
	}

	public void testValidateValidatableList() {
		List<Validatable> list = new ArrayList<Validatable>();
		list.add(new MyValidatable());
		list.add(new MyValidatable());
		ValidationUtils.validateMandatory(list);
	}

	public void testValidateValidatableListFail() {
		IllegalArgumentException e = new IllegalArgumentException();
		List<Validatable> list = new ArrayList<Validatable>();
		list.add(new MyValidatable());
		list.add(new MyValidatable(e));
		try {
			ValidationUtils.validateMandatory(list);
			fail();
		} catch (Exception thrown) {
			assertTrue(thrown == e);
		}
	}

	private static class MyValidatable implements Validatable {
		IllegalArgumentException ex;

		private MyValidatable() {
		}

		private MyValidatable(IllegalArgumentException ex) {
			this.ex = ex;
		}


		@Override
		public void validateMandatory() {
			if (ex != null) {
				throw ex;
			}
		}};
}
