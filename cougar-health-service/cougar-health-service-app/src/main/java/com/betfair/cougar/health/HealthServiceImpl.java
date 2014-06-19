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

package com.betfair.cougar.health;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.betfair.cougar.core.api.ev.TimeConstraints;
import com.betfair.cougar.core.impl.DefaultTimeConstraints;
import com.betfair.tornjak.monitor.service.InOutServiceMonitor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.betfair.cougar.api.ContainerContext;
import com.betfair.cougar.api.RequestContext;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.health.service.v3.HealthService;
import com.betfair.cougar.health.service.v3.enumerations.HealthExceptionErrorCodeEnum;
import com.betfair.cougar.health.service.v3.enumerations.HealthStatus;
import com.betfair.cougar.health.service.v3.enumerations.RestrictedHealthStatus;
import com.betfair.cougar.health.service.v3.exception.HealthException;
import com.betfair.cougar.health.service.v3.to.HealthDetailResponse;
import com.betfair.cougar.health.service.v3.to.HealthSummaryResponse;
import com.betfair.cougar.health.service.v3.to.SubComponentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.tornjak.monitor.Monitor;
import com.betfair.tornjak.monitor.MonitorRegistry;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusAggregator;


@ManagedResource
public class HealthServiceImpl implements HealthService {
	final static Logger LOGGER = LoggerFactory.getLogger(HealthServiceImpl.class);

	private MonitorRegistry monitorRegistry;

	@Override
	public void init(ContainerContext cc) {
        this.monitorRegistry = cc.getMonitorRegistry();
	}

	@Override
	public HealthSummaryResponse isHealthy(final RequestContext ctx, TimeConstraints timeConstraints) throws HealthException {
		HealthSummaryResponse response = new HealthSummaryResponse();

		if (isSystemInService()) {
			response.setHealthy(getHealth());
		} else {
			response.setHealthy(RestrictedHealthStatus.FAIL);

		}
		return response;
	}

	private RestrictedHealthStatus getHealth() throws HealthException {
		RestrictedHealthStatus currentState = RestrictedHealthStatus.OK;

        if (monitorRegistry == null) {
            LOGGER.error("MonitorRegistry is null");
            throw new HealthException(ResponseCode.InternalError, HealthExceptionErrorCodeEnum.NULL);
        }
        StatusAggregator agg = monitorRegistry.getStatusAggregator();
        if (agg == null) {
            LOGGER.error("StatusAggregator is null");
            throw new HealthException(ResponseCode.InternalError, HealthExceptionErrorCodeEnum.NULL);
        }
        Status status = agg.getStatus();
        if (status == null) {
            LOGGER.error("Status is null");
            throw new HealthException(ResponseCode.InternalError, HealthExceptionErrorCodeEnum.NULL);
        }
        if (status.equals(Status.FAIL)) {
            currentState = RestrictedHealthStatus.FAIL;
        }
		return currentState;

	}

	@Override
	public HealthDetailResponse getDetailedHealthStatus(RequestContext reqCtx, TimeConstraints timeConstraints) throws HealthException {
		HealthDetailResponse detail = new HealthDetailResponse();

        List<SubComponentStatus> subStatuses = new ArrayList<>();
        for (Monitor m : monitorRegistry.getMonitorSet()) {
            SubComponentStatus scs = new SubComponentStatus();
            scs.setName(m.getName());
            Status monitorStatus = m.getStatus();
            scs.setStatus(getHealthStatus(monitorStatus));
            subStatuses.add(scs);
        }
        detail.setSubComponentList(subStatuses);
		if (!isSystemInService()) {
			detail.setHealth(HealthStatus.OUT_OF_SERVICE);
		} else {
			detail.setHealth(getHealthStatus(monitorRegistry.getStatusAggregator().getStatus()));
		}
		return detail;
	}

    private HealthStatus getHealthStatus(Status status) {
        switch (status) {
            case OK:
            case WARN:
            case FAIL:
                return HealthStatus.valueOf(status.name());
        }
        throw new IllegalArgumentException("Cannot convert Status."+status+" to a HealthStatus");
    }

	@ManagedAttribute
	public boolean isSystemInService() {
        for (Monitor m : monitorRegistry.getMonitorSet()) {
            if (m instanceof InOutServiceMonitor) {
                return ((InOutServiceMonitor)m).isInService();
            }
        }
		return true;
	}


	@ManagedAttribute
	public boolean isSystemHealthy() {
		try {
			HealthStatus status = getDetailedHealthStatus(null, DefaultTimeConstraints.NO_CONSTRAINTS).getHealth();
			return status == HealthStatus.OK || status == HealthStatus.WARN;

		} catch (HealthException e) {
			return false;
		}
	}
}
