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

package com.betfair.cougar.core.api.mediatype;

import javax.ws.rs.core.MediaType;
import java.util.Comparator;


public class MediaTypeComparator implements Comparator<MediaType> {

    @Override
    public int compare(MediaType m1, MediaType m2) {

        //Prefer a wildcard to a concrete subtype.
        int dif = compareWildCardStatus(m1.isWildcardType(), m2.isWildcardType());

        //If the MediaTypes are still equal, prefer a wildcard to a concrete subtype.
        if (dif == 0) {
            dif = compareWildCardStatus(m1.isWildcardSubtype(), m2.isWildcardSubtype());
        }

        //If the MediaTypes are still equal, prefer the highest quality level
        if (dif == 0) {
            dif = compareQuality(m1, m2);
        }

        //If all else fails, order by string comparison to ensure consistency.
        if (dif == 0) {
            dif = m1.toString().compareTo(m2.toString());
        }

        return dif;
    }

    private int compareWildCardStatus(boolean wc1, boolean wc2) {
        if (wc1 == wc2) {
            return 0;  //they are equal
        }
        if (wc1) {
            return 1;  //first is preferred
        }
        return -1;     //second is preferred
    }

    private int compareQuality(MediaType m1, MediaType m2) {
        Float qa = getMediaTypeQualityFactor(m1.getParameters().get("q"));
        Float qb = getMediaTypeQualityFactor(m2.getParameters().get("q"));
        return Float.compare(qa, qb) * -1;
    }

    private static float getMediaTypeQualityFactor(String q) {
        if (q != null) {
            try {
                return Float.parseFloat(q);
            } catch (NumberFormatException ex) {
                // Not interested. Stick with the default.
            }
        }
        return 1;
    }

}
