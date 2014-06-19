/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.health;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.LogExtension;
import com.betfair.cougar.api.LoggableEvent;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.ServiceInfo;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.cougar.health.service.v3.HealthService;
import com.betfair.cougar.health.service.v3.enumerations.HealthStatus;
import com.betfair.cougar.health.service.v3.enumerations.RestrictedHealthStatus;
import com.betfair.cougar.health.service.v3.exception.HealthException;
import com.betfair.cougar.health.service.v3.to.HealthDetailResponse;
import com.betfair.cougar.health.service.v3.to.HealthSummaryResponse;
import com.betfair.cougar.health.service.v3.to.SubComponentStatus;
import com.betfair.cougar.logging.CougarLoggingUtils;
import org.slf4j.LoggerFactory;
import com.betfair.tornjak.monitor.ActiveMethodMonitor;
import com.betfair.tornjak.monitor.ErrorCountingPolicy;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusAggregator;
import com.betfair.tornjak.monitor.StatusChangeListener;
import com.betfair.tornjak.monitor.active.Check;
import com.betfair.tornjak.monitor.service.InOutServiceMonitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthServiceImplTest {
    private HealthServiceImpl impl;
    private HealthService service;
    private HealthService otherService;
    private MonitorRegistry monitorRegistry;
    private ContainerContext containerContext;
    private SavingRequestContext requestContext;
    private InOutServiceMonitor inOutServiceMonitor;

    @BeforeClass
    public static void suppressLogs() {
        CougarLoggingUtils.suppressAllRootLoggerOutput();
    }


    @Before
    public void before() throws IOException {
        monitorRegistry = mock(MonitorRegistry.class);
        impl = new HealthServiceImpl();
        containerContext = mock(ContainerContext.class);
        when(containerContext.getMonitorRegistry()).thenReturn(monitorRegistry);
        impl.init(containerContext);
        service = impl;
        inOutServiceMonitor = new InOutServiceMonitor(new File("service.status"));
        inOutServiceMonitor.setInService(true);
        when(monitorRegistry.getMonitorSet()).thenReturn(monitors(inOutServiceMonitor));

        otherService = mock(HealthService.class);

        requestContext = new SavingRequestContext();
    }

    private static Set<Monitor> monitors(Monitor... monitors) {
        final Map<Monitor, Integer> monitorIndexes = new HashMap<>();
        for (int i=0; i<monitors.length; i++) {
            monitorIndexes.put(monitors[i], i);
        }
        Set<Monitor> ret = new TreeSet<>(new Comparator<Monitor>() {
            @Override
            public int compare(Monitor o1, Monitor o2) {
                return monitorIndexes.get(o1) - monitorIndexes.get(o2);
            }
        });
        ret.addAll(Arrays.asList(monitors));
        return ret;
    }

    @Test
    public void isHealthy_Disabled() throws HealthException, IOException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });

        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.OK));
        inOutServiceMonitor.setInService(false);
        HealthSummaryResponse response = service.isHealthy(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(RestrictedHealthStatus.FAIL, response.getHealthy());
    }

    @Test
    public void isHealthy_Ok() throws HealthException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.OK));
        HealthSummaryResponse response = getResponse();
        assertEquals(RestrictedHealthStatus.OK, response.getHealthy());
    }

    @Test
    public void isHealthy_Warn() throws HealthException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.WARN));
        HealthSummaryResponse response = getResponse();
        assertEquals(RestrictedHealthStatus.OK, response.getHealthy());
    }

    @Test
    public void isHealthy_Fail() throws HealthException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.FAIL));
        HealthSummaryResponse response = getResponse();
        assertEquals(RestrictedHealthStatus.FAIL, response.getHealthy());
    }

    @Test
    public void getDetailedHealthStatus_OutOfService_NoOtherSubComponents() throws HealthException, IOException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.OK));
        inOutServiceMonitor.setInService(false);
        HealthDetailResponse response = service.getDetailedHealthStatus(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(HealthStatus.OUT_OF_SERVICE, response.getHealth());
        assertEquals(1, response.getSubComponentList().size());
    }

    @Test
    public void getDetailedHealthStatus_Ok_NoOtherSubComponents() throws HealthException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>())
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.OK));
        HealthDetailResponse response = service.getDetailedHealthStatus(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(HealthStatus.OK, response.getHealth());
        assertEquals(1, response.getSubComponentList().size());
    }

    @Test
    public void getDetailedHealthStatus_Ok_OtherSubComponents() throws HealthException, IOException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>()),
                otherServiceInfo()
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.OK));
        inOutServiceMonitor.setInService(true);
        DefaultMonitor otherMonitor = new DefaultMonitor("Fred", Status.OK);
        when(monitorRegistry.getMonitorSet()).thenReturn(monitors(inOutServiceMonitor, otherMonitor));

        HealthDetailResponse response = service.getDetailedHealthStatus(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(HealthStatus.OK, response.getHealth());


        List<SubComponentStatus> subComponents = response.getSubComponentList();
        assertEquals(2, subComponents.size());
        assertEquals(HealthStatus.OK, subComponents.get(0).getStatus());
        assertEquals("InOutServiceMonitor", subComponents.get(0).getName());
        assertEquals(HealthStatus.OK, subComponents.get(1).getStatus());
        assertEquals("Fred", subComponents.get(1).getName());
    }

    @Test
    public void getDetailedHealthStatus_Warn_WithSubComponents() throws HealthException, IOException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>()),
                otherServiceInfo()
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.WARN));
        inOutServiceMonitor.setInService(true);
        DefaultMonitor otherMonitor = new DefaultMonitor("Fred", Status.OK);
        when(monitorRegistry.getMonitorSet()).thenReturn(monitors(inOutServiceMonitor, otherMonitor));

        HealthDetailResponse response = service.getDetailedHealthStatus(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(HealthStatus.WARN, response.getHealth());


        List<SubComponentStatus> subComponents = response.getSubComponentList();
        assertEquals(2, subComponents.size());
        assertEquals(HealthStatus.OK, subComponents.get(0).getStatus());
        assertEquals("InOutServiceMonitor", subComponents.get(0).getName());
        assertEquals(HealthStatus.OK, subComponents.get(1).getStatus());
        assertEquals("Fred", subComponents.get(1).getName());
    }

    @Test
    public void getDetailedHealthStatus_Fail_WithSubComponents() throws HealthException, IOException {
        when(containerContext.getRegisteredServices()).thenReturn(new ServiceInfo[] {
                new ServiceInfo(null, service, "HealthService", "3.0", new ArrayList<String>()),
                otherServiceInfo()
        });
        when(monitorRegistry.getStatusAggregator()).thenReturn(new DefaultStatusAggregator(Status.FAIL));
        inOutServiceMonitor.setInService(true);
        DefaultMonitor otherMonitor = new DefaultMonitor("Fred", Status.OK);
        when(monitorRegistry.getMonitorSet()).thenReturn(monitors(inOutServiceMonitor, otherMonitor));

        HealthDetailResponse response = service.getDetailedHealthStatus(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        assertEquals(HealthStatus.FAIL, response.getHealth());


        List<SubComponentStatus> subComponents = response.getSubComponentList();
        assertEquals(2, subComponents.size());
        assertEquals(HealthStatus.OK, subComponents.get(0).getStatus());
        assertEquals("InOutServiceMonitor", subComponents.get(0).getName());
        assertEquals(HealthStatus.OK, subComponents.get(1).getStatus());
        assertEquals("Fred", subComponents.get(1).getName());
    }

    private HealthSummaryResponse getResponse() throws HealthException {
    	HealthSummaryResponse response = service.isHealthy(requestContext, DefaultTimeConstraints.NO_CONSTRAINTS);
        response.validateMandatory();
        return response;

    }
    private ServiceInfo otherServiceInfo() {
        List<String> operations = new ArrayList<>();
        operations.add("isHealthy");
        operations.add("getDetailedHealthStatus");
        return new ServiceInfo(null, otherService, "OtherService", "1.0", operations);
    }

    private class DefaultMonitor implements ActiveMethodMonitor {

        private String name;
        private Status status;

        private DefaultMonitor(String name, Status status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public void success() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void failure(Throwable throwable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void failure(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ErrorCountingPolicy getErrorCountingPolicy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Check getActiveMonitor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Status getMaxImpactToOverallStatus() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void addStatusChangeListener(StatusChangeListener statusChangeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeStatusChangeListener(StatusChangeListener statusChangeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }

    private class DefaultStatusAggregator implements StatusAggregator {
        private Status status;

        private DefaultStatusAggregator(Status status) {
            this.status = status;
        }

        @Override
        public void addStatusChangeListener(StatusChangeListener statusChangeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeStatusChangeListener(StatusChangeListener statusChangeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }

    private class SavingRequestContext implements RequestContext {
		@Override
		public void addEventLogRecord(LoggableEvent record) {
		}

		@Override
		public RequestUUID getRequestUUID() {
			return null;
		}

		@Override
		public void setRequestLogExtension(LogExtension extension) {
		}

        @Override
        public void setConnectedObjectLogExtension(LogExtension extension) {
        }

        @Override
        public LogExtension getConnectedObjectLogExtension() {
            return null;
        }

        @Override
		public void trace(String msg, Object... args) {
		}

		@Override
		public GeoLocationDetails getLocation() {
			return null;
		}

		@Override
		public IdentityChain getIdentity() {
			return null;
		}


		@Override
		public Date getReceivedTime() {
			return null;
		}

        @Override
        public Date getRequestTime() {
            return null;
        }

        @Override
        public boolean traceLoggingEnabled() {
            return false;
        }

        @Override
        public int getTransportSecurityStrengthFactor() {
            return 0;
        }

        @Override
        public boolean isTransportSecure() {
            return false;
        }
    }

}
