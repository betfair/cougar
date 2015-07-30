---
layout: default
---
A `com.betfair.cougar.core.api.fault.Fault` happens under the following conditions:

* The request is badly formed in some way (invalid encoding, mandatory data missing, etc)
* The Cougar framework had an unexpected problem (a Java runtime exception).
* The application code returned a defined exception (One of the exceptions listed in the `<exceptions>` tag of the operation).
* The application code had an unexpected problem (a Java runtime exception).

It contains:

* A `FaultCode` indicating whether the problem is at the client or on the server
* An `errorCode` in the form XXX-YYYY, where XXX indicates in which functional area the error occurred, and YYYY is a number signifying the cause
* A `FaultDetail` containing a message and possibly a trace, if detailed fault reporting is enabled

# Enabling Detailed Fault Reporting

You can do this statically by setting the Cougar core property `cougar.fault.detailed=true`, or dynamically using the MBean `CoUGAR:name=faultController`.

# Cougar errorCodes

`DSC` is the `errorCode` prefix for faults returned by Cougar itself.


<table style='background-color: #FFFFCE;'>
 <tr>
   <td valign='top'><img src='warning.gif' width='16' height='16' align='absmiddle' alt='' border='0'></td>
   <td><p>If you can't find what you need in this table, please look at the source of `ServerFaultCode` in your IDE and then update this page.</p></td>
  </tr>
</table>


<table>
<tr>
<th>Meaning</th><th>FaultCode</th><th>Client/Server</th><th>Associated HTTP Transport Response Code</th><th>Comments</th></tr>
<tr>
<td> DSC-0001 </td>
<td> StartupError </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0002 </td>
<td> FrameworkError </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0003 </td>
<td> InvocationResultIncorrect </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0005 </td>
<td> ServiceRuntimeException </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0006 </td>
<td> SOAPDeserialisationFailure </td>
<td> Client </td>
<td> 400 </td>
</tr>
<tr>
<td> DSC-0007 </td>
<td> XMLDeserialisationFailure </td>
<td> Client </td>
<td> 400 </td>
</tr>
<tr>
<td> DSC-0008 </td>
<td> JSONDeserialisationParseFailure </td>
<td> Client </td>
<td> 400 </td>
</tr>
<tr>
<td> DSC-0009 </td>
<td> ClassConversionFailure </td>
<td> Client </td>
<td> 400 </td>
<td> Invalid format for parameter, for example passing a string where a number was expected.  Can also happen when a value is passed that does not match any valid enum. </td>
</tr>
<tr>
<td> DSC-0010 </td>
<td> InvalidInputMediaType </td>
<td> Client </td>
<td> 415 </td>
</tr>
<tr>
<td> DSC-0011 </td>
<td> ContentTypeNotValid </td>
<td> Client </td>
<td> 415 </td>
</tr>
<tr>
<td> DSC-0012 </td>
<td> MediaTypeParseFailure </td>
<td> Client </td>
<td> 415 </td>
</tr>
<tr>
<td> DSC-0013 </td>
<td> AcceptTypeNotValid </td>
<td> Client </td>
<td> 406 </td>
</tr>
<tr>
<td> DSC-0014 </td>
<td> ResponseContentTypeNotValid </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0015 </td>
<td> SecurityException </td>
<td> Client </td>
<td> 403 </td>
</tr>
<tr>
<td> DSC-0016 </td>
<td> ServiceDisabled </td>
<td> Server </td>
<td> 503 </td>
</tr>
<tr>
<td> DSC-0017 </td>
<td> OperationDisabled </td>
<td> Server </td>
<td> 503 </td>
</tr>
<tr>
<td> DSC-0018 </td>
<td> MandatoryNotDefined </td>
<td> Client </td>
<td> 400 </td>
<td> A parameter marked as mandatory was not provided </td>
</tr>
<tr>
<td> DSC-0019 </td>
<td> Timeout </td>
<td> Server </td>
<td> 504 </td>
</tr>
<tr>
<td> DSC-0020 </td>
<td> BinDeserialisationParseFailure </td>
<td> Client </td>
<td> 400 </td>
</tr>
<tr>
<td> DSC-0021 </td>
<td> NoSuchOperation </td>
<td> Client </td>
<td> 404 </td>
</tr>
<tr>
<td> DSC-0022 </td>
<td> SubscriptionAlreadyActiveForEvent </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0023 </td>
<td> NoSuchService </td>
<td> Client </td>
<td> 404 </td>
</tr>
<tr>
<td> DSC-0024 </td>
<td> RescriptDeserialisationFailure </td>
<td> Client </td>
<td> 400 </td>
</tr>
<tr>
<td> DSC-0025 </td>
<td> JMSTransportCommunicationFailure </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0026 </td>
<td> RemoteCougarCommunicationFailure </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0027 </td>
<td> OutputChannelClosedCantWrite </td>
<td> Server </td>
<td> ? </td>
</tr>
<tr>
<td> DSC-0028 </td>
<td> XMLSerialisationFailure </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0029 </td>
<td> JSONSerialisationFailure </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0030 </td>
<td> SOAPSerialisationFailure </td>
<td> Server </td>
<td> 500 </td>
</tr>
<tr>
<td> DSC-0031\ </td>
<td> NoRequestsFound </td>
<td> Client\ </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0032\ </td>
<td> EPNSerialisationFailure </td>
<td> Server\ </td>
<td> 500\ </td>
</tr>
<tr>
<td> DSC-0033\ </td>
<td> UnidentifiedCaller </td>
<td> Client </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0034\ </td>
<td> UnknownCaller </td>
<td> Client </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0035\ </td>
<td> UnrecognisedCredentials </td>
<td> Client </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0036\ </td>
<td> InvalidCredentials </td>
<td> Client </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0037\ </td>
<td> SubscriptionRequired </td>
<td> Client </td>
<td> 403\ </td>
</tr>
<tr>
<td> DSC-0038\ </td>
<td> OperationForbidden </td>
<td> Client </td>
<td> 403\ </td>
</tr>
<tr>
<td> DSC-0039\ </td>
<td> NoLocationSupplied </td>
<td> Client\ </td>
<td> 400\ </td>
</tr>
<tr>
<td> DSC-0040\ </td>
<td> BannedLocation </td>
<td> Client\ </td>
<td> 403\ </td>
</tr>
</table>

# Service errorCodes

These are defined in the service BSIDLs, for example here we see that `WEX-0001` indicates "The wotsit is closed".

    <interface name="Baseline" ...
        ...
        <exceptionType name="WotsitException" prefix="WEX">
            <description>This exception might be thrown when an operation fails</description>
            <parameter name="errorCode" type="string">
                <description>the unique code for this error</description>
            <validValues>
                <value id="1" name="CLOSED">
                    <description>The wotsit is closed</description>
                </value>
                    ...

# Example faults

## HTTP Transport, SOAP Protocol

A SOAP fault for a malformed request with detailed fault reporting switched on:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Header/>
       <soap:Body>
          <soap:Fault>
             <faultcode>soap:Client</faultcode>
             <faultstring>DSC-0006</faultstring>
             <detail>
                <trace/>
                <message>com.ctc.wstx.exc.WstxParsingException: Unexpected close tag &lt;/bas:TestExceptionRquest>; expected &lt;/bas:TestExceptionRequest>.
     at [row,col {unknown-source}]: [5,31]</message>
             </detail>
          </soap:Fault>
       </soap:Body>
    </soap:Envelope>

A SOAP fault for a Null pointer exception being thrown in the application code with detailed fault reporting switched off:

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sec="http://www.betfair.com/security/">
       <soap:Header/>
       <soap:Body>
          <soap:Fault>
             <faultcode>soap:Server</faultcode>
             <faultstring>DSC-0005</faultstring>
             <detail/>
          </soap:Fault>
       </soap:Body>
    </soap:Envelope>

A SOAP fault for a defined exception (WotsitException) with 2 parameters (errorCode & type) being thrown and detailed fault reporting switched on

    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sec="http://www.betfair.com/security/">
       <soap:Header/>
       <soap:Body>
          <soap:Fault>
             <faultcode>soap:Client</faultcode>
             <faultstring>WEX-0001</faultstring>
             <detail>
                <app:WotsitException xmlns:app="http://www.betfair.com/servicetypes/v1/Baseline/">
                   <app:errorCode>CLOSED</app:errorCode>
                   <app:type>SPICY</app:type>
                </app:WotsitException>
                <trace>com.betfair.baseline.v1_0.exception.WotsitException: WEX-0001
        at com.betfair.cougar.baseline.BaselineServiceImpl.testException(BaselineServiceImpl.java:189)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at com.betfair.cougar.core.impl.handler.OperationInvoker.handleInternal(OperationInvoker.java:45)
        at com.betfair.cougar.core.api.handler.InternalHandler.handle(InternalHandler.java:18)
        at com.betfair.cougar.core.impl.ChainImpl.execute(ChainImpl.java:53)
        at com.betfair.cougar.core.impl.server.CougarServer.service(CougarServer.java:128)
        at com.betfair.cougar.transport.http.jetty.JettyServiceHandler.handle(JettyServiceHandler.java:45)
        at org.mortbay.jetty.handler.HandlerList.handle(HandlerList.java:49)
        at org.mortbay.jetty.handler.HandlerCollection.handle(HandlerCollection.java:113)
        at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)
        at org.mortbay.jetty.Server.handle(Server.java:324)
        at org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:550)
        at org.mortbay.jetty.HttpConnection$RequestHandler.content(HttpConnection.java:890)
        at org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:743)
        at org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:215)
        at org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:407)
        at org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:421)
        at org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:520)</trace>
                <message>WotsitException</message>
             </detail>
          </soap:Fault>
       </soap:Body>
    </soap:Envelope>

## HTTP Transport, RESCRIPT Protocol, XML Content Type

A fault for an internal problem in the cougar code with detailed fault reporting switched on:

    <fault>
       <faultcode>Server</faultcode>
       <faultstring>DSC-0002</faultstring>
       <detail>
          <trace/>
          <message>Unhandled event found in serialiser</message>
       </detail>
    </fault>

## HTTP Transport, RESCRIPT Protocol, JSON Content Type

An IDL defined exception (SimpleException with fields errorCode and reason) thrown by the application code:

    {
       "detail":    {
          "message": "SimpleException",
          "trace": "com.betfair.baseline.v1_0.exception.SimpleException: SEX-0001\r\n\tat com.betfair.cougar.baseline.BaselineServiceImpl.testException(BaselineServiceImpl.java:179)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\r\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\r\n\tat java.lang.reflect.Method.invoke(Method.java:597)\r\n\tat com.betfair.cougar.core.impl.handler.OperationInvoker.handleInternal(OperationInvoker.java:45)\r\n\tat com.betfair.cougar.core.api.handler.InternalHandler.handle(InternalHandler.java:18)\r\n\tat com.betfair.cougar.core.impl.ChainImpl.execute(ChainImpl.java:53)\r\n\tat com.betfair.cougar.core.impl.server.CougarServer.service(CougarServer.java:129)\r\n\tat com.betfair.cougar.transport.http.jetty.JettyServiceHandler.handle(JettyServiceHandler.java:45)\r\n\tat org.mortbay.jetty.handler.HandlerList.handle(HandlerList.java:49)\r\n\tat org.mortbay.jetty.handler.HandlerCollection.handle(HandlerCollection.java:113)\r\n\tat org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)\r\n\tat org.mortbay.jetty.Server.handle(Server.java:324)\r\n\tat org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:550)\r\n\tat org.mortbay.jetty.HttpConnection$RequestHandler.headerComplete(HttpConnection.java:876)\r\n\tat org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:535)\r\n\tat org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:209)\r\n\tat org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:407)\r\n\tat org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:421)\r\n\tat org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:520)\r\n",
          "SimpleException":       {
             "reason": "GENERIC",
             "errorCode": "GENERIC"
          }
       },
       "faultcode": "Client",
       "faultstring": "SEX-0001"
    }