package com.betfair.cougar.transport.api;

import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;

import java.util.Set;

public interface DehydratedExecutionContextResolver<Transport, CredentialsContainer> {
    DehydratedExecutionContextComponent[] supportedComponents();
    void resolving(Set<DehydratedExecutionContextComponent> handling);
    void resolve(Transport transport, CredentialsContainer credentialsContainer, DehydratedExecutionContextBuilder builder);

}
