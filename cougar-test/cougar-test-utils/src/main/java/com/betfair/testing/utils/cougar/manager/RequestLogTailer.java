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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class RequestLogTailer extends LogTailer<RequestLogRequirement> {

    public static final String REQUEST_UUID = "_REQUEST_UUID";
    public static final String SERVICE_VERSION = "_SERVICE_VERSION";
    public static final String OPERATION = "_OPERATION";
    public static final String FAULT_CODE = "_FAULT_CODE";
    public static final String OPERATION_TIME_NS = "_OPERATION_TIME_NS";
    public static final String LOG_EXTENSION_FIELD_ = "_LOG_EXTENSION_FIELD_";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public RequestLogTailer(File toRead) throws IOException {
        super(toRead, 60000L);
    }

    @Override
    protected Map<String, String> getFieldsForLine(String s) {
        String[] fields = s.split(",");
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(LogTailer.DATE_FIELD, fields[0]);
        ret.put(REQUEST_UUID, fields[1]);
        ret.put(SERVICE_VERSION, fields[2]);
        ret.put(OPERATION, fields[3]);
        ret.put(FAULT_CODE, fields[4]);
        ret.put(OPERATION_TIME_NS, fields[5]);
        for (int i=6; i<fields.length; i++) {
            String v = fields[i];
            ret.put(LOG_EXTENSION_FIELD_+(i-5), v);
        }
        return ret;
    }

    @Override
    protected Timestamp toDate(String dateFieldValue) throws ParseException {
        return new Timestamp(dateFormat.parse(dateFieldValue).getTime());
    }

    @Override
    protected boolean matches(LogLine line, RequestLogRequirement requirement) {
        if (requirement.operation != null) {
            if (!requirement.operation.equals(line.getFields().get(OPERATION))) {
                return false;
            }
        }
        if (requirement.serviceVersion != null) {
            if (!requirement.serviceVersion.equals(line.getFields().get(SERVICE_VERSION))) {
                return false;
            }
        }
        return true;
    }

}
