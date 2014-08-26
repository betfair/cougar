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

package com.betfair.cougar.client.query;

import com.betfair.cougar.marshalling.api.databinding.Marshaller;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Generates an HTTP query string based on provided map of key/value pairs
 *
 */
public class DefaultQueryStringGeneratorImpl extends AbstractQueryStringGenerator {
    private static final String UTF8 = "utf-8";
    private Marshaller marshaller;

    public DefaultQueryStringGeneratorImpl(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

	protected String parseValue(Object o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            marshaller.marshall(bos, o, UTF8, true);
            return URLEncoder.encode(new String(bos.toByteArray()).replace("\"",""), UTF8);
        } catch (UnsupportedEncodingException ignored) {
            return null; //Idiotic checked exception to the fore
        }
	}
}