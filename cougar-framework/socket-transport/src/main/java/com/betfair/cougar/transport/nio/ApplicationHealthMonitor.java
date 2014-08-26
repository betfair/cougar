/*
 * Copyright 2014, The Sporting Exchange Limited
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

package com.betfair.cougar.transport.nio;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.betfair.tornjak.monitor.MonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.betfair.cougar.api.Service;
import com.betfair.cougar.core.api.ServiceAware;
import com.betfair.cougar.transport.nio.HealthMonitorStrategy.HealthMonitorStrategyListener;
import com.betfair.tornjak.monitor.Status;
import com.betfair.tornjak.monitor.StatusAggregator;
import com.betfair.tornjak.monitor.StatusChangeEvent;
import com.betfair.tornjak.monitor.StatusChangeListener;

/**
 * Actively monitors the state of the host application to inform interested parties (via a configurable strategy) of the host's health
 * Can be configured to either actively or passively monitor the application health.  If passive then subscribes to updates from StatusAggregators, this assumes
 * an external party (such as netscalers) are causing status updates.  If active monitoring then a thread is started to periodically poll application health.
 * </p>
 * If any service hosted in the container is unhealthy, then the result is 'unhealthy'
 * </p>
 * Only status 'FAIL' is considered unhealthy (WARN is not)
 * </p>
 * passive monitoring only receives updates when the status changes, so it's important a suitable strategy is used.  DebounceHealthMonitorStrategy suites
 * passive monitoring, CountingHealthMonitorStrategy suites active monitoring.
 */
public class ApplicationHealthMonitor  implements ServiceAware {

	private static final Logger log = LoggerFactory.getLogger(ApplicationHealthMonitor.class);

	private final long 					monitorInterval;
	private final HealthMonitorStrategy strategy;
    private final MonitorRegistry monitorRegistry;


    public ApplicationHealthMonitor(final ExecutionVenueNioServer nioServer,
                                    HealthMonitorStrategy strategy,
                                    long monitorInterval,
                                    MonitorRegistry monitorRegistry) {
        this.monitorInterval = monitorInterval;
        this.strategy = strategy;
        this.strategy.registerListener(new HealthMonitorStrategyListener() {

			@Override
			public void onUpdate(boolean isHealthy) {
				if (log.isDebugEnabled()) {
					log.debug("updating health state to " + isHealthy);
				}
				nioServer.setHealthState(isHealthy);

			}
		});
        this.monitorRegistry = monitorRegistry;

        log.info("socket health monitor using strategy " + strategy.getClass().getName());
    }

	@Override
	public void setServices(Set<Service> services) {
		if (monitorInterval > 0) {
			startActiveMonitoring(monitorRegistry.getStatusAggregator());
		}
		else {
			startPassiveMonitoring(monitorRegistry.getStatusAggregator());
		}

	}


	/**
	 * Add listeners to all status aggregators to be advised when status of the aggregator changes
	 * </p>
	 * Keep track of all current status.  if, after update, all status are healthy then overall status = healthy, otherwise = unhealthy.
	 * </p>
	 * advise strategy only if the overall status has changed
	 */
    private void startPassiveMonitoring(final StatusAggregator aggregator) {

    	log.info("Starting application health monitoring in PASSIVE mode");

        final AtomicBoolean health = new AtomicBoolean();
        health.set(!Status.FAIL.equals(aggregator.getStatus()));

        aggregator.addStatusChangeListener(new StatusChangeListener() {

            @Override
            public void statusChanged(StatusChangeEvent event) {
                boolean healthStatusChanged ;
                boolean isHealthy;
                synchronized (health) {
                    boolean currentOverallHealth = health.get();  //true iff all services are healthy
                    isHealthy = !Status.FAIL.equals(event.getNewStatus()); 				//service is healthy if not fail status
                    health.set(isHealthy);
                    healthStatusChanged = currentOverallHealth != isHealthy;
                }
                if (healthStatusChanged) {
                    strategy.update(isHealthy);
                }
            }
        });
		strategy.update(health.get());
	}

    /**
     * Start a new thread and periodically poll all status aggregators for their current status
     * </p>
     * Calculate a new status where newStatus = healthy if all aggregator's status = healthy
     */
	private void startActiveMonitoring(final StatusAggregator aggregator) {

    	log.info("Starting application health monitoring in ACTIVE mode");

    	ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r,"SocketTransport App Health Monitor");
				t.setDaemon(true);
				return t;
			}
		});

    	executor.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
                try {
                    boolean healthy = !Status.FAIL.equals(aggregator.getStatus());

                    setStatus(healthy ? Status.OK : Status.FAIL);
                }
                catch (Exception e) {
                    log.warn("Error whilst setting health status",e);
                }
			}
		}, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
    }


    private void setStatus(Status status) {
        boolean isUnhealthy = Status.FAIL.equals(status);
        strategy.update(!isUnhealthy);
    }

}
