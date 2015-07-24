---
layout: default
---
Identities
==========

Knowing who the users of your service are is a common need for service owners. When building Cougar we looked at existing
libraries and frameworks for modelling and resolving entities (users or other applications) and found them too restrictive,
so in time honoured tradition, we've implemented our own, although they are heavily based on JAAS, which we've used where we can.

Cougar's identity model
-----------------------

The base of Cougar's identity model is the concept of a Principal, which represents any entity, such as an individual,
a corporation, and a login id. An Identity consists of both the Principal (the logical identity) and the Credentials
that were used to resolve that Principal.

Service implementations interact with instances of Identity, however these in turn are resolved from IdentityTokens
(string key/value pairs) which are resolved on a per transport basis. This means that the logic to resolve the tokens can
vary by transport (as is needed, since the binary transport presents data rather differently from an HTTP transport).

Identity token resolution is performed in the transport thread, Identity resolution occurs on an EV thread. In the case of
transports supporting batch calls (e.g. JSON-RPC), Identity resolution is done once for the whole batch and the resolved Identity
list is sent to all executables.

Identity tokens can also be 'rewritten' back to the client, to support tokens which change (e.g. single use tokens or rotating
keys on an encrypted sso token).


Identity token resolution
-------------------------

Identity token resolution is performed through transport-specific implementations of `IdentityTokenResolver`:

    public interface IdentityTokenResolver<I, O, C> {
        public List<IdentityToken> resolve(I input, C transportAuthTokens);
        public void rewrite(List<IdentityToken> credentials, O output);
        public boolean isRewriteSupported();
    }

The types of the generic parameters vary by transport and for custom transports you should consult specific documentation to ascertain these. Standard Cougar transports are as follows:
<table>
  <tr><th>Name</th><th>I</th><th>O</th><th>C</th></tr>
  <tr><td>Rescript</td><td>javax.servlet.http.HttpServletRequest</td><td>javax.servlet.http.HttpServletResponse</td><td>java.security.cert.X509Certificate[]</td></tr>
  <tr><td>SOAP</td><td>org.apache.axiom.om.OMElement</td><td>org.apache.axiom.om.OMElement</td><td>java.security.cert.X509Certificate[]</td></tr>
  <tr><td>Socket</td><td colspan="3">Custom identity token resolvers not supported</td></tr>
  <tr><td>ActiveMQ</td><td></td><td></td><td></td></tr>
</table>



Identity resolution
-------------------
