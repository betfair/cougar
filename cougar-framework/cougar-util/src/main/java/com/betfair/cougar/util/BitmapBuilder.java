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

import java.util.Arrays;

public class BitmapBuilder {

    public static final int WORD_LENGTH = 8;

    /**
     * Increases the array size to a multiple of WORD_LENGTH
     *
     * @param list
     * @return
     */
    private static int[] pad(int[] list) {
        double m = list.length / (double)WORD_LENGTH;
        int size = (int)Math.ceil(m);
        int[] ret = new int[size * WORD_LENGTH];
        Arrays.fill(ret, 0);
        System.arraycopy(list,0,ret,0,list.length);
        return ret;
    }

    /**
     * Builds a bit map from the given list. The list should values of 0 or 1s
     *
     * @param list
     * @return
     */
    public static int[] listToMap(int[] list) {
        list = pad(list);
        int[] bitMap = new int[(int)(list.length / WORD_LENGTH)];
        Arrays.fill(bitMap, 0);
        int j = -1;
        for (int i = 0; i < list.length; i++) {
            if (i % WORD_LENGTH == 0) {
                j++;
            }
            bitMap[j] |= (list[i] << ((WORD_LENGTH - 1) - (i % WORD_LENGTH)));
        }
        return bitMap;
    }

    /**
     * Builds a list from the given bit map.
     * The list will contain values of 0 or 1s
     *
     * @param list
     * @return
     */
    public static int[] mapToList(int[] bitMap) {
        int[] list = new int[(int)(bitMap.length * WORD_LENGTH)];
        Arrays.fill(list, 0);
        int j = -1;
        for (int i = 0; i < list.length; i++) {
            if (i % WORD_LENGTH == 0) {
                j++;
            }
            list[i] = (bitMap[j] & (1 << ((WORD_LENGTH - 1) - (i % WORD_LENGTH)))) == 0 ? 0 : 1;
        }
        return list;
    }
}