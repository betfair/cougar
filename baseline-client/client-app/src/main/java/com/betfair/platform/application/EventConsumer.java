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

package com.betfair.platform.application;

import com.betfair.baseline.v2.BaselineClient;
import com.betfair.baseline.v2.events.MatchedBet;
import com.betfair.baseline.v2.events.TimeTick;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.ev.Subscription;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Date;

/**
 * Basic class for consuming Events from the configured JMS transport
 */
public class EventConsumer implements ApplicationListener {
    private BaselineClient client;

    private Subscription timeTickSubscription;
    private Subscription matchedBetSubscription;

    public void destroy() {
        timeTickSubscription.close();
        matchedBetSubscription.close();
    }

    public void init() {
        subscribe();
    }


    private void subscribe() {
        BaseExecutionContext ctx = new BaseExecutionContext();

        client.subscribeToTimeTick(ctx, new Object[0],
            new ExecutionObserver() {
                @Override
                public void onResult(ExecutionResult result) {
                    switch (result.getResultType()) {
                        case Subscription:
                            timeTickSubscription = result.getSubscription();
                            break;
                        case Success:
                            TimeTick timeTick = (TimeTick)result.getResult();
                            System.out.println("Time service tick: " + timeTick.getTime());
                            break;
                        case Fault:
                            System.out.println(result.getFault());
                            break;
                    }
                }
        });

        client.subscribeToMatchedBet(ctx, new Object[0], new ExecutionObserver() {

            @Override
            public void onResult(ExecutionResult result) {
                switch (result.getResultType()) {
                    case Subscription:
                        matchedBetSubscription = result.getSubscription();
                        break;
                    case Success:
                        MatchedBet bet = (MatchedBet)result.getResult();
                        System.out.println("received bet object: " + bet.toString());
                        break;
                    case Fault:
                        System.out.println(result.getFault());
                        break;
                }
            }
        });
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println(event.getClass().getName());

        if (event instanceof ContextRefreshedEvent) {
            subscribe();
        }
    }

    private class BaseExecutionContext implements ExecutionContext {

        @Override
        public GeoLocationDetails getLocation() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public IdentityChain getIdentity() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public RequestUUID getRequestUUID() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Date getReceivedTime() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Date getRequestTime() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean traceLoggingEnabled() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getTransportSecurityStrengthFactor() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isTransportSecure() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

    }

    public BaselineClient getClient() {
        return client;
    }

    public void setClient(BaselineClient client) {
        this.client = client;
    }
}
