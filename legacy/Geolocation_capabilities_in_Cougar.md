---
layout: default
---

A key core feature of Cougar is the resolution of information passed in the request (such as IP address) into location
information which can be used to drive logic within the implementation of service interfaces.

# Information available to services

## GeoLocationDetails on ExecutionContext

    /**
     * The direct address the request originated from
     */
    String getRemoteAddr();
    /**
     * <p>The originating IP address and the IP addresses of any proxies the request has passed through</p>
     * <p>The originating IP address (the customer) will be the first entry in the list, subsequent proxies will append to the
     list.</p>
     * <p>This information is obtained from the X-Forwarded-For header (if present). If no header exists then there
     * will be one entry that is equal to {@link #getRemoteAddr()}</p>
     */
    List<String> getResolvedAddresses();
    /**
     * The country in which the resolved address has been located
     */
    String getCountry();
    /**
     * @return true if we don't have much confidence in the geoLocated Country.  This can
     * happen due to ISPs such as AOL that span countries
     */
    boolean isLowConfidenceGeoLocation();
    /**
     * The accurate location in which the resolved address has been located, currently always null.
     */
    String getLocation();
    /**
     * Returns the inferred country
     */
    String getInferredCountry();

# Controlling resolution

## Customising the header(s) to obtain the resolved addresses from

By default the header to obtain a single resolved address from (used with load balancers and older versions of Cougar) is
`X-Forwarded-For`, which can be configured by the `cougar.http.ipheader` property. Additionally, for Cougar-Cougar calls,
the full list of all IP addresses this request has passed through (with the initiating address as the first in the list)
is in the header `X-Forwarded-For` and can be configured via the `cougar.http.ipsheader` property. In Cougar clients, these
headers can be set separately using the `cougar.client.http.ipheader` and `cougar.client.http.ipsheader` properties respectively.

## Customising the suspect network list

This is controlled via the property `cougar.geoip.suspectNetworks` and contains a comma delimited list of subnets of
the form `address/mask`. The current default value is `64.12.96.0/255.255.224.0,149.174.160.0/255.255.240.0,152.163.240.0/255.255.248.0,152.163.248.0/255.255.252.0,152.163.252.0/255.255.254.0,152.163.96.0/255.255.252.0,152.163.100.0/255.255.254.0,195.93.96.0/255.255.224.0,198.81.16.0/255.255.240.0,205.188.192.0/255.255.240.0,205.188.208.0/255.255.254.0,205.188.112.0/255.255.240.0,205.188.146.144/255.255.255.252,207.200.112.0/255.255.248.0,172.128.0.0/255.192.0.0,172.192.0.0/255.240.0.0`.

## Country inference

By default country inference is not performed. To enable define a **single** spring bean which implements
`com.betfair.cougar.api.security.InferredCountryResolver<HttpServletRequest>`. If more than one implementation of
`InferredCountryResolver` is found within the Spring context then Cougar will fail to start.
