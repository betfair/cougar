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

package com.betfair.cougar.baseline;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.betfair.baseline.v2.enumerations.SomeComplexObjectEnumParameterEnum;
import com.betfair.baseline.v2.to.SomeComplexObjectDelegate;

public class SomeComplexObjectDelegateImpl implements SomeComplexObjectDelegate {

	private Date dateTimeParameter = new Date(112233);
	private String stringParameter;
	private SomeComplexObjectEnumParameterEnum enumParameter = SomeComplexObjectEnumParameterEnum.BAR;
		
	public SomeComplexObjectDelegateImpl(String stringParameter) {
		this.stringParameter = stringParameter;
	}
	
	public Date getDateTimeParameter() {
		return dateTimeParameter;
	}

	public SomeComplexObjectEnumParameterEnum getEnumParameter() {
		return enumParameter;
	}

	public List<String> getListParameter() {
		List<String> listParameter = new ArrayList<String>();
		listParameter.add("item1");
		listParameter.add("item2");
		return listParameter;
	}

	public String getStringParameter() {
		return stringParameter;
	}

    @Override
    public String getRawEnumParameterValue() {
        return enumParameter.name();
    }

    @Override
    public void setRawEnumParameterValue(String enumParameter) {
        // Do Nothing
    }

    public void setDateTimeParameter(Date dateTimeParameter) {
		// Do Nothing
		
	}

	public void setEnumParameter(SomeComplexObjectEnumParameterEnum enumParameter) {
		//Do Nothing
		
	}

	public void setListParameter(List<String> listParameter) {
		// Do Nothing
		
	}

	public void setStringParameter(String stringParameter) {
		// Do Nothing
		
	}
}
