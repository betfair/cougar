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

package com.betfair.baseline.v2.to;

import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectBodyParameterEnum;
import com.betfair.baseline.v2.enumerations.EnumOperationResponseObjectQueryParameterEnum;


/**
 * deliberately removed headerParameter to simulate the server adding parameters to the response
 */
public class  EnumOperationResponseObject {
    private EnumOperationResponseObjectQueryParameterEnum queryParameter;
    public final EnumOperationResponseObjectQueryParameterEnum getQueryParameter()  {
    	return queryParameter;
    }
    public final void setQueryParameter(EnumOperationResponseObjectQueryParameterEnum queryParameter)  {
    	this.queryParameter=queryParameter;
    }

    /*  
    private EnumOperationResponseObjectHeaderParameterEnum headerParameter;
    public final EnumOperationResponseObjectHeaderParameterEnum getHeaderParameter()  {
    	return headerParameter;
    }
    public final void setHeaderParameter(EnumOperationResponseObjectHeaderParameterEnum headerParameter)  {
    	this.headerParameter=headerParameter;
    }
    */
    
    private EnumOperationResponseObjectBodyParameterEnum bodyParameter;
    public final EnumOperationResponseObjectBodyParameterEnum getBodyParameter()  {
    	return bodyParameter;
    }

    public final void setBodyParameter(EnumOperationResponseObjectBodyParameterEnum bodyParameter)  {
    	this.bodyParameter=bodyParameter;
    }
    

}


