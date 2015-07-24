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
</table>

Each transport instance has a seperate, single identity token resolver (normally a compound one into which many individual resolvers can be registered).

Identity resolution
-------------------

Identity resolution is the transport-agnostic process of converting `IdentityToken`s into relevant instances of `Identity`.
As mentioned previously, identity resolution always occurs on an `ExecutionVenue` thread. If no `Executable`s are found for
an invocation, then the identities will never be resolved. Identity resolution occurs via implementations of `IdentityResolver`
only one of which (normally a compound one into which many individual resolvers can be registered) is set on an `ExecutionVenue`
instance.

Identity token emission
-----------------------

Finally, if you're making a call to a Cougar service using a provided Cougar client implementation, then the `Identity(s)`
passed in the client call are converted into `IdentityToken`s as part of the call serialisation. This makes use of the `tokenise`
call on `IdentityResolver`, the outputs of which are passed into an `IdentityTokenResolver`. This rather confusingly uses
the same interface as on the server, but effectively 'swaps' the input and output types from those you would expect to see
on a server side resolver. To explain more clearly here are the types of the generic parameters:
<table>
  <tr><th>Name</th><th>I</th><th>O</th><th>C</th></tr>
  <tr><td>Sync Rescript</td><td>org.apache.http.client.methods.HttpUriRequest</td><td>org.apache.http.client.methods.HttpUriRequest</td><td>java.security.cert.X509Certificate[]</td></tr>
  <tr><td>Async Rescript</td><td>org.eclipse.jetty.client.api.Response</td><td>org.eclipse.jetty.client.api.Request</td><td>java.security.cert.X509Certificate[]</td></tr>
  <tr><td>Socket</td><td colspan="3">Custom identity token resolvers not supported</td></tr>
</table>

To emit the tokens, a call is made to `rewrite`, but only if `isRewriteSupported()` returns `true`. There is currently no
mechanism for handling token rewrites by the server.

Each client instance has both an `IdentityResolver` and an `IdentityTokenResolver`.


