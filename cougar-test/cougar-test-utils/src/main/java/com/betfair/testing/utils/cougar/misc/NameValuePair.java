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

package com.betfair.testing.utils.cougar.misc;



public class NameValuePair implements Cloneable {

	public Object value;
	public Object name;
	
	public NameValuePair(Object name, Object value) {
		setName(name);
		setValue(value);
	}
	public NameValuePair() {
	}
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Object getName() {
		return name;
	}
	public void setName(Object name) {
		if (name != null && name.getClass() == String.class) {
			name = name.toString().trim();
		}
		this.name = name;
	}
	public NameValuePair clone() {
		try {
			return (NameValuePair)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Unable to clone NameValuePair", e);
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}	
		if((obj == null) || (obj.getClass() != this.getClass())){
			return false;
		}
		NameValuePair nvp = (NameValuePair)obj;
		return (
		(value == nvp.value ||(value != null && value.equals(nvp.value))
		&& (name == nvp.name ||( name!= null && name.equals(nvp.name)))));

	}
	
	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 9 * hash + (null == value ? 0 : value.hashCode());
		hash = 8 * hash + (null == name? 0: name.hashCode());
		return hash;
	}
	
	
	
}
