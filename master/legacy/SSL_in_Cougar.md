---
layout: default
---
# SSL in Cougar

This document describes how to enable SSL in Cougar (for client and server communications).  If you're only securing
your Cougar Service, then ignore the client sections below.  It assumes an understanding of SSL / Certificates - detailed 
explanation of these topics is beyond the scope of this document.

## Configuring 1-way SSL (Server Cert only) 

To secure your Cougar client / server with 1-way SSL, you'll need to do the following:

### Server:

#### Ensure your server cert is stored in your server keystore.  
	
#### Configure the HTTP Transport:

Override the following properties:

```
jetty.https.port=8443
jetty.https.needClientAuth=false
jetty.https.keystore=<your_keystore_path_here>/keystore.jks
jetty.https.keystoreType=JKS
jetty.https.keyPassword=xxx
```

### Client:

#### Import the Server's certificate (from above), into your trust store of certs (not your keystore)

#### Configure your Rescript Transport in Spring:

```
<property name="transportSSLEnabled" value="true"/>
<property name="httpsKeystore" value="<put the path to your keystore here>"/>
<property name="httpsKeyPassword" value="xxx"/>
<property name="httpsTruststore" value="<put the path to your truststore here>"/>
<property name="httpsTrustPassword" value="yyy"/>
```

## 2 Way SSL (aka ClientAuth)

The configuration is as above except the server must now have a truststore configured, and the needClientAuth property 
must be set to true

2 Way SSL configuration is achieved as follows:

### Server:

#### Ensure the CA Cert used to sign the Client's certificate is in your set of trusted certs:

```keytool -list -keystore truststore.jks```

#### Configure the HTTP Transport:

Override the following properties:

```
jetty.https.port=8443
jetty.https.needClientAuth=true
jetty.https.keystore=<your_keystore_path_here>
jetty.https.keystoreType=JKS
jetty.https.keyPassword=xxx
jetty.https.truststore=<your_trust_store_path_here>
jetty.https.truststoreType=JKS
jetty.https.trustPassword=yyy
```
		

### Client:

#### Ensure the CA Cert used to sign the Server's certificate is in your set of trusted certs:

```keytool -list -keystore truststore.jks```

#### Configure your Rescript Transport:

```
<property name="transportSSLEnabled" value="true"/>
<property name="httpsKeystore" value="<put the path to your keystore here>"/>
<property name="httpsKeyPassword" value="xxx"/>
<property name="httpsTruststore" value="<put the path to your truststore here>"/>
<property name="httpsTrustPassword" value="yyy"/>
```

### Some very useful tips

The following command is helpful to examine what the contents of a keystore:

```keytool -list -keystore keystore.jks```

From this output, you'll see something like this:

```		
Keystore type: JKS
Keystore provider: SUN
.
Your keystore contains 1 entry
.
server, 11-May-2011, PrivateKeyEntry, 
Certificate fingerprint (MD5): 96:F8:6D:16:59:6F:67:BB:C2:1C:14:14:CD:D1:E6:8F
```

To export a certificate from your keystore (to be added to the client's trusted cert set below):

```keytool -exportcert -alias server -file server_cert.der -keystore keystore.jks```

To import a certificate into a keystore:

```keytool -import -alias server -file server_cert.der -keystore truststore.jks```

There are many different serialized formats of certificates, openssl is extremely useful for converting between them.

This page gives a step by step series of instructions to create a dummy rootCA, create a new keystore, create and sign 
your new certificate, convert between various formats etc:

http://blog.tumy-tech.com/2011/04/06/creating-self-signed-certs-for-a-development-environment-oracle-idm-pki/

By adding the following flag to you're applications launch args, SSL debugging will be enabled - this is useful for narrowing 
where the problem is:

```-Djavax.net.debug=all```


