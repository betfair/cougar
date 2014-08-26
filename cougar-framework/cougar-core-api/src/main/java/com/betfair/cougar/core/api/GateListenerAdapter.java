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

package com.betfair.cougar.core.api;

import java.lang.reflect.Method;

import org.springframework.util.Assert;

/**
 * A {@link GateListener} which, when called, invokes a specified method on a given object.
 * <p>
 * Essentially does what Spring's {@code init-method} attribute does, allowing us to start up
 * beans without them having to know anything about the {@link GateListener} interface.
 */
public class GateListenerAdapter implements GateListener {

	private Object o;
	private String methodName;
	private int priority;

	@Override
	public String getName() {
		return (o != null ? o.getClass().getName() : "null")
			+ "#"
			+ methodName;
	}

	@Override
	public void onCougarStart() {

		Assert.notNull(o, "Object to be invoked is null.");
		Assert.notNull(methodName, "Method name is null.");
		Assert.isTrue(methodName.length() > 0, "Method name is empty");

		try {
			Method m = o.getClass().getMethod(methodName);
			m.invoke(o);
		} catch (Exception e) {
			// better exception?
			throw new RuntimeException("Error invoking method for " + getName() + ": " + e, e);
		}
	}

	/**
	 * 'bean' is easier to type than 'object'
	 *
	 * @param bean the object on which to invoke the 'init' method.
	 */
	public void setBean(Object bean) {
		this.o = bean;
	}

	/**
	 * @param methodName name of method to be invoked
	 */
	public void setMethod(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}


}
