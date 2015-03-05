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

public class ExceptionValidator extends AbstractValidator {
	private Set<String> exceptionNames = new HashSet<String>();
    private boolean legacyExceptionModeValidation;

    public ExceptionValidator() {
        this(false);
    }

    public ExceptionValidator(boolean legacyExceptionModeValidation) {
        this.legacyExceptionModeValidation = legacyExceptionModeValidation;
    }

    @Override
	public boolean nodeMustExist() {
		return true;
	}

	@Override
	public String getName() {
		return "Exception Validator";
	}

	@Override
	public String getXPath() {
		return "/interface/exceptionType";
	}

	@Override
	public void validate(Node node) throws ValidationException {
		String name = getAttribute(getName(), node, "name");
		if (name == null || name.length() < 1) {
			throw new ValidationException("exceptions must have a name", node);
		} else if (Character.isLowerCase(name.charAt(0))) {
			throw new ValidationException("exceptions must start with a capital letter", node);
		} else if (exceptionNames.contains(name)) {
			throw new ValidationException("The exception " + name + " is already defined", node);
		}
        exceptionNames.add(name);
         // Ensure there is at least one parameter, and it's an enum.
         Node firstParam = getFirstChildWithName (node, "parameter");

         // Ensure that the parameter has valid values
         getFirstChildWithName(firstParam, "validValues");

        //Check that all params in the exception type are firstly
        List<Node> parameters = getChildrenWithName(getName(), node, "parameter");
        for (Node param : parameters) {
            String paramName = getAttribute(getName(), param, "name");
            String paramType = getAttribute(getName(), param, "type");
            checkTypeIsSimple( paramName, param, paramType, false);

            //We simply don't permit this dude in exceptions
            if (paramType.toLowerCase().equals("datetime")) {
                throw new ValidationException("Datetime arguments [" + paramName + "] are not permitted as exception parameters", param);
            }
            if (!legacyExceptionModeValidation && (paramName.equals("message") || paramName.equals("Message"))) {
                throw new ValidationException("Exceptions can't have a parameter named [message]", param);
            }
            if (paramName.equals("localizedMessage") || paramName.equals("LocalizedMessage")) {
                throw new ValidationException("Exceptions can't have a parameter named [localizedMessage]", param);
            }
            if (paramName.equals("cause") || paramName.equals("Cause")) {
                throw new ValidationException("Exceptions can't have a parameter named [cause]", param);
            }
            if (paramName.equals("stackTrace") || paramName.equals("StackTrace")) {
                throw new ValidationException("Exceptions can't have a parameter named [stackTrace]", param);
            }
            if (paramName.equals("stackTraceDepth") || paramName.equals("StackTraceDepth")) {
                throw new ValidationException("Exceptions can't have a parameter named [stackTraceDepth]", param);
            }
            if (paramName.equals("suppressed") || paramName.equals("Suppressed")) {
                throw new ValidationException("Exceptions can't have a parameter named [suppressed]", param);
            }
        }
	}
}
