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

package com.betfair.cougar.marshalling.impl.databinding;


import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;

import javax.ws.rs.core.MediaType;
import java.util.Set;


public class DataBindingMap {
    private Set<String> contentTypes;
    private String preferredContentType;
    private DataBindingFactory factory;

    public Set<String> getContentTypes() {
        return contentTypes;
    }
    public void setContentTypes(Set<String> contentTypes) {
        this.contentTypes = contentTypes;
    }
    public MediaType getPreferredMediaType() {
		return MediaType.valueOf(preferredContentType);
	}
    public String getPreferredContentType() {
		return preferredContentType;
	}
	public void setPreferredContentType(String preferredContentType) {
		this.preferredContentType = preferredContentType;
	}
	public DataBindingFactory getFactory() {
        return factory;
    }
    public void setFactory(DataBindingFactory factory) {
        this.factory = factory;
    }

}
