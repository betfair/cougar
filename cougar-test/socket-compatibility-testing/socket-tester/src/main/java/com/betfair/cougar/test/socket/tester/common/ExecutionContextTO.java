/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.tester.common;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
*
*/
public class ExecutionContextTO implements Transcribable {

    private GeoLocationDetailsTO location;
    private List<IdentityTO> identity;
    private String requestUuid;
    private Date receivedTime;
    private Date requestTime;
    private boolean traceLoggingEnabled;
    private int transportSecurityStrengthFactor;
    private boolean transportSecure;

    private static final Parameter __locationParam = new Parameter("location",new ParameterType(GeoLocationDetailsTO.class, null ),true);
    private static final Parameter __identityParam = new Parameter("identity",new ParameterType(List.class, new ParameterType [] { new ParameterType(IdentityTO.class, null ) } ),true);
    private static final Parameter __requestUuidParam = new Parameter("requestUuid",new ParameterType(String.class, null ),true);
    private static final Parameter __receivedTimeParam = new Parameter("receivedTime",new ParameterType(Date.class, null ),true);
    private static final Parameter __requestTimeParam = new Parameter("requestTime",new ParameterType(Date.class, null ),true);
    private static final Parameter __traceLoggingEnabledParam = new Parameter("traceLoggingEnabled",new ParameterType(Boolean.class, null ),true);
    private static final Parameter __transportSecurityStrengthFactorParam = new Parameter("transportSecurityStrengthFactor",new ParameterType(Integer.class, null ),true);
    private static final Parameter __transportSecureParam = new Parameter("transportSecure",new ParameterType(Boolean.class, null ),true);

    public GeoLocationDetailsTO getLocation() {
        return location;
    }

    public void setLocation(GeoLocationDetailsTO location) {
        this.location = location;
    }

    public List<IdentityTO> getIdentity() {
        return identity;
    }

    public void setIdentity(List<IdentityTO> identity) {
        this.identity = identity;
    }

    public String getRequestUuid() {
        return requestUuid;
    }

    public void setRequestUuid(String requestUuid) {
        this.requestUuid = requestUuid;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public boolean isTraceLoggingEnabled() {
        return traceLoggingEnabled;
    }

    public void setTraceLoggingEnabled(boolean traceLoggingEnabled) {
        this.traceLoggingEnabled = traceLoggingEnabled;
    }

    public int getTransportSecurityStrengthFactor() {
        return transportSecurityStrengthFactor;
    }

    public void setTransportSecurityStrengthFactor(int transportSecurityStrengthFactor) {
        this.transportSecurityStrengthFactor = transportSecurityStrengthFactor;
    }

    public boolean isTransportSecure() {
        return transportSecure;
    }

    public void setTransportSecure(boolean transportSecure) {
        this.transportSecure = transportSecure;
    }

    public static final Parameter[] PARAMETERS = new Parameter[] { __locationParam, __identityParam, __requestUuidParam, __receivedTimeParam, __requestTimeParam, __traceLoggingEnabledParam, __transportSecurityStrengthFactorParam, __transportSecureParam };

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getLocation(), __locationParam, client);
        out.writeObject(getIdentity(), __identityParam, client);
        out.writeObject(getRequestUuid(), __requestUuidParam, client);
        out.writeObject(getReceivedTime(), __receivedTimeParam, client);
        out.writeObject(getRequestTime(), __requestTimeParam, client);
        out.writeObject(isTraceLoggingEnabled(), __traceLoggingEnabledParam, client);
        out.writeObject(getTransportSecurityStrengthFactor(), __transportSecurityStrengthFactorParam, client);
        out.writeObject(isTransportSecure(), __transportSecureParam, client);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        setLocation((GeoLocationDetailsTO) in.readObject(__locationParam, client));
        setIdentity((List<IdentityTO>) in.readObject(__identityParam, client));
        setRequestUuid((String) in.readObject(__requestUuidParam, client));
        setReceivedTime((Date) in.readObject(__receivedTimeParam, client));
        setRequestTime((Date) in.readObject(__requestTimeParam, client));
        setTraceLoggingEnabled((Boolean) in.readObject(__traceLoggingEnabledParam, client));
        setTransportSecurityStrengthFactor((Integer) in.readObject(__transportSecurityStrengthFactorParam, client));
        setTransportSecure((Boolean) in.readObject(__transportSecureParam, client));
    }

    public static final ServiceVersion SERVICE_VERSION = Common.SERVICE_VERSION;

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

    @Override
    public String toString() {
        return "ExecutionContextTO{" +
                "location=" + location +
                ", identity=" + identity +
                ", requestUuid='" + requestUuid + '\'' +
                ", receivedTime=" + receivedTime +
                ", requestTime=" + requestTime +
                ", traceLoggingEnabled=" + traceLoggingEnabled +
                ", transportSecurityStrengthFactor=" + transportSecurityStrengthFactor +
                ", transportSecure=" + transportSecure +
                '}';
    }
}
