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

package com.betfair.cougar.marshalling.impl.databinding.json;

import com.betfair.cougar.util.dates.DateTimeUtility;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;


public class JSONDateFormat extends DateFormat {

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        toAppendTo.append(DateTimeUtility.encode(date));
        return toAppendTo;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        pos.setIndex(pos.getIndex() + source.length());
        return DateTimeUtility.parse(source);
    }
}
