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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Holds a list of name/value properties used for setting up a Test Step for input and outputs.
 *
 */
public class StepMetaData implements Cloneable {

	private String id;
	private List<NameValuePair> metaData = new ArrayList<NameValuePair>();
	
	private int index = 0;
	
	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((metaData == null) ? 0 : metaData.hashCode());
		return result;
	}
	
	//TODO: This is ignoring the index now. However, the index never get updated after remove. We should update the index accordingly and includes index in the equal
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepMetaData other = (StepMetaData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (metaData == null) {
			if (other.metaData != null)
				return false;
		} else if (!metaData.equals(other.metaData))
			return false;
		return true;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Object put(int index, NameValuePair nvPair) {
		Object oldValue = metaData.set(index, nvPair);
		return oldValue;
	}
	
	public Object put(NameValuePair nvPair) {
		Object oldValue = metaData.add(nvPair);
		index++;
		return oldValue;
	}
	
	public Object remove(int index) {
		return metaData.remove(index);
	}
	
	public Object remove(NameValuePair nvPair) {
		return metaData.remove(nvPair);
	}
	
	public Object get(String name) {
		for (NameValuePair curName : metaData) {
			if (curName.getName().equals(name)) {
				return curName;
			}
		}
		throw new IllegalStateException("Attempting to retrieve:" + name +", but not found");
	}
	
	public Object getIndex(int index) {
		return metaData.get(index);
	}
	
	public Object getValueAtIndex(int index) {
		return metaData.get(index).getValue();
	}
	
	public List<NameValuePair> getNameValuePairs() {
		return metaData;
	}
	
	public Set<Object> keySet() {
		Set<Object> keys = new HashSet<Object>();
		for (NameValuePair nvPair : metaData) {
			keys.add(nvPair.getName());
		}
		return keys;
	}
	
	public int size() {
		return metaData.size();
	}
	public Object getNameAtIndex(int index) {
		return metaData.get(index).getName();
	}
	
	
	
	public StepMetaData clone() {
		
		try {
			StepMetaData clone = (StepMetaData)super.clone();
			List<NameValuePair> originalMetaData = this.metaData;
			List<NameValuePair> clonedMetaData = new ArrayList<NameValuePair>(originalMetaData.size());
			for (NameValuePair nameValuePair: originalMetaData) {
				clonedMetaData.add(nameValuePair.clone());
			}
			clone.metaData = clonedMetaData;
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Unable to clone StepMetaData", e);
		}
	}
	
}
