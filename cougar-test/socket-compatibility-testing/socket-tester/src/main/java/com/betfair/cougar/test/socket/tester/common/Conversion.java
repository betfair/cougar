/*
 * Copyright 2015, Simon Matić Langford
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
package com.betfair.cougar.test.socket.tester.common;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.Identity;
import com.betfair.cougar.api.security.IdentityChain;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Conversion {
    public static Object convert(Object o, Class src, Class target) {
        if (!canTransferBetween(src,target)) {
            throw new RuntimeException("Can't convert between "+src+" and "+target);
        }
        if (o instanceof ExecutionContext) {
            ExecutionContextTO ret = new ExecutionContextTO();
            transfer(ExecutionContext.class, o,ret);
            return ret;
        }
        if (o instanceof GeoLocationDetails) {
            GeoLocationDetailsTO ret = new GeoLocationDetailsTO();
            transfer(GeoLocationDetails.class,o,ret);
            return ret;
        }
        if (src.getName().startsWith("java.lang")) {
            return o;
        }
        if (src.isPrimitive()) {
            return o;
        }
        if (src.getName().startsWith("java.util.List")) {
            return new ArrayList((List)o);
        }
        if (src.getName().startsWith("java.util.Date")) {
            return o;
        }
        if (src == IdentityChain.class) {
            List<IdentityTO> ret = new ArrayList<>();
            IdentityChain chain = (IdentityChain) o;
            if (chain.getIdentities() != null) {
                for (Identity id : chain.getIdentities()) {
                    IdentityTO newId = new IdentityTO();
                    newId.setPrincipal(id.getPrincipal().getName());
                    newId.setCredential((String) id.getCredential().getValue());
                    ret.add(newId);
                }
            }
            return ret;
        }
        if (src == RequestUUID.class && target == String.class) {
            return o.toString();
        }
        throw new IllegalStateException("Can't convert between "+src+" and "+target);
    }

    private static void transfer(Class srcClass, Object src, Object target) {
        Map<String, Method> targetSettersByPropertyName = new HashMap<>();
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().startsWith("set")) {
                targetSettersByPropertyName.put(m.getName().substring(3).toLowerCase(), m);
            }
        }

        for (Method m : srcClass.getDeclaredMethods()) {
//            System.out.println("Processing "+m);
            String candidateMethod = m.getName().toLowerCase();
            if (tryProcess(targetSettersByPropertyName, candidateMethod, m, src, target)) {
                continue;
            }
            if (candidateMethod.startsWith("get")) {
                candidateMethod = candidateMethod.substring(3);
                if (tryProcess(targetSettersByPropertyName, candidateMethod, m, src, target)) {
                    continue;
                }
            }
            if (candidateMethod.startsWith("is") && (m.getReturnType() == Boolean.class || m.getReturnType() == boolean.class)) {
                candidateMethod = candidateMethod.substring(2);
                if (tryProcess(targetSettersByPropertyName, candidateMethod, m, src, target)) {
                    continue;
                }
            }
            throw new RuntimeException("Couldn't find match for source method "+m+" on "+target.getClass());
        }
    }

    private static boolean tryProcess(Map<String, Method> targetSettersByPropertyName, String candidateMethod, Method sourceMethod, Object sourceObject, Object targetObject) {
        if (targetSettersByPropertyName.containsKey(candidateMethod)) {
            Method m = targetSettersByPropertyName.remove(candidateMethod);

            Class sourceType = sourceMethod.getReturnType();
            Class targetType = m.getParameterTypes()[0];
            if (canTransferBetween(targetType,sourceType)) {
                try {
                    m.invoke(targetObject, convert(sourceMethod.invoke(sourceObject), sourceType, targetType));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean canTransferBetween(Class a, Class b) {
        return canTransferBetween(a,b,true);
    }

    private static boolean canTransferBetween(Class a, Class b, boolean flipIfNoMatch) {
        if (a == GeoLocationDetailsTO.class && b == GeoLocationDetails.class) {
            return true;
        }
        if (a == ExecutionContextTO.class && b == ExecutionContext.class) {
            return true;
        }
        if (a == List.class && b == IdentityChain.class) {
            return true;
        }
        if (a == String.class && b == RequestUUID.class) {
            return true;
        }
        if (flipIfNoMatch) {
            return canTransferBetween(b,a,false);
        }
        return a == b;
    }
}
