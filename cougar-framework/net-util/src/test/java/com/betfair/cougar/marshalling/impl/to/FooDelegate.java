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

package com.betfair.cougar.marshalling.impl.to;

import java.util.Map;

public interface  FooDelegate  {

    public String getFooName()  ;
    public void setFooName(String marketId);
    public Bar getBar()  ;
    public void setBar(Bar rates);
	public void setBarBazMap(Map<Bar, Baz> barBazMap);
	public Map<Bar, Baz> getBarBazMap();
	public byte[] getPrimitiveArray();
	public void setPrimitiveArray(byte[] bytes);





}

