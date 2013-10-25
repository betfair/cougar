package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import org.xml.sax.SAXParseException;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 */
public class JdkEmbeddedXercesSchemaValidationFailureParser implements SchemaValidationFailureParser {

    private final ResourceBundle schemaResourceBundle;

    private final Map<String, ServerFaultCode> faultCodesSoap;
    private final Map<String, ServerFaultCode> faultCodesRescript;

    public JdkEmbeddedXercesSchemaValidationFailureParser() {
        schemaResourceBundle = PropertyResourceBundle.getBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", Locale.getDefault());
        faultCodesSoap = new HashMap<>();
        faultCodesSoap.put("cvc-elt.3.1",ServerFaultCode.MandatoryNotDefined);
        faultCodesSoap.put("cvc-complex-type.2.4.b",ServerFaultCode.MandatoryNotDefined);
        faultCodesSoap.put("cvc-datatype-valid.1.2.1",ServerFaultCode.SOAPDeserialisationFailure); // todo: would be nice to have a specific error message for this
        faultCodesRescript = new HashMap<>();
        faultCodesRescript.put("cvc-elt.3.1",ServerFaultCode.MandatoryNotDefined);
        faultCodesRescript.put("cvc-complex-type.2.4.b",ServerFaultCode.MandatoryNotDefined);
        faultCodesRescript.put("cvc-datatype-valid.1.2.1",ServerFaultCode.XMLDeserialisationFailure); // todo: would be nice to have a specific error message for this
    }

    @Override
    public CougarValidationException parse(SAXParseException spe, XmlSource source) {
        String toParse = spe.getMessage();

        Map<String, ServerFaultCode> faultCodes = null;
        switch (source) {
            case RESCRIPT:
                faultCodes = faultCodesRescript;
                break;
            case SOAP:
                faultCodes = faultCodesSoap;
                break;
            default:
                throw new IllegalArgumentException("Unrecognised source: "+source);
        }
        // only worth looking through those we've defined
        for (String key : faultCodes.keySet()) {
            MessageFormat mf = new MessageFormat(schemaResourceBundle.getString(key));
            try {
                Object[] args = mf.parse(toParse);
                String result = mf.format(args);
                if (result.equals(toParse)) {
                    // we've found the key, if we have a mapping then return the appropriate exception, otherwise no point continuing
                    ServerFaultCode sfc = faultCodes.get(key);
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
