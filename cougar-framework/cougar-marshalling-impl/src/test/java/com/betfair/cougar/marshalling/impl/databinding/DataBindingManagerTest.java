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

import java.util.*;

import javax.ws.rs.core.MediaType;

import com.betfair.cougar.marshalling.api.databinding.*;
import com.betfair.cougar.test.CougarTestCase;

public class DataBindingManagerTest extends CougarTestCase {

	public void testGetFactory() throws Exception {
		DataBindingManager dbm = DataBindingManager.getInstance();

		DataBindingMap map = new DataBindingMap();
		map.setPreferredContentType("application/xml");
		Set<String> cTypes =  new HashSet<String>();
		cTypes.add("text/xml");
		cTypes.add("application/xml");
		cTypes.add("foo/bar");
		map.setContentTypes(cTypes);
		map.setFactory(new DataBindingFactory() {

			@Override
			public Marshaller getMarshaller() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public FaultMarshaller getFaultMarshaller() {
				// TODO Auto-generated method stub
				return null;
			}

            @Override
            public FaultUnMarshaller getFaultUnMarshaller() {
                return null;
            }

            @Override
			public UnMarshaller getUnMarshaller() {
				// TODO Auto-generated method stub
				return null;
			}});

        List bindingMaps = new ArrayList<Map>();
        bindingMaps.add(map);

		DataBindingManagerHelper dbmh = new DataBindingManagerHelper();

		dbmh.setDataBindingManager(dbm);
		dbmh.setDataBindingMaps(bindingMaps);
		dbmh.afterPropertiesSet();

        DataBindingFactory appXmlFactory = dbm.getFactory(MediaType.valueOf("application/xml"));
		assertNotNull(appXmlFactory);

        //Essentially all the content types defined above should resolve to the same factory
        assertEquals(appXmlFactory, dbm.getFactory(MediaType.valueOf("foo/bar")));
	}

}
