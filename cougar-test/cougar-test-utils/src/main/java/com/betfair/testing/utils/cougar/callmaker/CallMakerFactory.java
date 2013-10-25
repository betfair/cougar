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

package com.betfair.testing.utils.cougar.callmaker;

import com.betfair.testing.utils.cougar.enums.*;

import java.util.HashMap;
import java.util.Map;

public final class CallMakerFactory {

	private CallMakerFactory(){}
	
	private static Map<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker> requestBuilderMap = new HashMap<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker>();

	public static AbstractCallMaker resolveRequestBuilderForCougarService(CougarMessageProtocolRequestTypeEnum protocolRequestType) {
	
		AbstractCallMaker callMaker = requestBuilderMap.get(protocolRequestType);
		if (callMaker == null) {
			throw new IllegalStateException("Don't know how to make call for:" + protocolRequestType.toString());
		}
		return callMaker;
	}

	public static Map<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker> getRequestBuilderMap() {
		return requestBuilderMap;
	}

	public static void setRequestBuilderMap(Map<CougarMessageProtocolRequestTypeEnum, AbstractCallMaker> rbm) {
		CallMakerFactory.requestBuilderMap = rbm;
	}	
	
}
