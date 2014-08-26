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

package com.betfair.cougar.marshalling.impl.util;

import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.api.transcription.EnumDerialisationException;
import com.betfair.cougar.core.api.transcription.EnumUtils;
import com.betfair.cougar.util.dates.DateTimeUtility;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class BindingUtils {

	private static final Pattern csv = Pattern.compile(",");


	public static Object convertToSimpleType(Class<?> clazz, Class<?> genericClass, String name, String value, boolean unescapeStrings, boolean hardFailEnums, String format, boolean client) throws IllegalArgumentException {
        Object result = null;
        if (value != null) {
        	try {
	        	if (clazz == String.class) {
		            try {
		            	if (unescapeStrings) {
		            		result = URLDecoder.decode(value, "UTF-8");
		            	} else {
		            		result = value;
		            	}
					} catch (UnsupportedEncodingException e) {
						throw new CougarFrameworkException("Unsupported encoding", e);
					}
	        	}
				else if (clazz == Integer.class) {
		            result = Integer.valueOf(value);
		        } else if (clazz == Byte.class) {
		            result = Byte.valueOf(value);
		        } else if (clazz == Short.class) {
		            result = Short.valueOf(value);
		        } else if (clazz == Long.class) {
		            result = Long.valueOf(value);
		        } else if (clazz == Float.class) {
		        	try {
		        		result = Float.valueOf(value);
		        	} catch (NumberFormatException e) {
		        		result = checkXMLInfinity(clazz, value);
		        		if (result == null) {
		        			throw e;
		        		}
		        	}
		        } else if (clazz == Double.class) {
		        	try {
			            result = Double.valueOf(value);
		        	} catch (NumberFormatException e) {
		        		result = checkXMLInfinity(clazz, value);
		        		if (result == null) {
		        			throw e;
		        		}
		        	}
		        } else if (clazz == Boolean.class) {
		        	// Boolean.valueOf only checks for true, so a value of "foo" will return Boolean.FALSE
		        	if (Boolean.valueOf(value)) {
		        		result = Boolean.TRUE;
		        	} else if (value.equalsIgnoreCase("false")) {
		        		result = Boolean.FALSE;
		        	} else {
	        			throw newValidationException(name, value, clazz, null, format, client);
		        	}
		        } else if (clazz == Character.class) {
		            result = value.charAt(0);
		        } else if (clazz.isEnum()) {
                    result = EnumUtils.readEnum((Class) clazz, value, hardFailEnums);
		        } else if (clazz == List.class || clazz == Set.class) {
                    int start=0,end=value.length();
                    if (value.startsWith("[")) {
                        start=1;
                    }
                    if (value.endsWith("]")) {
                        end--;
                    }
		            Collection<Object> collection = (clazz == List.class) ? new ArrayList<Object>() : new HashSet<Object>();
		            for (String val: csv.split(value.substring(start,end))) {
		            	val = val.trim();
		            	if (genericClass == String.class || val.length() > 0) {
		            		collection.add(convertToSimpleType(genericClass, null, name + "[member]",  val.trim(), unescapeStrings, hardFailEnums, format, client));
		            	}
		            }
		            if (collection.size() > 0) {
		            	result = collection ;
		            }

		        } else if(clazz == java.util.Date.class) {
						result = DateTimeUtility.parse(value);
		        } else {
		        	throw newValidationException(name, value, clazz, null, format, client);
		        }
        	} catch (RuntimeException ex) {
        		if (ex instanceof CougarMarshallingException || ex instanceof EnumDerialisationException) {
        			throw ex;
        		} else {
        			throw newValidationException(name, value, clazz, ex, format, client);
        		}
        	}

        }
        return result;
    }


	private final static CougarMarshallingException newValidationException(String name, String value, Class clazz, Exception originalException, String format, boolean client) {
		final StringBuilder msg = new StringBuilder("Unable to convert '");
		msg.append(value).append("' to ").append(clazz.getName()).append(" for parameter: " + name);

        return CougarMarshallingException.unmarshallingException(format, msg.toString(), originalException, client);
	}



    private static final Object checkXMLInfinity(Class<?> clazz, String value) {
    	boolean returnFloat = Float.class.equals(clazz);
    	if (value.equals("INF")) {
    		if (returnFloat) {
    			return Float.POSITIVE_INFINITY;
    		} else {
    			return Double.POSITIVE_INFINITY;
    		}
    	} else if (value.equals("-INF")) {
    		if (returnFloat) {
    			return Float.NEGATIVE_INFINITY;
    		} else {
    			return Double.NEGATIVE_INFINITY;
    		}
    	}
		return null;

    }
}
