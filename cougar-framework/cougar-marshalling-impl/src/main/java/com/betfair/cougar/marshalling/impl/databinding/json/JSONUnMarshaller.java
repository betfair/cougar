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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import com.betfair.cougar.api.fault.FaultCode;
import com.betfair.cougar.core.api.client.EnumWrapper;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.fault.CougarFault;
import com.betfair.cougar.core.api.fault.FaultDetail;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;

public class JSONUnMarshaller implements UnMarshaller, FaultUnMarshaller {

	private final ObjectMapper objectMapper;

	public JSONUnMarshaller(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String getFormat() {
		return "json";
	}

	@Override
	public Object unmarshall(InputStream inputStream, Class<?> clazz, String encoding, boolean client) {
		try {
			Reader reader = new BufferedReader(new InputStreamReader(inputStream,encoding));
			return objectMapper.readValue(reader, clazz);
		} catch (JsonProcessingException e) {
//            if (e.getCause() instanceof CougarException) {
//                throw (CougarException) e.getCause();
//            }
            throw CougarMarshallingException.unmarshallingException(getFormat(), e, client);
		} catch (IOException e) {
            throw CougarMarshallingException.unmarshallingException(getFormat(), "Failed to unmarshall object", e, client);
		}
	}

    private static final ParameterType STRING_PARAM_TYPE = new ParameterType(String.class, null);

    public Object unmarshall(InputStream inputStream, ParameterType parameterType, String encoding, boolean client) {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream,encoding));
            if (parameterType.getImplementationClass().equals(EnumWrapper.class)) {
                String value = objectMapper.readValue(reader, buildJavaType(STRING_PARAM_TYPE));
                //noinspection unchecked
                return new EnumWrapper(parameterType.getComponentTypes()[0].getImplementationClass(), value);
            }
            else {
                return objectMapper.readValue(reader, buildJavaType(parameterType));
            }
        } catch (EnumDerialisationException e) {
            throw CougarMarshallingException.unmarshallingException(getFormat(), "Failed to unmarshall enum", e, client);
        } catch (JsonProcessingException e) {
            throw CougarMarshallingException.unmarshallingException(getFormat(), "Failed to unmarshall object", e, client);
        } catch (IOException e) {
            throw CougarMarshallingException.unmarshallingException(getFormat(), "Failed to unmarshall object", e, client);
        }

    }

    public static JavaType buildJavaType(ParameterType paramType) {
		return paramType.transform(new ParameterType.TransformingVisitor<JavaType>() {
			@Override
			public JavaType transformMapType(JavaType keyType, JavaType valueType) {
				return TypeFactory.defaultInstance().constructMapType(HashMap.class, keyType, valueType);
			}
			@Override
			public JavaType transformListType(JavaType elemType) {
				return TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, elemType);
			}
			@Override
			public JavaType transformSetType(JavaType elemType) {
				return TypeFactory.defaultInstance().constructCollectionType(HashSet.class, elemType);
			}
			@Override
			public JavaType transformType(ParameterType.Type type, Class implementationClass) {
                return TypeFactory.defaultInstance().uncheckedSimpleType(implementationClass);
			}
		});

	}


    @Override
    public CougarFault unMarshallFault(InputStream inputStream, String encoding) {
        //noinspection unchecked
        final HashMap<String,Object> faultMap = (HashMap<String,Object>) unmarshall(inputStream, HashMap.class, encoding, true);

        final String faultString = (String)faultMap.get("faultstring");
        final FaultCode faultCode = FaultCode.valueOf((String) faultMap.get("faultcode"));


        //noinspection unchecked
        final HashMap<String, Object> detailMap = (HashMap<String, Object>)faultMap.get("detail");
        String exceptionName = (String)detailMap.get("exceptionname");

        List<String[]> faultParams = Collections.emptyList();
        if (exceptionName != null) {
            faultParams = new ArrayList<>();
            //noinspection unchecked
            Map<String, Object> paramMap = (Map<String, Object>) detailMap.get(exceptionName);

            for(Map.Entry e:paramMap.entrySet()){
                String[] nvpair=new String[] { (String)e.getKey(), e.getValue().toString() };
                faultParams.add(nvpair);
            }
        }

        final FaultDetail fd=new FaultDetail(faultString, faultParams);

        return new CougarFault() {
            @Override
            public String getErrorCode() {
                return faultString;
            }

            @Override
            public FaultCode getFaultCode() {
                return faultCode;
            }

            @Override
            public FaultDetail getDetail() {
                return fd;
            }
        };
    }
}
