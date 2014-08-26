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
import java.util.HashMap;
import java.util.Map;


public class DataBindingManager {

    private static final DataBindingManager instance = new DataBindingManager();
    private final Map<String, DataBindingFactory> factories;

    public static DataBindingManager getInstance() {
        return instance;
    }


    private DataBindingManager() {
        factories = new HashMap<>();
    }

    public void addBindingMap(DataBindingMap map) {
    	for (String contentType : map.getContentTypes()) {
	        factories.put(contentType, map.getFactory());
    	}
    }

    public DataBindingFactory getFactory(MediaType mediaType) {
    	return factories.get(mediaType.toString());
    }
}
