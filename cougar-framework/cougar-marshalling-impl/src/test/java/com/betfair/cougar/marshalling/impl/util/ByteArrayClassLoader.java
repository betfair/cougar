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

package com.betfair.cougar.marshalling.impl.util;


/**
 * Load a class from an array of bytes.  loads the class in preference to any parent class loader
 */
public class ByteArrayClassLoader extends ClassLoader {

	private byte[] classBytes;
	private String className;

	public ByteArrayClassLoader(String className, byte[] classBytes) {
		  this(ByteArrayClassLoader.class.getClassLoader(), className, classBytes);
	}

	public ByteArrayClassLoader(ClassLoader parent, String className, byte[] classBytes) {
		super(parent);
		this.className = className;
		this.classBytes = classBytes;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (className.equals(name)) {
			return defineClass(name, classBytes, 0, classBytes.length);
		}
		return super.findClass(name);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz == null && className.equals(name)) {
			clazz = findClass(name);
			if (resolve) {
				resolveClass(clazz);
			}
			return clazz;
		}
		return super.loadClass(name, resolve);
	}

}

