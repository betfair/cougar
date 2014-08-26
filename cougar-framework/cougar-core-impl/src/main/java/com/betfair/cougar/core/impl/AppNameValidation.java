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

package com.betfair.cougar.core.impl;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class AppNameValidation {
    public AppNameValidation(String appName) {
        if (StringUtils.isBlank(appName)) {
            throw new IllegalArgumentException("'cougar.app.name' is a mandatory property");
        }
        if (appName.startsWith("-")) {
            throw new IllegalArgumentException("'cougar.app.name' must not start with a '-': '"+appName+"'");
        }
    }
}
