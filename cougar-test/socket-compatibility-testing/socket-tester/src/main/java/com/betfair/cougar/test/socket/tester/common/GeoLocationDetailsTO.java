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

import java.util.List;
import java.util.Set;

/**
*
*/
public class GeoLocationDetailsTO implements Transcribable {
    private String remoteAddr;
    private List<String> resolvedAddresses;
    private String country;
    private boolean lowConfidenceGeoLocation;
    private String location;
    private String inferredCountry;

    private static final Parameter __remoteAddrParam = new Parameter("remoteAddr",new ParameterType(String.class, null ),true);
    private static final Parameter __resolvedAddressesParam = new Parameter("resolvedAddresses",new ParameterType(List.class, new ParameterType [] { new ParameterType(String.class, null ) } ),true);
    private static final Parameter __countryParam = new Parameter("country",new ParameterType(String.class, null ),true);
    private static final Parameter __lowConfidenceGeoLocationParam = new Parameter("lowConfidenceGeoLocation",new ParameterType(Boolean.class, null ),true);
    private static final Parameter __locationParam = new Parameter("location",new ParameterType(String.class, null ),true);
    private static final Parameter __inferredCountryParam = new Parameter("inferredCountry",new ParameterType(String.class, null ),true);

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public List<String> getResolvedAddresses() {
        return resolvedAddresses;
    }

    public void setResolvedAddresses(List<String> resolvedAddresses) {
        this.resolvedAddresses = resolvedAddresses;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isLowConfidenceGeoLocation() {
        return lowConfidenceGeoLocation;
    }

    public void setLowConfidenceGeoLocation(boolean lowConfidenceGeoLocation) {
        this.lowConfidenceGeoLocation = lowConfidenceGeoLocation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInferredCountry() {
        return inferredCountry;
    }

    public void setInferredCountry(String inferredCountry) {
        this.inferredCountry = inferredCountry;
    }

    public static final Parameter[] PARAMETERS = new Parameter[] { __remoteAddrParam, __resolvedAddressesParam, __countryParam, __lowConfidenceGeoLocationParam, __locationParam, __inferredCountryParam };

    public Parameter[] getParameters() {
        return PARAMETERS;
    }

    public void transcribe(TranscriptionOutput out, Set<TranscribableParams> params, boolean client) throws Exception {
        out.writeObject(getRemoteAddr(), __remoteAddrParam, client);
        out.writeObject(getResolvedAddresses(), __resolvedAddressesParam, client);
        out.writeObject(getCountry(), __countryParam, client);
        out.writeObject(isLowConfidenceGeoLocation(), __lowConfidenceGeoLocationParam, client);
        out.writeObject(getLocation(), __locationParam, client);
        out.writeObject(getInferredCountry(), __inferredCountryParam, client);
    }

    public void transcribe(TranscriptionInput in, Set<TranscribableParams> params, boolean client) throws Exception {
        setRemoteAddr((String) in.readObject(__remoteAddrParam, client));
        setResolvedAddresses((List<String>) in.readObject(__resolvedAddressesParam, client));
        setCountry((String) in.readObject(__countryParam, client));
        setLowConfidenceGeoLocation((Boolean) in.readObject(__lowConfidenceGeoLocationParam, client));
        setLocation((String) in.readObject(__locationParam, client));
        setInferredCountry((String) in.readObject(__inferredCountryParam, client));
    }

    public static final ServiceVersion SERVICE_VERSION = Common.SERVICE_VERSION;

    public ServiceVersion getServiceVersion() {
        return SERVICE_VERSION;
    }

    @Override
    public String toString() {
        return "GeoLocationDetailsTO{" +
                "remoteAddr='" + remoteAddr + '\'' +
                ", resolvedAddresses=" + resolvedAddresses +
                ", country='" + country + '\'' +
                ", lowConfidenceGeoLocation=" + lowConfidenceGeoLocation +
                ", location='" + location + '\'' +
                ", inferredCountry='" + inferredCountry + '\'' +
                '}';
    }
}
