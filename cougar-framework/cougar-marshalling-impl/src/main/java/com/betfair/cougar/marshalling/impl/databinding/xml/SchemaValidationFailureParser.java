package com.betfair.cougar.marshalling.impl.databinding.xml;

import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarValidationException;
import org.xml.sax.SAXParseException;

import javax.ws.rs.core.MediaType;

/**
 *
 */
public interface SchemaValidationFailureParser {
    CougarException parse(SAXParseException spe, String format, boolean client);
}
