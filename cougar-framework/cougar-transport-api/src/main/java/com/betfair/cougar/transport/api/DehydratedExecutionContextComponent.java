package com.betfair.cougar.transport.api;

/**
 * Parts of DehydratedExecutionContext that resolvers can provide.
 */
public enum DehydratedExecutionContextComponent {
    Location,
    IdentityTokens,
    RequestUuid,
    ReceivedTime,
    RequestedTime,
    TraceLoggingEnabled,
    TransportSecurityStrengthFactor
}
