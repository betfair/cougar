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

package com.betfair.testing.utils.cougar.manager;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class TraceLogTailer extends LogTailer<TraceLogRequirement> {

    public static final String MESSAGE = "_MESSAGE";
    public static final String THREAD_ID = "_THREAD_ID";
    public static final String UUID = "_UUID";


    public TraceLogTailer(File toRead) throws IOException {
        super(toRead, 60000L);
    }

    @Override
    protected Map<String, String> getFieldsForLine(String s) {
        int comma = s.indexOf(": ");
        if (comma > 0) {
            Map<String, String> ret = new HashMap<String, String>();
            ret.put(LogTailer.DATE_FIELD, null);
            String[] preFields = s.substring(comma).split(" ");
            ret.put(THREAD_ID, preFields[0]);
            ret.put(UUID, preFields[2]);
            ret.put(MESSAGE, s.substring(comma+2));
            return ret;
        }
        else {
            return null;
        }
    }

    @Override
    protected Timestamp toDate(String dateFieldValue) throws ParseException {
        return new Timestamp(System.currentTimeMillis());
    }

    @Override
    protected boolean matches(LogLine line, TraceLogRequirement requirement) {
        if (requirement.message != null) {
            if (!requirement.message.equals(line.getFields().get(MESSAGE))) {
                return false;
            }
        }
        return true;
    }

}
