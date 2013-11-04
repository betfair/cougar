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
```X-Forwarded-For```, which can be configured by the ```cougar.http.ipheader``` property. Additionally, for Cougar-Cougar calls,
the full list of all IP addresses this request has passed through (with the initiating address as the first in the list)
is in the header ```X-Forwarded-For``` and can be configured via the ```cougar.http.ipsheader``` property. In Cougar clients, these
headers can be set separately using the ```cougar.client.http.ipheader``` and ```cougar.client.http.ipsheader``` properties respectively.

## Specifying the Maxmind data file location

By default the Maxmind data file is loaded from a file called ```GeoIP.dat``` in ```/etc/geoip/```. You can change to
location of the file (but not the name of the file) by setting ```cougar.geoip.location```.

The geoip location code automatically polls the disk for changes to this file, and reloads the data in a thread safe
manner if it has changed. This means that you don't need to take a node out of service or restart it whilst the geolocation
data is changed. The frequency of this polling can be changed via the ```cougar.geoip.checkInterval``` property, it
defaults to ```30000```(ms).

In **non-production** environments you can also use the (very old) geoip data which is distributed with Cougar by setting
```cougar.geoip.useDefault``` to ```true```. **Note: that this must not be done in production**.

## Extending the list of countries in the Maxmind data file

[Occasionally](http://dev.maxmind.com/geoip/release-notes) Maxmind, our chosen provider for geolocation services, adds
support for new countries to their geoip data file, and at the same time makes changes to it's Java client library.
Unfortunately, this means that every time a country is added, all using services would have to change to support the
resolution of this new country.

To ease this process, we have extended their client library to be runtime configurable as to the list of extra country
codes and names supported by the data files.


<table style='background-color: #FFFFCE;'>
         <tr>
           <td valign='top'><img src='warning.gif' width='16' height='16' align='absmiddle' alt='' border='0'></td>
           <td><p>You must ensure that you add country codes and names in the exact form and order as those specified in
           Maxmind' updated client library. Additionally, as new Cougar versions are released, we will move new codes/names
           into the Cougar release and so you **must** check your override list every time you upgrade Cougar. If you are
           in any doubt, please ask on the mailing list.</p></td>
          </tr>
</table>
.

The list of codes/names can be controlled via the ```cougar.geoip.extraCountryCodes``` and ```cougar.geoip.extraCountryNames```
properties, and will always default to an empty string. The strings are comma delimited.

## Customising the suspect network list

This is controlled via the property ```cougar.geoip.suspectNetworks``` and contains a comma delimited list of subnets of
the form ```address/mask```. The current default value is ```64.12.96.0/255.255.224.0,149.174.160.0/255.255.240.0,152.163.240.0/255.255.248.0,152.163.248.0/255.255.252.0,152.163.252.0/255.255.254.0,152.163.96.0/255.255.252.0,152.163.100.0/255.255.254.0,195.93.96.0/255.255.224.0,198.81.16.0/255.255.240.0,205.188.192.0/255.255.240.0,205.188.208.0/255.255.254.0,205.188.112.0/255.255.240.0,205.188.146.144/255.255.255.252,207.200.112.0/255.255.248.0,172.128.0.0/255.192.0.0,172.192.0.0/255.240.0.0```.

## Country inference

By default country inference is not performed. To enable define a **single** spring bean which implements
```com.betfair.cougar.api.security.InferredCountryResolver<HttpServletRequest>```. If more than one implementation of
```InferredCountryResolver``` is found within the Spring context then Cougar will fail to start.
