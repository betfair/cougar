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

package com.betfair.cougar.marshalling.impl.databinding.json;


import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.marshalling.api.databinding.FaultMarshaller;
import com.betfair.cougar.marshalling.api.databinding.Marshaller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultController;
import com.betfair.cougar.core.api.fault.FaultDetail;

public class JSONMarshaller implements Marshaller, FaultMarshaller {
	private final ObjectMapper objectMapper;

	public JSONMarshaller(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String getFormat() {
		return "json";
	}

	@Override
	public void marshall(OutputStream outputStream, Object result, String encoding, boolean client) {
		try {
			objectMapper.writeValue(outputStream, result);
		} catch (JsonProcessingException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to marshall object of class "+result.getClass().getCanonicalName()+" to JSON", e, client);
		} catch (IOException e) {
            throw CougarMarshallingException.marshallingException(getFormat(), "Failed to write JSON object to stream", e, client);
        }
	}

	@Override
	public void marshallFault(OutputStream outputStream, CougarFault fault, String encoding) {
		HashMap<String,Object> faultMap = new HashMap<>();
		HashMap<String,Object> detailMap = new HashMap<>();
		faultMap.put("faultcode", fault.getFaultCode().name());
		faultMap.put("faultstring", fault.getErrorCode());
		faultMap.put("detail", detailMap);

		FaultDetail detail = fault.getDetail();
    	if (FaultController.getInstance().isDetailedFaults()) {
    		detailMap.put("trace", detail.getStackTrace());
    		detailMap.put("message", detail.getDetailMessage());
    	}
        List<String[]> faultMessages = detail.getFaultMessages();
        if (faultMessages != null) {
            detailMap.put("exceptionname", detail.getFaultName());
        	HashMap<String,Object> paramMap = new HashMap<>();
        	detailMap.put(detail.getFaultName(), paramMap);
        	for (String[] msg: faultMessages) {
    	        paramMap.put(msg[0], msg[1]);
        	}
        }

        marshall(outputStream, faultMap, encoding, false);
	}


}
