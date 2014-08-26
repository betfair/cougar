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

package com.betfair.cougar.core.api.monitor;

import com.betfair.cougar.api.Service;


public class InterfaceUtils {
    @SuppressWarnings("unchecked")
	public static Class<Service> getInterface(final Service service) {
        Class<Service> clazz = null;

        Class<?> classExamining = service.getClass();
        while (classExamining != null) {
            for (Class<?> intfClass : classExamining.getInterfaces()) {
                if (Service.class.isAssignableFrom(intfClass)) {
                    clazz = (Class<Service>)intfClass;
                    break;
                }
            }
            // walk up the tree - handles the case where we don't have the real implementation, but perhaps a
            // cglib enhanced sub-class
            classExamining = classExamining.getSuperclass();
        }
        return clazz;
	}
}
