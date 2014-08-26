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

package com.betfair.cougar.transport.activemq;

import com.betfair.cougar.core.api.ev.ExecutionObserver;
import com.betfair.cougar.core.api.ev.ExecutionResult;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
//import org.apache.activemq.transport.TransportListener;
//import progress.message.jclient.ConnectionStateChangeListener;
//import progress.message.jclient.Constants;

import javax.jms.JMSException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ActiveMQSubscriptionEventListener  {

    private Set<WeakReference<ExecutionObserver>> executionObservers = new HashSet<WeakReference<ExecutionObserver>>();
    private ActiveMQEventTransportImpl eventTransport;

//    @Override
    public void connectionStateChanged(int state) {
//        switch (state) {
//            case Constants.FAILED:
//            case Constants.CLOSED:
                try {
                    eventTransport.closeConnection();
                } catch (JMSException e) {
                    // it's already closed, so this will happen every call, but we don't care ('cos we just want to make sure we've killed the reference to it)
                    // that way the observers can choose to re-subscribe (or whatever) and it will reconnect appropriately
                }
                notifyObservers(state);
//        }
    }

    public void addObserver(ExecutionObserver observer) {
        synchronized (executionObservers) {
            executionObservers.add(new WeakReference(observer));
        }
        cleanupObserverList();
    }

    public void notifyObservers(int state) {
        synchronized (executionObservers) {
            for (WeakReference<ExecutionObserver> ref : executionObservers) {
                ExecutionObserver obs = ref.get();
                if (obs != null) {
                    obs.onResult(new ExecutionResult(new CougarFrameworkException(ServerFaultCode.JMSTransportCommunicationFailure, "Connection to ActiveMQ has been lost")));
                }
            }
            executionObservers = new HashSet<WeakReference<ExecutionObserver>>();
        }
    }

    private void cleanupObserverList() {
        List<WeakReference> deadList = new ArrayList<WeakReference>();
        synchronized (executionObservers) {
            for (WeakReference<ExecutionObserver> ref : executionObservers) {
                ExecutionObserver obs = ref.get();
                if (obs == null) {
                    deadList.add(ref);
                }
            }
            executionObservers.removeAll(deadList);
        }
    }

    public boolean hasObservers(){
        cleanupObserverList();
        return executionObservers.size()>0;
    }

    public void setEventTransport(ActiveMQEventTransportImpl impl) {
        this.eventTransport = impl;
    }
}