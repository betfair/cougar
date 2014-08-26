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

import java.util.List;

import org.w3c.dom.Node;

import com.betfair.cougar.codegen.ValidationException;

public class MapsValidator extends AbstractValidator {

	@Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Maps Validator";
	}

	@Override
	public String getXPath() {
		return "//dataType";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		List<Node> values = getChildrenWithName(getName(), node, "parameter");
		for (Node val: values) {
			String type = getAttribute(getName(), val, "type");
			if (type == null) {
				throw new ValidationException(getName() + " - type not defined", node);
			}
			if(type.startsWith("map(")) {
				String[] types = type.split("[()]");
				String[] mapTypes = types[1].split(",");
				checkTypeIsSimple(getName(), node, mapTypes[0].trim(), false);
			}
		}
	}


}
