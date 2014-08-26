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

/**
 *
 */
package com.betfair.cougar.core.api.transcription;

import com.betfair.cougar.core.api.client.EnumWrapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterType {

	public enum Type {
		INT(false),
		DOUBLE(false),
		FLOAT(false),
		STRING(false),
		BOOLEAN(false),
		BYTE(false),
		DATE(false),
		LONG(false),
		MAP(true),
		SET(true),
		LIST(true),
		OBJECT(false),
		ENUM(false);

		private boolean collection;
		Type(boolean collection) {
			this.collection = collection;
		}
		public boolean isCollection() {
			return collection;
		}
	}

	private final Type type;
	private final Class implementationClass;
	private final ParameterType [] componentTypes;

	public static Type getType(final Class clazz) {
		if (Map.class.isAssignableFrom(clazz)) {
			return Type.MAP;
		} else if (clazz.isArray() || List.class.isAssignableFrom(clazz)) {
			return Type.LIST;
		} else if (Set.class.isAssignableFrom(clazz)) {
			return Type.SET;
		} else if (clazz==Integer.class || clazz==int.class) {
			return Type.INT;
		} else if (clazz==Double.class || clazz==double.class) {
			return Type.DOUBLE;
		} else if (clazz==String.class) {
			return Type.STRING;
		} else if (clazz==Boolean.class || clazz==boolean.class) {
			return Type.BOOLEAN;
		} else if (clazz==Long.class || clazz==long.class) {
			return Type.LONG;
		} else if (clazz==Date.class) {
			return Type.DATE;
		} else if (clazz==Float.class || clazz==float.class) {
			return Type.FLOAT;
		} else if (clazz==Byte.class || clazz==byte.class) {
			return Type.BYTE;
		} else if (clazz.isEnum() || clazz==Enum.class) {
			return Type.ENUM;
		} else {
			return Type.OBJECT;
		}
	}

	public static ParameterType create(final Class clazz, final Class... generics) {

		ParameterType [] subTypes = null;

		if (generics!=null) {
			subTypes = new ParameterType[generics.length];
			for (int i=0;i<generics.length;i++) {
				subTypes[i] = new ParameterType(generics[i], null);
			}
		}

		return new ParameterType(clazz, subTypes);
	}

	public ParameterType(final Class clazz, final ParameterType[] componentTypes) {//NOSONAR
		this.type = getType(clazz);
		this.implementationClass = clazz;
		if (clazz.isArray()) {
			this.componentTypes = new ParameterType[] { new ParameterType(clazz.getComponentType(), null) };
		} else if (type.isCollection() && componentTypes!=null) {
			this.componentTypes = componentTypes.clone();
		} else if (implementationClass.equals(EnumWrapper.class) && componentTypes!=null) {
            this.componentTypes = componentTypes.clone();
        }
        else {
			this.componentTypes = null;
		}
	}

	public Type getType() {
		return type;
	}

	public ParameterType[] getComponentTypes() {
		return componentTypes;
	}

	public Class getImplementationClass() {
		return implementationClass;
	}

	public interface TransformingVisitor<T> {
		public T transformMapType(T keyType, T valueType);
		public T transformListType(T elemType);
		public T transformSetType(T elemType);
		public T transformType(Type type, Class implementationClass);
	}

	public <T> T transform(TransformingVisitor<T> visitor) {
		switch (getType()) {
		case SET:
			return visitor.transformSetType(getComponentTypes()[0].transform(visitor));
		case LIST:
			return visitor.transformListType(getComponentTypes()[0].transform(visitor));
		case MAP:
			return visitor.transformMapType(getComponentTypes()[0].transform(visitor), getComponentTypes()[1].transform(visitor));
		default:
			return visitor.transformType(getType(), getImplementationClass());
		}
	}


}