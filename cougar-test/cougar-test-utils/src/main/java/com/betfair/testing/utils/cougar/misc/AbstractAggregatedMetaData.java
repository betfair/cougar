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
import java.util.List;

/**
 * Aggregates all Step Meta Data (eg a list of bets with parameters) into this object.
 *
 */
public abstract class AbstractAggregatedMetaData {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAggregatedMetaData other = (AbstractAggregatedMetaData) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	private List<StepMetaData> data = new ArrayList<StepMetaData>();
	
	public void addMetaData(StepMetaData metaData) {
		data.add(metaData);
	}
	
	public StepMetaData getMetaDataAtIndex(int x) {
		return data.get(x);
	}
	
	public StepMetaData getMetaDataForKey(String key) throws RuntimeException {
		for (StepMetaData metaData : data) {
			if (metaData.getId().equals(key)) {
				return metaData;
			}
		}
		
		throw new RuntimeException("Unable to find meta data for key:" + key);
	}
	
	public List<StepMetaData> getData() {
		return data;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public List<StepMetaData> getValues() {
		return data;
	}
	
	public int size() {
		return data.size();
	}

	public void setData(List<StepMetaData> data) {
		this.data = data;
	}
}
