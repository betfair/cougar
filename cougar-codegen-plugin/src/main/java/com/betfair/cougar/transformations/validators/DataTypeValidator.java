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

package com.betfair.cougar.transformations.validators;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import com.betfair.cougar.codegen.ValidationException;

public class DataTypeValidator extends AbstractValidator{
	private Set<String> namesUsed = new HashSet<String>();

	@Override
	public boolean nodeMustExist() {
		return false;
	}

	@Override
	public String getName() {
		return "Data Type Validator";
	}

	@Override
	public String getXPath() {
		return "/interface/dataType";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		// Check that the data type name starts with a upper case letter
         String name = getAttribute(getName(), node, "name");
         if (name == null || name.length() < 1) {
        	 throw new ValidationException("Data types must have a name", node);
         } else if (Character.isLowerCase(name.charAt(0))) {
        	 throw new ValidationException("Data types must start with a capital letter", node);
         } else if (namesUsed.contains(name)) {
 			throw new ValidationException("The data type " + name + " is already defined", node);
 		}
         namesUsed.add(name);
	}
}
