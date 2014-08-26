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

package com.betfair.cougar.core.impl.security;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.Rdn;
import java.util.List;

/**
 *
 */
public class CommonNameCertInfoExtractor implements CertInfoExtractor {
    @Override
    public String extractCertInfo(List<Rdn> rdns) throws NamingException {
        for (Rdn rdn : rdns) {
            Attributes attrs = rdn.toAttributes();
            Attribute attr = attrs.get("CN");
            if (attr != null) {
                Object o = attr.get();
                if (o != null) {
                    return o.toString();
                }
            }
        }
        return null;
    }
}
