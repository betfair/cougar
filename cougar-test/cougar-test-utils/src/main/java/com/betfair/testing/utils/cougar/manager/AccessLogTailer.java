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
public class AccessLogTailer extends LogTailer<AccessLogRequirement> {

    public static final String REQUEST_UUID = "_REQUEST_UUID";
    public static final String REQUEST_URI = "_REQUEST_URI";
    public static final String COMPRESSION = "_COMPRESSION";
    public static final String REMOTE_ADDRESS = "_REMOTE_ADDRESS";
    public static final String RESOLVED_ADDRESS = "_RESOLVED_ADDRESS";
    public static final String RESOLVED_COUNTRY = "_RESOLVED_COUNTRY";
    public static final String RESPONSE_CODE = "_RESPONSE_CODE";
    public static final String PROCESSING_TIME_NANOS = "_PROCESSING_TIME_NANOS";
    public static final String BYTES_READ = "_BYTES_READ";
    public static final String BYTES_WRITTEN = "_BYTES_WRITTEN";
    public static final String REQUEST_MEDIA_TYPE = "_REQUEST_MEDIA_TYPE";
    public static final String RESPONSE_MEDIA_TYPE = "_RESPONSE_MEDIA_TYPE";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public AccessLogTailer(File toRead) throws IOException {
        super(toRead, 60000L);
    }

    @Override
    protected Map<String, String> getFieldsForLine(String s) {
        String[] fields = s.split(",");
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(LogTailer.DATE_FIELD, fields[0]);
        ret.put(REQUEST_UUID, fields[1]);
        ret.put(REQUEST_URI, fields[2]);
        ret.put(COMPRESSION, fields[3]);
        ret.put(REMOTE_ADDRESS, fields[4]);
        ret.put(RESOLVED_ADDRESS, fields[5]);
        ret.put(RESOLVED_COUNTRY, fields[6]);
        ret.put(RESPONSE_CODE, fields[7]);
        ret.put(PROCESSING_TIME_NANOS, fields[8]);
        ret.put(BYTES_READ, fields[9]);
        ret.put(BYTES_WRITTEN, fields[10]);
        ret.put(REQUEST_MEDIA_TYPE, fields[11]);
        ret.put(RESPONSE_MEDIA_TYPE, fields[12]);
        for (int i=13; i<fields.length; i++) {
            String kv = fields[i];
            int equals = kv.indexOf("=");
            if (equals > 0) {
                String k = kv.substring(0, equals);
                String v = kv.substring(equals+1);
                ret.put(k,v);
            }
        }
        return ret;
    }

    @Override
    protected Timestamp toDate(String dateFieldValue) throws ParseException {
        return new Timestamp(dateFormat.parse(dateFieldValue).getTime());
    }

    @Override
    protected boolean matches(LogLine line, AccessLogRequirement requirement) {
        if (requirement.resolvedAddress != null) {
            if (!requirement.resolvedAddress.equals(line.getFields().get(RESOLVED_ADDRESS))) {
                return false;
            }
        }
        if (requirement.resolvedCountry != null) {
            if (!requirement.resolvedCountry.equals(line.getFields().get(RESOLVED_COUNTRY))) {
                return false;
            }
        }
        if (requirement.requestUri != null) {
            if (!requirement.requestUri.equals(line.getFields().get(REQUEST_URI))) {
                return false;
            }
        }
        if (requirement.responseCode != null) {
            if (!requirement.responseCode.equals(line.getFields().get(RESPONSE_CODE))) {
                return false;
            }
        }
        return true;
    }

}
