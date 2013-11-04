---
layout: default
---
# Invocation URLs

Port is usually 8080 unless over-ridden by ```jetty.http.port```.

|What|Where|Notes|
| -- | --- | --- |
|Service RESCRIPT endpoint|```http://localhost:8080/<interfacePath>/v<x>/<operationPath>[?<queryString>]```|```port``` is usually 8080 unless overridden (```jetty.http.port```)
```interfacePath``` is defined in RESCRIPT extensions document, and may be omitted entirely
```x``` is the major version number of the interface
```operationPath``` is defined in the RESCRIPT extensions document |
| SOAP Servive WSDL | ```http://localhost:8080/wsdl/<nameOfServiceBSIDLFile>.wsdl``` | {color:red}This should be changed to the name of the service and be version-qualified{color} |
| SOAP Service Endpoint | ```http://localhost:8080/<serviceName>Service/vX``` | \- |

# Monitoring URLs

<table>
<tr>
<th>What</th><th>Where</th><th>Notes</th></tr>
<tr>
<td> JMX Html Adapter </td>
<td> [http://localhost:9999/](http://localhost:9999/) </td>
<td> For use by humans </td>
</tr>
<tr>
<td> Batch query </td>
<td> [http://localhost:9999/administration/batchquery.jsp] | For use by programs such as Nagios scripts, matches the format exposed by [Tornjak](http://localhost:9999/administration/batchquery.jsp] | For use by programs such as Nagios scripts, matches the format exposed by [Tornjak)(http://github.com/betfair/tornjak)</td>
</tr>
<tr>
<td> Thread dump </td>
<td> [http://localhost:9999/administration/threaddump.jsp](http://localhost:9999/administration/threaddump.jsp) </td>
<td> - </td>
</tr>
</table>

Read [related documentation](Cougar_Monitoring.html).

# Health Service URLs

Port is usually 8080 unless over-ridden by ```jetty.http.port```.

<table>
<tr>
<th>What</th><th>Where</th><th>Notes</th></tr>
<tr>
<td> Health check summary (RESCRIPT) </td>
<td> [http://localhost:8080/healthcheck/v2/summary](http://localhost:8080/healthcheck/v2/summary) </td>
<td> Use this for webping style checks </td>
</tr>
<tr>
<td> Health check detail (RESCRIPT) </td>
<td> [http://localhost:8080/healthcheck/v2/detailed](http://localhost:8080/healthcheck/v2/detailed) </td>
</tr>
<tr>
<td> Health check WSDL </td>
<td> [http://localhost:8080/wsdl/CougarHealthService.wsdl](http://localhost:8080/wsdl/CougarHealthService.wsdl) </td>
</tr>
<tr>
<td> Health check SOAP Endpoint </td>
<td> [http://localhost:8080/HealthService/v2](http://localhost:8080/HealthService/v2) </td>
</tr>
</table>

Read [related documentation](Cougar_Monitoring.html)