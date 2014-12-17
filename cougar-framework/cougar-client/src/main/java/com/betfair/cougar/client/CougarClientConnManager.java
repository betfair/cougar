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

package com.betfair.cougar.client;

import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.impl.conn.tsccm.ConnPoolByRoute;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.TimeUnit;

/**
 * Expose counters on client connection pools in JMX.
 */
@ManagedResource
public class CougarClientConnManager extends ThreadSafeClientConnManager {

    public static final int TOTAL_CONNECTIONS = 20;

    @Override
    protected ConnPoolByRoute createConnectionPool(long connTTL, TimeUnit connTTLTimeUnit) {
        return new CougarConnPoolByRoute(connOperator, connPerRoute, TOTAL_CONNECTIONS, connTTL, connTTLTimeUnit);
    }

    @ManagedAttribute
    public int getFreeConnections() {
        return ((CougarConnPoolByRoute) pool).getFreeConnections();
    }


    private final class CougarConnPoolByRoute extends ConnPoolByRoute {
        private CougarConnPoolByRoute(ClientConnectionOperator operator, ConnPerRoute connPerRoute,
                                      int maxTotalConnections, long connTTL, TimeUnit connTTLTimeUnit) {
            super(operator, connPerRoute, maxTotalConnections, connTTL, connTTLTimeUnit);
        }

        public int getFreeConnections() {
            getLock().lock();
            try {
                return freeConnections.size();
            } finally {
                getLock().unlock();
            }
        }
    }
}
