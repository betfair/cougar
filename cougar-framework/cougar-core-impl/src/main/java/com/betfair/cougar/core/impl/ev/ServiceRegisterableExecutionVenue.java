/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.core.impl.ev;

import java.util.*;
import java.util.logging.Level;

import com.betfair.cougar.api.*;
import com.betfair.cougar.core.api.ServiceDefinition;
import com.betfair.cougar.core.api.ServiceRegistrar;
import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.ev.*;
import com.betfair.cougar.core.api.security.IdentityResolverFactory;
import com.betfair.cougar.logging.CougarLogger;
import com.betfair.cougar.logging.CougarLoggingUtils;
import com.betfair.cougar.util.configuration.PropertyConfigurer;
import com.betfair.tornjak.kpi.KPIMonitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Implementation of ExecutionVenue which facilitates service registration.
 *
 */
public class ServiceRegisterableExecutionVenue extends BaseExecutionVenue implements ApplicationListener, ServiceRegistrar {
	
	private final static CougarLogger logger = CougarLoggingUtils.getLogger(ServiceRegisterableExecutionVenue.class);

	private Map<String, Map<ServiceDefinition, Service>> serviceImplementationMap = new HashMap<String, Map<ServiceDefinition, Service>>();
    private Map<ServiceKey, String> serviceStatNames = new HashMap<>();
    private Map<ServiceKey, ServiceLogManager> serviceLogManagers = new HashMap<>();
    private KPIMonitor stats;
    private ServiceLogManagerFactory serviceLogManagerFactory;
    private IdentityResolverFactory identityResolverFactory;
    protected MonitorRegistry monitorRegistry;

    public void setIdentityResolverFactory(IdentityResolverFactory identityResolverFactory) {
        this.identityResolverFactory = identityResolverFactory;
    }

    public void setServiceLogManagerFactory(ServiceLogManagerFactory serviceLogManagerFactory) {
        this.serviceLogManagerFactory = serviceLogManagerFactory;
    }

    public void setStats(KPIMonitor stats) {
        this.stats = stats;
    }

    public void setMonitorRegistry(MonitorRegistry monitorRegistry) {
        this.monitorRegistry = monitorRegistry;
    }

    private void registerServiceDefinition(String namespace, ServiceDefinition serviceDefinition, ExecutableResolver resolver) {
        String serviceStatName = getServiceStatName(namespace, serviceDefinition);
        for (OperationDefinition op : serviceDefinition.getOperationDefinitions()) {
            OperationKey namespacedOperationKey = namespace == null ? op.getOperationKey() : new OperationKey(op.getOperationKey(), namespace);
            registerOperation(
                namespace,
                op,
                resolver.resolveExecutable(namespacedOperationKey, this),
                stats != null ? new ServiceOperationExecutionTimingRecorder(stats, serviceStatName, op.getOperationKey().getOperationName()) : new NullExecutionTimingRecorder());
        }
    }

    private Map<ServiceDefinition, Service> getImplementationMapForNamespace(String namespace) {
        Map<ServiceDefinition, Service> namespacedMap = serviceImplementationMap.get(namespace);
        if (namespacedMap == null) {
            namespacedMap = new HashMap<ServiceDefinition, Service>();
            serviceImplementationMap.put(namespace, namespacedMap);
        }
        return namespacedMap;
    }
	@Override
	public void registerService(ServiceDefinition serviceDefinition, Service implementation, ExecutableResolver resolver) {
        registerService(null, serviceDefinition, implementation, resolver);
	}

    @Override
    public void registerService(String namespace, ServiceDefinition serviceDefinition, Service implementation, ExecutableResolver resolver) {
        getImplementationMapForNamespace(namespace).put(serviceDefinition, implementation);
        registerServiceDefinition(namespace, serviceDefinition, resolver);

        logger.log(Level.INFO, "Initialising %s Service version %s",
                serviceDefinition.getServiceName(),
                serviceDefinition.getServiceVersion().toString());

        implementation.init(getContainerContext(getServiceLogManager(namespace, serviceDefinition)));

        logger.log(Level.INFO, "Initialisation complete");
    }

    protected Map<String, Map<ServiceDefinition, Service>> getServiceImplementationMap() {
        return serviceImplementationMap;
    }

    /**
     * This method returns an unmodifiable map between each namespace and the set of serviceDefinitions bound
     * to that namespace.  Note that by default the namespace is null, so there could be a null
     * namespace with services enumerated within
     * @return
     */
    public Map<String, Set<ServiceDefinition>> getNamespaceServiceDefinitionMap() {
        Map<String, Set<ServiceDefinition>> namespaceServiceDefinitionMap = new HashMap<String, Set<ServiceDefinition>>();

        for (String namespace : serviceImplementationMap.keySet()) {
            Set<ServiceDefinition> serviceDefinitions = new HashSet<ServiceDefinition>();
            namespaceServiceDefinitionMap.put(namespace, serviceDefinitions);
            for (ServiceDefinition sd : serviceImplementationMap.get(namespace).keySet()) {
                serviceDefinitions.add(sd);
            }
        }
        return Collections.unmodifiableMap(namespaceServiceDefinitionMap);
    }


	/**
	 * In the case of ContextRefreshedEvent (that is, once all Spring configuration is loaded), the services will be
     * initialised
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
		    // Firstly dump out all the properties read from the various files
		    dumpProperties();
            // now init the identity resolver
            setIdentityResolver(identityResolverFactory.getIdentityResolver());
		}
	}

	private void dumpProperties() {
		final Map<String,String> props = PropertyConfigurer.getAllLoadedProperties();
		logger.log(Level.INFO, "Properties loaded from config files and system property overrides");
		int longest = 0;
		for (Map.Entry<String,String> me : props.entrySet()) {
			longest = Math.max(longest, me.getKey().length());
		}
        for (Map.Entry<String, String> me: props.entrySet()) {
            String sysOverride = System.getProperty(me.getKey());
            String value = (sysOverride == null ? me.getValue() : sysOverride);
            if (me.getKey().toLowerCase().contains("password")) {
            	value = "*****";
            }
            logger.log(Level.INFO, "  %-"+longest+"s = %s%s", 
                            me.getKey(),
                            value,
                            (sysOverride == null ? "" : " [OVERRIDDEN]"));
        }
	}
	
    private ContainerContext getContainerContext(final ServiceLogManager logManager) {
    	return new ContainerContext() {

			@Override
			public ServiceInfo[] getRegisteredServices() {
                List<ServiceInfo> services = new ArrayList<ServiceInfo>();
                for (Map.Entry<String, Map<ServiceDefinition, Service>> e: getServiceImplementationMap().entrySet()) {
                    String namespace = e.getKey();
                    for (Map.Entry<ServiceDefinition, Service> namespacedEntry: e.getValue().entrySet()) {
                        services.add(makeServiceInfo(namespace, namespacedEntry.getKey(), namespacedEntry.getValue()));
                    }
                }
				Collections.sort(services, new Comparator<ServiceInfo>() {
					public int compare(ServiceInfo o1, ServiceInfo o2) {
						int result = o1.getServiceName().compareTo(o2.getServiceName());
						if (result == 0) {
							result = o1.getVersion().compareTo(o2.getVersion());
						}
						return result;
					}
				});
				return services.toArray(new ServiceInfo[services.size()]);
			}

			@Override
			public void registerExtensionLoggerClass(
					Class<? extends LogExtension> clazz, int numFieldsLogged) {
				logManager.registerExtensionLoggerClass(clazz, numFieldsLogged);
			}

            @Override
            public void registerConnectedObjectExtensionLoggerClass(Class<? extends LogExtension> clazz, int numFieldsLogged) {
                logManager.registerConnectedObjectExtensionLoggerClass(clazz, numFieldsLogged);
            }

            @Override
            public MonitorRegistry getMonitorRegistry() {
                return monitorRegistry;
            }
        };
    }
	
	private ServiceInfo makeServiceInfo(String namespace, ServiceDefinition serviceDefinition, Service implementation) {
		List<String> operations = new ArrayList<>();
		for (OperationDefinition operationDefinition : serviceDefinition.getOperationDefinitions()) {
            operations.add(operationDefinition.getOperationKey().getOperationName());
		}
		return new ServiceInfo(namespace,
                implementation,
				serviceDefinition.getServiceName(), 
				serviceDefinition.getServiceVersion().toString(),
                operations);
	}



    private ServiceLogManager getServiceLogManager(String namespace, ServiceDefinition serviceDefinition) {
        ServiceKey key = new ServiceKey(namespace, serviceDefinition.getServiceName(), serviceDefinition.getServiceVersion());
        ServiceLogManager ret = serviceLogManagers.get(key);
        if (ret == null) {
            ret = serviceLogManagerFactory.create(namespace, serviceDefinition.getServiceName(), serviceDefinition.getServiceVersion());
            serviceLogManagers.put(key, ret);
        }
        return ret;
    }

    public ServiceLogManager getServiceLogManager(String namespace, String serviceName, ServiceVersion version) {
        ServiceKey key = new ServiceKey(namespace, serviceName, version);
        ServiceLogManager ret = serviceLogManagers.get(key);
        if (ret == null) {
            ret = serviceLogManagerFactory.create(namespace, serviceName, version);
            serviceLogManagers.put(key, ret);
        }
        return ret;
    }

    private String getServiceStatName(String namespace, ServiceDefinition serviceDefinition) {
        return getServiceStatName(namespace, serviceDefinition.getServiceName(), serviceDefinition.getServiceVersion());
    }

    private String getServiceStatName(String namespace, String serviceName, ServiceVersion version) {
        ServiceKey key = new ServiceKey(namespace, serviceName, version);
        String ret = serviceStatNames.get(key);
        if (ret == null) {
            ret = serviceName + "." + version + ((namespace == null) ? "" : "." + namespace);
            serviceStatNames.put(key, ret);
        }
        return ret;
    }

    private static class ServiceKey {
        private String namespace;
        private String serviceName;
        private ServiceVersion version;

        private ServiceKey(String namespace, String serviceName, ServiceVersion version) {
            this.namespace = namespace;
            this.serviceName = serviceName;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceKey serviceKey = (ServiceKey) o;

            if (namespace != null ? !namespace.equals(serviceKey.namespace) : serviceKey.namespace != null) return false;
            if (serviceName != null ? !serviceName.equals(serviceKey.serviceName) : serviceKey.serviceName != null) return false;
            if (version != null ? !version.equals(serviceKey.version) : serviceKey.version != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = namespace != null ? namespace.hashCode() : 0;
            result = 43 * result + (serviceName != null ? serviceName.hashCode() : 0);
            result = 43 * result + (version != null ? version.hashCode() : 0);
            return result;
        }
    }

}
