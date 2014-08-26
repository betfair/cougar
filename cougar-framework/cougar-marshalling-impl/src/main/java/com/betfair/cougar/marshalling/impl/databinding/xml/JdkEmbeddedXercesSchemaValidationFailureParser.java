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

package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarMarshallingException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import org.xml.sax.SAXParseException;

import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 *
 */
public class JdkEmbeddedXercesSchemaValidationFailureParser implements SchemaValidationFailureParser {

    private final ResourceBundle schemaResourceBundle;

    private final Map<String, ServerFaultCode> faultCodes;
    private final Set<String> deserialisationFailures;

    public JdkEmbeddedXercesSchemaValidationFailureParser() {
        schemaResourceBundle = PropertyResourceBundle.getBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", Locale.getDefault());
        faultCodes = new HashMap<>();
        faultCodes.put("cvc-elt.3.1", ServerFaultCode.MandatoryNotDefined);
        faultCodes.put("cvc-complex-type.2.4.b", ServerFaultCode.MandatoryNotDefined);
        faultCodes.put("cvc-datatype-valid.1.2.1", null); // null means check the directional failures set
        deserialisationFailures = new HashSet<>();
        deserialisationFailures.add("cvc-datatype-valid.1.2.1");
    }

    @Override
    public CougarException parse(SAXParseException spe, String format, boolean client) {
        String toParse = spe.getMessage();

        // only worth looking through those we've defined
        for (String key : faultCodes.keySet()) {
            MessageFormat mf = new MessageFormat(schemaResourceBundle.getString(key));
            try {
                Object[] args = mf.parse(toParse);
                String result = mf.format(args);
                if (result.equals(toParse)) {
                    // we've found the key, if we have a mapping then return the appropriate exception, otherwise no point continuing
                    ServerFaultCode sfc = faultCodes.get(key);
                    if (sfc == null && deserialisationFailures.contains(key)) {
                        return CougarMarshallingException.unmarshallingException(format, spe.getMessage(), spe, client);
                    }
                    if (sfc != null) {
                        return new CougarValidationException(sfc, spe);
                    }
                    return null;
                }
            } catch (ParseException e) {
                // no match
            }
        }
        return null;
    }
}
