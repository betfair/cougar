package com.betfair.cougar.transport.impl;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.export.Protocol;
import com.betfair.cougar.api.export.ProtocolParadigm;
import com.betfair.cougar.api.export.ProtocolRegistry;
import com.betfair.cougar.core.api.GateListener;
import com.betfair.cougar.core.api.builder.DehydratedExecutionContextBuilder;
import com.betfair.cougar.transport.api.DehydratedExecutionContextComponent;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolution;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolverFactory;
import com.betfair.cougar.transport.api.DehydratedExecutionContextResolver;

import java.util.*;

/**
 */
public class DehydratedExecutionContextResolutionImpl implements GateListener, DehydratedExecutionContextResolution {

    private List<DehydratedExecutionContextResolverFactory> factories = new ArrayList<>();
    private Map<Protocol, Set<DehydratedExecutionContextResolver>> protocolResolvers = new HashMap<>();

    @Override
    public void registerFactory(DehydratedExecutionContextResolverFactory factory) {
        factories.add(factory);
    }

    @Override
    public int getPriority() {
        // we want to run after all the transports have been registered
        // they will want to be initialised early anyway so they're registered before
        // the services/executables get bound
        return Integer.MAX_VALUE;
    }

    @Override
    public void onCougarStart() {
        init(true);
    }

    public void init(boolean failIfNotComplete) {
        for (ProtocolParadigm paradigm : ProtocolParadigm.values()) {
            Set<Protocol> protocols = ProtocolRegistry.protocols(paradigm);

            for (Protocol p : protocols) {
                Set<DehydratedExecutionContextComponent> remainingComponents = new HashSet<>(Arrays.asList(DehydratedExecutionContextComponent.values()));
                Set<DehydratedExecutionContextResolver> componentResolvers = new HashSet<>();
                for (DehydratedExecutionContextResolverFactory f : factories) {
                    DehydratedExecutionContextResolver<?,?>[] resolvers = f.resolvers(p);
                    if (resolvers != null) {
                        for (DehydratedExecutionContextResolver<?,?> resolver : resolvers) {
                            DehydratedExecutionContextComponent[] components = resolver.supportedComponents();
                            Set<DehydratedExecutionContextComponent> handling = new HashSet<>();
                            for (DehydratedExecutionContextComponent component : components) {
                                if (remainingComponents.contains(component)) {
                                    componentResolvers.add(resolver);
                                    remainingComponents.remove(component);
                                    handling.add(component);
                                }
                            }
                            // notify the resolver what they will be resolving
                            resolver.resolving(Collections.unmodifiableSet(handling));
                        }
                    }
                }
                //todo: #82: this needs to go back once all protocols have been built.. && failIfNotComplete
//                if (!remainingComponents.isEmpty()) {
//                    throw new IllegalStateException("I have unhandled components: "+remainingComponents);
//                }
                protocolResolvers.put(p, componentResolvers);
            }
        }
    }

    @Override
    public String getName() {
        return "DehydratedExecutionContextResolution";
    }

    @Override
    public <T,C> DehydratedExecutionContext resolveExecutionContext(Protocol protocol, T transport, C credentialsContainer) {
        DehydratedExecutionContextBuilder builder = new DehydratedExecutionContextBuilder();

        Set<DehydratedExecutionContextResolver> resolvers = protocolResolvers.get(protocol);
        if (resolvers != null) {
            //noinspection unchecked
            for (DehydratedExecutionContextResolver<T,C> r : resolvers) {
                // todo: #82: the builder needs to verify that items are only set once
                r.resolve(transport, credentialsContainer, builder);
            }
        }

        // the builder checks that all needed components have been set
        return builder.build();
    }
}
