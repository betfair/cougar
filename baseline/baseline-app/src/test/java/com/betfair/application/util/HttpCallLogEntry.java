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

package com.betfair.application.util;

public class HttpCallLogEntry implements Comparable<HttpCallLogEntry>{
    private String method;
    private String protocol;
    
    
    public String getMethod() {
		return method;
	}
	
    public void setMethod(String method) {
		this.method = method;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpCallLogEntry) {
            HttpCallLogEntry cle = (HttpCallLogEntry) obj;
            return cle.method.equals(method) &&
                    cle.protocol.equals(protocol);
            
        }
        return false;
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    public int compareTo(HttpCallLogEntry o) {
        int retVal = method.compareTo(o.method);
        if (retVal == 0) {
            retVal = protocol.compareTo(o.protocol);
        }
        return retVal;
    }

}
