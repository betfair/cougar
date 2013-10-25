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
import com.betfair.cougar.transport.api.protocol.http.rescript.RescriptIdentityTokenResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class HttpCookieIdentityResolver implements RescriptIdentityTokenResolver {
    
    @Override
    public List<IdentityToken> resolve(HttpServletRequest input, X509Certificate[] transportAuthTokens) {
        List<IdentityToken> tokens = new ArrayList<IdentityToken>();

        Cookie[] cookies = input.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieValue = cookie.getValue();
                String cookieName = cookie.getName();
                for (SimpleIdentityTokenName t: SimpleIdentityTokenName.values()) {
                    if (("X-Token-"+t.name()).equals(cookieName) && cookieValue != null && cookieValue.length() > 0) {
                        tokens.add(new IdentityToken(cookieName, cookieValue));
                    }
                }
            }
        }
        return tokens;
    }

    @Override
    public void rewrite(List<IdentityToken> credentials, HttpServletResponse output) {
    }

    @Override
    public boolean isRewriteSupported() {
        return false;
    }
    
}
