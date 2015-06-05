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

package com.betfair.cougar.transport.impl.protocol.http.rescript;

import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.*;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.core.api.transcription.Parameter;
import com.betfair.cougar.core.api.transcription.ParameterType;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.UnMarshaller;
import com.betfair.cougar.marshalling.impl.databinding.DataBindingManager;
import com.betfair.cougar.marshalling.impl.util.BindingUtils;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptBody;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptOperationBindingDescriptor;
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptParamBindingDescriptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the binding between a rescript operation and an operation definition
 */
public class RescriptOperationBinding {

    private final OperationKey operationKey;
    private final OperationDefinition operationDefinition;
    private final String method;
    private final RescriptParamBindingDescriptor [] paramBindings;
    private final RescriptOperationBindingDescriptor bindingDescriptor;
    private final boolean hardFailEnums;

    public RescriptOperationBinding(RescriptOperationBindingDescriptor bindingDescriptor, OperationDefinition opDef, boolean hardFailEnums) {
        Parameter [] params = opDef.getParameters();
        //this is just to optimize away the lookup of a physical binding from a parameter definition
        //note resultant paramBindings array is in ingress order for the operation, i.e. positional ordering
        paramBindings = new RescriptParamBindingDescriptor[params.length];
        for (int i=0;i<paramBindings.length;i++) {
            paramBindings[i] =
                bindingDescriptor.getHttpParamBindingDescriptor(
                            params[i].getName());
        }
        this.operationKey = bindingDescriptor.getOperationKey();
        this.operationDefinition = opDef;
        this.method = bindingDescriptor.getHttpMethod();
        this.bindingDescriptor = bindingDescriptor;
        this.hardFailEnums = hardFailEnums;
    }

    public String getMethod() {
        return method;
    }

    public OperationKey getOperationKey() {
        return operationKey;
    }

    public RescriptOperationBindingDescriptor getBindingDescriptor() {
		return bindingDescriptor;
	}

	public Object[] resolveArgs(HttpServletRequest request, InputStream inputStream, MediaType mediaType, String encoding) {
        Object[] args = new Object[paramBindings.length];

        String format = mediaType == null || mediaType.getSubtype() == null ? "unknown" : mediaType.getSubtype();
        RescriptBody body = null;
        if (bindingDescriptor.containsBodyData()) {
            //If the request contains body data, then it must be a post request
            if (!request.getMethod().equals("POST")) {
                throw CougarMarshallingException.unmarshallingException(format, "Bad body data", false);
            }
            body = resolveBody(inputStream, mediaType, encoding);
        }

        // jetty 9 handily gives back null when you specify a header with no value, whereas jetty 7 treated this as an empty string.. which we rely on
        // so, we're going to list all the header names, and for each on if the value is null, add them to a set so we can query later..
        Set<String> headersWithNullValues = new HashSet<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            if (request.getHeader(header) == null) {
                headersWithNullValues.add(header.toLowerCase());
            }
        }
        try {
            for (int i = 0; i < args.length; ++i) {
                RescriptParamBindingDescriptor descriptor = paramBindings[i];
                Parameter param = operationDefinition.getParameters()[i];
                switch (descriptor.getSource()) {
                    case HEADER :
                        String key = descriptor.getName();
                        args[i] = resolveArgument(headersWithNullValues.contains(key.toLowerCase()) ? "" : request.getHeader(key), param, descriptor, format);
                        break;
                    case QUERY :
                        args[i] = resolveArgument(request.getParameter(descriptor.getName()), param, descriptor, format);
                        break;
                    case BODY :
                        if (body != null) {
                            args[i] = body.getValue(descriptor.getName());
                            // non-null enums get stored as their raw string value so need converting to the true enum value
                            if (param.getParameterType().getType() == ParameterType.Type.ENUM) {
                                if (args[i] != null) {
                                    args[i] = EnumUtils.readEnum(param.getParameterType().getImplementationClass(), (String) args[i]);
                                }
                            }
                        }
                        break;
                    default :
                        throw new PanicInTheCougar("Unsupported argument annotation "+ descriptor.getSource());
                }
                //request.trace("Deserialised argument {} from {} to value {}", i, param.getSource(), args[i]);
            }
        }
        catch (EnumDerialisationException ede) {
            throw CougarMarshallingException.unmarshallingException(format, ede.getMessage(), ede.getCause(), false);
        }
        return args;
    }

    public Object resolveArgument(String value, Parameter param, RescriptParamBindingDescriptor descriptor, String format) {
        if (value != null) {
            //We only support one generic type - no maps etc.
            Class<?> genericClass = null;
            if (param.getParameterType().getComponentTypes() != null &&
                    param.getParameterType().getComponentTypes().length == 1) {
                genericClass = param.getParameterType().getComponentTypes()[0].getImplementationClass();
            }
            return BindingUtils.convertToSimpleType(
                    param.getParameterType().getImplementationClass(),
                    genericClass, param.getName(),  value, false, hardFailEnums, format, false);
        }
        return null;
    }

    public RescriptBody resolveBody(InputStream inputStream, MediaType mediaType, String encoding) {
        // First of all deserialise the body to get the basics of the request
        if (mediaType != null) {
            DataBindingFactory factory = DataBindingManager.getInstance().getFactory(mediaType);
            if(factory == null) {
                throw new CougarFrameworkException("Invalid content type " + mediaType);
            }
            UnMarshaller unMarshaller = factory.getUnMarshaller();
            return (RescriptBody)unMarshaller.unmarshall(inputStream,
                        bindingDescriptor.getBodyClass(),
                        encoding, false);
        }
        return null;
    }

    @Override
    public String toString() {
        return "RescriptOperationBinding{" +
                "operationKey=" + operationKey +
                ", operationDefinition=" + operationDefinition +
                ", method='" + method + '\'' +
                ", paramBindings=" + (paramBindings == null ? null : Arrays.asList(paramBindings)) +
                ", bindingDescriptor=" + bindingDescriptor +
                ", hardFailEnums=" + hardFailEnums +
                '}';
    }
}

