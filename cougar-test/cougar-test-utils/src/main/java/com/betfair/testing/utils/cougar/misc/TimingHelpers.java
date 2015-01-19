/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.testing.utils.cougar.misc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 */
public class TimingHelpers {

    public static String convertUTCDateTimeToCougarFormat(int year, int month, int day, int hour, int minute, int second, int millis) {
        /*
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month-1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millis);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return sdf.format(c.getTime());
        */
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("-");
        if (month < 10) {
            sb.append("0");
        }
        sb.append(month).append("-");
        if (day < 10) {
            sb.append("0");
        }
        sb.append(day).append("T");
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour).append(":");
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute).append(":");
        if (second < 10) {
            sb.append("0");
        }
        sb.append(second).append(".");
        if (millis < 100) {
            sb.append("0");
        }
        if (millis < 10) {
            sb.append("0");
        }
        sb.append(millis).append("Z");
        return sb.toString();
    }

    public static String convertUTCDateTimeToLocalTimezoneXMLSchema2(int year, int month, int day, int hour, int minute, int second, int millis) {
        return convertUTCDateTimeToCougarFormat(year, month, day, hour, minute, second, millis);
    }
}
