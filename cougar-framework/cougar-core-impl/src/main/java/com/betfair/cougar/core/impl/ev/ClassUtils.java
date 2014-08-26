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

package com.betfair.cougar.core.impl.ev;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassUtils {
	public static Class [] getGenerics(Type t) {
		Class [] generics = null;

		if (t instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) t;
            Type[] typeArguments = type.getActualTypeArguments();
            generics = new Class[typeArguments.length];
            for(int i=0; i < typeArguments.length; ++i){
            	generics[i] = (Class) typeArguments[i];
            }
        }

		return generics;
	}
}
