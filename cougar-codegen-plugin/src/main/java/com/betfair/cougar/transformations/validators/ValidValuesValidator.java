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

public class ValidValuesValidator extends AbstractValidator {
    @Override
    public boolean nodeMustExist() {
        return false;
    }

    @Override
    public String getName() {
        return "Valid Values Validator";
    }

    @Override
    public String getXPath() {
        return "//validValues";
    }

    @Override
    public void validate(Node node) throws ValidationException {
        // First thing is to find out if the valid values must have an ID
        Node parent = node.getParentNode();
        Node grandParent = parent.getParentNode();

        boolean needsIDs = false;
        if (grandParent.getLocalName().equals("exceptionType")) {
            // Only the first child of an exception type needs an ID
            Node firstParam = getFirstChildWithName(grandParent, "parameter");
            if (firstParam == null) {
                throw new ValidationException(getName() + " - Node does not have any parameter children defined", node);
            }
            if (firstParam.equals(parent)) {
                needsIDs = true;
            }
        }

        // Ensure that all enumerations are of type string.
        String type = getAttribute(getName(), parent, "type");
        if (!type.equals("string")) {
            throw new ValidationException(getName() + " - Valid values is not of base type string.", node);
        }

        // If it's a simpleType enumeration, it must start with an upper case letter
        if (parent.getLocalName().equals("simpleType")) {
            String name = getAttribute(getName(), parent, "name");
            if (name == null || name.length() < 1) {
                throw new ValidationException("Data types must have a name", node);
            } else if (Character.isLowerCase(name.charAt(0))) {
                throw new ValidationException("Simple type names must start with a capital letter", node);
            }
        }
        Set<Integer> idsUsed = new HashSet<Integer>();
        Set<String> namesUsed = new HashSet<String>();

        List<Node> values = getChildrenWithName(getName(), node, "value");
        if (values.size() == 0) {
            throw new ValidationException(getName() + " - Valid values list does not have any children defined", node);
        }
        for (Node val : values) {
            Integer id = getAttributeAsInt(getName(), val, "id");

            if (needsIDs == (id == null)) {
                if (needsIDs) {
                    throw new ValidationException(getName() + " - no ID defined for valid value", val);
                } else {
                    throw new ValidationException(getName() + " - ID defined for valid value which does not require one", val);
                }
            }

            if (id != null) {
                if (idsUsed.contains(id)) {
                    throw new ValidationException(getName() + " - duplicate id: " + id, val);
                }
                idsUsed.add(id);
            }
            String vvName = getAttribute(getName(), val, "name");
            if (vvName != null) {
                if (vvName.length() < 1) {
                    throw new ValidationException("Valid values must have a name", node);
                } else if (Character.isLowerCase(vvName.charAt(0))) {
                    throw new ValidationException("Valid value names must start with a capital letter", node);
                }
                if (namesUsed.contains(vvName)) {
                    throw new ValidationException(getName() + " - duplicate name: " + vvName, val);
                }
                namesUsed.add(vvName);
            } else {
                throw new ValidationException(getName() + " - no name defined for valid value", val);
            }


        }

    }


}
