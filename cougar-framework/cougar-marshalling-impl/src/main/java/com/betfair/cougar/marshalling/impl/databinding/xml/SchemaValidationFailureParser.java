package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.core.api.exception.CougarValidationException;
import org.xml.sax.SAXParseException;

/**
 *
 */
public interface SchemaValidationFailureParser {
    CougarValidationException parse(SAXParseException spe, XmlSource source);
    static enum XmlSource { SOAP, RESCRIPT }
}
