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

package com.betfair.cougar.transport.impl;

import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.TransportCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry to enable simple addition of CommandValidators
 */
public class CommandValidatorRegistry<T extends TransportCommand> {

    private List<CommandValidator<T>> validators = new ArrayList<CommandValidator<T>>();

    public List<CommandValidator<T>> getValidators() {
        return validators;
    }

    public CommandValidatorRegistry addValidator(CommandValidator<T> validator) {
        validators.add(validator);
        return this;
    }
}
