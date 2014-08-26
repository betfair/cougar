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

package com.betfair.cougar.transport.impl.protocol.http;

import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.protocol.http.HttpCommand;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Command validator to enable requirement for SSL. Has support for allowing termination on a Netscaler or equivalent
 * device.
 */
@ManagedResource
public class HttpsCommandValidator implements CommandValidator<HttpCommand> {

    private boolean enabled = true;
    private boolean allowExternalTermination = true;
    private String externalTerminationHeader = "Front-End-Https";

    @Override
    public void validate(HttpCommand command) throws CougarException {
        if (enabled) {
            // https is obvious SSL
            if (!command.getRequest().getScheme().equals("https")) {
                // if over http, then if we support external termination, then externalTerminationHeader must be present
                // to indicate it was terminated
                if (!allowExternalTermination || command.getRequest().getHeader(externalTerminationHeader) == null) {
                    throw new CougarServiceException(ServerFaultCode.SecurityException, "This service requires a secure communication protocol");
                }
            }
        }
    }

    /**
     * Allows control of whether this validator in enabled at runtime (rather than at build time). Defaults to <code>true</code>.
     */
    @ManagedAttribute
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Whether to enable termination of SSL on an external device (such as a Netscaler). Defaults to <code>true</code>.
     * @param allowExternalTermination
     */
    @ManagedAttribute
    public void setAllowExternalTermination(boolean allowExternalTermination) {
        this.allowExternalTermination = allowExternalTermination;
    }

    /**
     * The header that an external device sends through in the case of external termination of SSL. Defaults to <code>Front-End-Https</code>.
     */
    @ManagedAttribute
    public void setExternalTerminationHeader(String externalTerminationHeader) {
        this.externalTerminationHeader = externalTerminationHeader;
    }

    @ManagedAttribute
    public boolean isEnabled() {
        return enabled;
    }

    @ManagedAttribute
    public boolean isAllowExternalTermination() {
        return allowExternalTermination;
    }

    @ManagedAttribute
    public String getExternalTerminationHeader() {
        return externalTerminationHeader;
    }
}
