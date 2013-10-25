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

package com.betfair.cougar.baseline.security;

import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.transport.api.protocol.http.soap.SoapIdentityTokenResolver;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleSoapIdentityTokenResolver implements SoapIdentityTokenResolver {

    @Override
    public List<IdentityToken> resolve(OMElement input, X509Certificate[] certificateChain) {
        List<IdentityToken> tokens = new ArrayList<IdentityToken>();
		if (input != null) {
			for (Iterator i = input.getChildElements(); i.hasNext();) {
				OMElement element = (OMElement) i.next();
                for (SimpleIdentityTokenName t: SimpleIdentityTokenName.values()) {
                    if (element.getLocalName().equalsIgnoreCase(t.name())) {
                        tokens.add(new IdentityToken(t.name(), element.getText()));
                        break;

                    }
                }
			}
		}
        return tokens;
    }

    @Override
    public void rewrite(List<IdentityToken> credentials, OMElement output) {
        OMFactory factory = output.getOMFactory();
        for (IdentityToken ik: credentials) {
            OMElement e = factory.createOMElement(ik.getName(), output.getNamespace());
            e.setText(ik.getValue());
            output.addChild(e);
        }
    }

    @Override
    public boolean isRewriteSupported() {
        return true;
    }
}
