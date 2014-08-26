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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaTypeUtils {
    public static final MediaType MEDIA_WILDCARD = new MediaType();

    private static MediaTypeComparator mtc = new MediaTypeComparator();

    public static List<MediaType> parseMediaTypes(String types) {
        if (types == null || types.length() == 0) {
            return getMediaTypes(null);
        }
        return getMediaTypes(types.split("\\,"));
    }

    public static List<MediaType> getMediaTypes(String[] values) {
        List<MediaType> types = new ArrayList<MediaType>();
        if (values == null || values.length == 0) {
            types.add(MEDIA_WILDCARD);
        } else {
            for (String value : values) {
                types.add(MediaType.valueOf(value));
            }
        }
        return sortMediaTypes(types);
    }

    public static boolean isValid(List<MediaType> consumes, MediaType contentType) {
        for (MediaType allowed : consumes) {
            if (contentType.isCompatible(allowed)) {
                return true;
            }
        }
        return false;
    }

    public static MediaType getResponseMediaType(List<MediaType> produces, List<MediaType> accept) {
        float bestQ = Float.NEGATIVE_INFINITY;
        MediaType type = null;
        for (MediaType prod : produces) {
            for (MediaType usr : accept) {
                if (usr.isCompatible(prod)) {

                    String baseType = prod.getType().equals(MediaType.MEDIA_TYPE_WILDCARD)
                            ? usr.getType() : prod.getType();
                    String subType = prod.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)
                            ? usr.getSubtype() : prod.getSubtype();
                    float q = getMediaTypeQualityFactor(usr.getParameters().get("q"));

                    // Ensure that wildcard types are not returned unless absolutely necessary.
                    if (baseType.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                        q = q - 10000;
                    } else if (subType.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                        q = q - 100;
                    }
                    if (q > bestQ) {
                        type = new MediaType(baseType, subType);
                        bestQ = q;
                    }
                }
            }
        }
        return type;
    }

    private static List<MediaType> sortMediaTypes(List<MediaType> types) {
        Collections.sort(types, mtc);
        return types;
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

