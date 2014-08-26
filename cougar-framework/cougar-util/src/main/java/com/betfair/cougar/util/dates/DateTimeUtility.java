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

package com.betfair.cougar.util.dates;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

public class DateTimeUtility {

    private static DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withZone(DateTimeZone.UTC);
    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    public static String encode(Date date) {
        String dateStr=formatter.print(date.getTime());
        if(!dateStr.endsWith("Z")){
            dateStr=dateStr+"Z";
        }
        return dateStr;
    }

    public static Date parse(String string) {
        return parser.parseDateTime(string).toDate();
    }
}
