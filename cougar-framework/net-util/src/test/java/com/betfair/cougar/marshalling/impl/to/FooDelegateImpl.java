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

package com.betfair.cougar.marshalling.impl.to;

import java.util.HashMap;
import java.util.Map;

public class FooDelegateImpl implements FooDelegate {

	private String value;
	private Bar bar;

	public FooDelegateImpl(String value) {
		this.value = value;
		this.bar = new Bar(new BarDelegateImpl(7));
	}

	@Override
	public String getFooName() {
		return value;
	}

	@Override
	public Bar getBar() {
		return bar;
	}




	@Override
	public void setFooName(String foo) {
		throw new IllegalArgumentException();

	}

	@Override
	public void setBar(Bar bar) {
		this.bar = bar;

	}

	@Override
	public Map<Bar, Baz> getBarBazMap() {
		Map<Bar,Baz> barBazMap = new HashMap<Bar, Baz>();
		Bar bar = new Bar(new BarDelegateImpl(22));
		Baz baz = new Baz(new BazDelegateImpl(44));
		barBazMap.put(bar, baz);
		return barBazMap;
	}

	@Override
	public void setBarBazMap(Map<Bar, Baz> barBazMap) {
		throw new IllegalArgumentException();

	}

	@Override
	public byte[] getPrimitiveArray() {
		byte[] bytes = {1, 2, 3};
		return bytes;
	}

	@Override
	public void setPrimitiveArray(byte[] bytes) {
		throw new IllegalArgumentException();
	}

}
