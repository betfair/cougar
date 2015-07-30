---
layout: default
---
This document describes basic configuration of binary protocol supported by Cougar.

One of the main differences of binary protocol is that it is TCP\- rather than HTTP-based, hence it does
not require any resource (path) mapping.

Binary protocol client supports ability to connect to multiple servers as well as keeping connection alive and
load-balance (so far only round-robin style) between those.

## Server-side config

To enable binary protocol on a Cougar server, you need to add socket-transport dependency to your classpath:

    <dependency>
        <groupId>com.betfair.cougar</groupId>
        <artifactId>socket-transport</artifactId>
        <version>${cougar.version}</version>
    </dependency>

Additionally, you may set the following property values (below are defaults):

    cougar.socket.bindaddress=0.0.0.0
    cougar.socket.serverport=9003
    cougar.socket.reuseAddress=true

Usually one would want to override only port on which Cougar listens for new connections.

### Server SSL

The socket transport supports SSL from Cougar, which is controlled by a number of properties:

* `cougar.socket.ssl.supportsTls` \- defaults to `false`. Whether SSL is supported.
* `cougar.socket.ssl.requiresTls` \- defaults to `false`. Whether SSL is required (connection dropped if not supported by the client).
* `cougar.socket.ssl.needClientAuth` \- defaults to `false`. Whether client certs are required (connection dropped if not supported by the client).
* `cougar.socket.ssl.wantClientAuth` \- defaults to `cougar.socket.ssl.needClientAuth`. Whether client certs are desired.

If `cougar.socket.ssl.supportsTls` is set to `true` then additional properties need to be set to give the keystore for the server certificate:

* `cougar.socket.ssl.keystore` \- The Spring resource path to the keystore.
* `cougar.socket.ssl.keystoreType` \- The type of keystore, defaults to `JKS`.
* `cougar.socket.ssl.keystorePassword` \- The password for the keystore - **must not** be set as a system property.

If `cougar.socket.ssl.wantClientAuth` is set to `true` then additional properties need to be set to give the truststore for client certificates:

* `cougar.socket.ssl.truststore` \- The Spring resource path to the truststore.
* `cougar.socket.ssl.truststoreType` \- The type of truststore, defaults to `JKS`.
* `cougar.socket.ssl.truststorePassword` \- The password for the truststore - **must not** be set as a system property.


#### Connection rejection

There are 2 similar situations where the server might reject connections from cougar clients when SSL is enabled:

* A cougar client is using an older version of the socket protocol, which does not support SSL.
* A cougar client is using a version of the protocol that does support SSL, but which has it (or required sub-features of it) disabled.

The configurations which could lead to rejections in this case:

* If `cougar.socket.ssl.requiresTls` is set to `true`.
* If `cougar.socket.ssl.needClientAuth` is set to `true` and the client has SSL enabled but doesn't present a client certificate.

Similarly the client might choose to [drop a connection](#Connectiontermination.html).


## Client-side config

Cougar client supports binary  protocol by default and because of complex implementation of this protocol you must use Cougar Client library.

You need to add the following module to your client's classpath:

    <dependency>
        <groupId>com.betfair.cougar</groupId>
        <artifactId>cougar-client</artifactId>
        <version>${project.version}</version>
    </dependency>

In your Spring wiring you will need to use the following config:

    <bean id="YOUR_BEAN_NAME" parent="abstractSocketTransport" >
        <constructor-arg value="$ETX{etx.server.endpoint}"/> <!-- coma-separated list of binary transport hosts -->
    </bean>


### Address resolution

#### DNS

By default cougar client resolves the address specified in the transport definition as above, into actual server addresses via DNS lookup.
This is controlled by the following cougar property.

    cougar.client.socket.address.resolver=DNSBasedNetworkAddressResolver

#### FILE

Alternatively, the resolution can also be done based on a config file. To use this mode the following property needs to be specified

    cougar.client.socket.address.resolver=FILEBasedNetworkAddressResolver

In this case the resolved addresses are picked up from a config file which is defined by default as

    cougar.client.socket.address.resolver.config.file=/etc/bf-cougar/cougar.hosts

The format of the config file is a comma separated list of hosts for each endpoint. Eg.

    app1.yourdomain.com=10.10.10.10,10.10.10.11,10.10.10.12

### Client SSL

The socket transport supports SSL from Cougar, which is controlled by a number of properties:

* `cougar.client.socket.ssl.supportsTls` \- defaults to `false`. Whether SSL is supported.
* `cougar.client.socket.ssl.requiresTls` \- defaults to `false`. Whether SSL is required (connection dropped if not supported by the server).
* `cougar.client.socket.ssl.wantClientAuth` \- defaults to `false`. Whether client certs are desired. The client will not set the keystore unless this is `true`.

If `cougar.client.socket.ssl.supportsTls` is set to `true` then additional properties need to be set to give the truststore for the server certificate:

* `cougar.client.socket.ssl.truststore` \- The Spring resource path to the truststore.
* `cougar.client.socket.ssl.truststoreType` \- The type of truststore, defaults to `JKS`.
* `cougar.client.socket.ssl.truststorePassword` \- The password for the truststore - **must not** be set as a system property.

If client certificates are to be supported then additional properties need to be set to give the keystore for the client certificate:

* `cougar.client.socket.ssl.keystore` \- The Spring resource path to the keystore.
* `cougar.client.socket.ssl.keystoreType` \- The type of keystore, defaults to `JKS`.
* `cougar.client.socket.ssl.keystorePassword` \- The password for the keystore - **must not** be set as a system property.

#### Connection termination

There are 2 similar situations where the client might terminate connections to a cougar server when SSL is enabled:

* A cougar server is using an older version of the socket protocol, which does not support SSL.
* A cougar server is using a version of the protocol that does support SSL, but which has it (or required sub-features of it) disabled.

The configuration which could lead to rejections in this case:

* If `cougar.client.socket.ssl.requiresTls` is set to `true`.

Similarly the server might choose to [reject a client](#Connectionrejection.html).

## Control

The binary interface on the cougar server is dependant on the health of the application. On startup the interface is
disabled by default. Once the application is fully initialised and the health check turns OK, the interface will be
enabled and ready to accept client connections.

    2013-01-16 10:01:46.030: com.betfair.cougar.transport.nio.ExecutionVenueNioServer INFO - setting protocol to enabled

Similarly, if the application becomes unhealthy the interface will get automatically disabled. This is to ensure that
the application does not serve requests while in an invalid state.

The interface status can also be manually controlled via JMX. This is to allow administrators to take an application
out of service, to stop it serving requests over binary protocol.

    http://SERVICE_HOST:JMX_PORT:/ViewObjectRes//CoUGAR.socket.transport%3Aname%3Dserver

Any cougar application exposing the binary interface would need to have this additional step executed while taking the
node out of service. See also [Cougar Status Control](Cougar_Status_Control.html).

When the interface shutdown is triggered , all inflight requests will be completed before shutting the interface down
completely. More details on this [here](Cougar_Binary_Interface_Shutdown_Sequence.html)
