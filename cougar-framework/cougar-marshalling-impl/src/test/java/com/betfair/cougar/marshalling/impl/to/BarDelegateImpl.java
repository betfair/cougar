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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BarDelegateImpl implements BarDelegate {


	private double value;
	private Baz baz = new Baz(new BazDelegateImpl(15));


	public BarDelegateImpl(double i) {
		this.value = i;
	}


	@Override
	public Double getBarDouble() {
		return value;
	}


	@Override
	public void setBarDouble(Double marketBaseRate) {
		throw new IllegalArgumentException();

	}


	@Override
	public List<Baz> getBazList() {
		return Collections.singletonList(baz);
	}


	@Override
	public Set<Baz> getBazSet() {
		return Collections.singleton(baz);
	}


	@Override
	public void setBazList(List<Baz> bazs) {
		throw new IllegalArgumentException();

	}


	@Override
	public void setBazSet(Set<Baz> bazSet) {
		throw new IllegalArgumentException();

	}


	@Override
	public Baz[] getBazArray() {
		Baz[] baz = new Baz[4];
		baz[0] = this.baz;
		for (int i=1;i<baz.length;i++) {
			baz[i] = new Baz(new BazDelegateImpl(i));
		}
		return baz;
	}


	@Override
	public void setBazArray(Baz[] bazArray) {
		throw new IllegalArgumentException();
	}

}
