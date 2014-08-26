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
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;

import com.betfair.cougar.codegen.ValidationException;

public class NameClashValidator extends AbstractValidator{
	private Set<String> classNamesUsed = new HashSet<String>();

    private static final String NAME="name";

	@Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Name Clash Validator";
	}

	@Override
	public String getXPath() {
		return "/interface";
	}


	@Override
	public void validate(Node node) throws ValidationException {
		// Need to get all operations, data types and exceptions end ensure there are no name clashes
		// between them and the actual classes generated.
		List<Node> operations = getChildrenWithName(getName(), node, "operation");
		for (Node op: operations) {
			String name = getAttribute(getName(), op, NAME);
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			// Add the SOAP wrapper class names
			addName(op, name+"Response");
			addName(op, name+"Request");
		}
		List<Node> dataTypes = getChildrenWithName(getName(), node, "dataType");
		for (Node dt: dataTypes) {
			String name = getAttribute(getName(), dt, NAME);
			addName(dt, name);

		}
		List<Node> simpleTypes = getChildrenWithName(getName(), node, "simpleType");
		for (Node st: simpleTypes) {
			String name = getAttribute(getName(), st, NAME);
			addName(st, name);

		}
		List<Node> exceptions = getChildrenWithName(getName(), node, "exceptionType");
		for (Node ex: exceptions) {
			String name = getAttribute(getName(), ex,NAME);
			addName(ex, name);
		}

	}

	private void addName(Node node, String className) throws ValidationException {
		if (!classNamesUsed.add(className)) {
			throw new ValidationException("The class name "+className+" is already used", node);
		}
	}
}
