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

package com.betfair.cougar.api.security;

/**
 * Exception to be thrown when an IdentityResolver can't resolve credentials into an
 * identity (for example because the credentials are not valid).
 */
public class InvalidCredentialsException extends Exception {

    // Optional custom fault code to be included in the exception
    private CredentialFaultCode cfc;

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCredentialsException(String message, CredentialFaultCode cfc) {
        super(message);
        this.cfc = cfc;
    }

    public InvalidCredentialsException(String message, Throwable cause, CredentialFaultCode cfc) {
        super(message, cause);
        this.cfc = cfc;
    }

    public CredentialFaultCode getCredentialFaultCode() {
        return cfc;
    }
}
