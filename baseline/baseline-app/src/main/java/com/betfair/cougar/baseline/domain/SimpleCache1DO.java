/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.baseline.domain;

import java.io.Serializable;

public class SimpleCache1DO implements Serializable {

	protected int value;
	protected String name;
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getName() {
		String returnName = name.split("=")[1].trim();
		return returnName;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return "Baseline - SimpleCache1 - " + name + ", loadCount = " + value;
	}
	
	public String getCacheName() {
		return "SimpleCache1";
	}
	
	public String getServiceName() {
		return "Baseline";
	}
}
