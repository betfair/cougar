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

import com.betfair.cougar.codegen.ValidationException;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Requires that versions only be of the form x.y.
 */
public class InterfaceVersionValidator extends AbstractValidator{
	private Set<String> namesUsed = new HashSet<String>();

	@Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Interface Version Validator";
	}

	@Override
	public String getXPath() {
		return "/interface";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		// Check that the data type name starts with a upper case letter
        String version = getAttribute(getName(), node, "version");
        if (version == null || version.length() < 1) {
            throw new ValidationException("Interface must have a version", node);
        }
        String[] split = version.split("\\.");
        if (split.length!=2) {
            throw new ValidationException("Interface version must be of the form x.y: "+version, node);
        }
        for (String s : split) {
            try {
                int i = Integer.parseInt(s);
                if (i<0) {
                    throw new ValidationException("All components of the interface version must be numeric and >=0: "+version, node);
                }
            }
            catch (NumberFormatException nfe) {
                throw new ValidationException("All components of the interface version must be numeric: "+version, node);//NOSONAR
            }
        }
	}
}
