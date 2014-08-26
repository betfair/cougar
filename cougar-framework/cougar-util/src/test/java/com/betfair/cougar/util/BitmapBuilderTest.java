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

package com.betfair.cougar.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BitmapBuilderTest {

	@Test
	public void testListToMap(){
		int[] list = new int[]{0,0,1,0,0,0,1,1,0,1,1,0,1,0,1,1};
		int[] words = BitmapBuilder.listToMap(list);
		assertEquals(2,words.length);
		assertEquals(35,words[0]);
		assertEquals(107,words[1]);
	}

	@Test
	public void testListToMapNeedsPadding(){
		int[] list = new int[]{0,0,1,0,0,0,1,1,0,1,1,0,1,0,1};
		int[] words = BitmapBuilder.listToMap(list);
		assertEquals(2,words.length);
		assertEquals(35,words[0]);
		assertEquals(106,words[1]);
	}

	@Test
	public void testListToMapNeedsPaddingOneWord(){
		int[] list = new int[]{0,0,1,0,0,0};
		int[] words = BitmapBuilder.listToMap(list);
		assertEquals(1,words.length);
		assertEquals(32,words[0]);
	}

	@Test
	public void testMapToList(){
		int[] expectedlist = new int[]{0,0,1,0,0,0,1,1,0,1,1,0,1,0,1,0};
		int[] words = new int[]{35,106};
		int[] list = BitmapBuilder.mapToList(words);
		assertArrayEquals(expectedlist,list);
	}
}
