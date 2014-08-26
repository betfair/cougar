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

package com.betfair.cougar.transport.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.betfair.cougar.netutil.nio.HeapDelta;
import com.betfair.cougar.netutil.nio.TerminateSubscription;
import com.betfair.cougar.netutil.nio.connected.InitialUpdate;
import com.betfair.cougar.netutil.nio.connected.Update;
import com.betfair.cougar.netutil.nio.connected.UpdateAction;
import com.betfair.cougar.transport.api.protocol.CougarObjectOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Implementation of CougarObjectOutput usable for using complex expectations on multi-threaded code
 */
public class ExpectingOutput implements CougarObjectOutput, Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(ExpectingOutput.class);

    private List<Update> expectedUpdates;
    private int expectedUpdatesCurrentIndex = 0;
    private int currentUpdateNextIndex = 0;
    private long maxTimeBetween;
    private AtomicReference<CountDownLatch> latchRef = new AtomicReference<CountDownLatch>();
    private List<Object> allValues = new ArrayList<Object>();
    private List<TerminateSubscription> subTerminations = new ArrayList<TerminateSubscription>();
    private List<TerminateSubscription> expectedSubTerminations = new ArrayList<TerminateSubscription>();

    public ExpectingOutput(long maxTimeBetween) {
        this.maxTimeBetween = maxTimeBetween;
        latchRef.set(new CountDownLatch(1));
    }

    @Override
    public void writeObject(Object object) throws IOException {
        allValues.add(object);
        if (!(object instanceof HeapDelta)) {
            if (!(object instanceof TerminateSubscription)) {
                notifyFailure("Attempt to write unexpected object: "+object);
            }
            else {
                subTerminations.add((TerminateSubscription)object);
            }
            return;
        }
        HeapDelta delta = (HeapDelta) object;
        if (expectedUpdates == null) {
            return;
        }
        if (expectedUpdatesCurrentIndex >= expectedUpdates.size()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received unexpected update: "+delta);
            }
            notifyFailure("Received unexpected update: "+delta);
            return;
        }
        List<Update> updates = delta.getUpdates();
        Update nextExpected = expectedUpdates.get(expectedUpdatesCurrentIndex);
        if (nextExpected instanceof InitialUpdate) {
            // initial updates are always atomic
            expectedUpdatesCurrentIndex++;
            InitialUpdate expectedInitial = (InitialUpdate) nextExpected;
            if (updates.size() > 1) {
                notifyFailure("Received >1 update for initial update: "+delta);
                return;
            }

            if (updates.isEmpty()) {
                notifyFailure("Received zero length update list for initial update: "+delta);
                return;
            }

            Update actualInitial = updates.get(0);
            if (!expectedInitial.equals(actualInitial)) {
                notifyFailure("Expected: "+expectedInitial+", got: "+actualInitial);
                return;
            }
            else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found expected initial: "+actualInitial);
                }
            }

            // if more to come then move along, otherwise we can stop now..
            if (expectedUpdatesCurrentIndex < expectedUpdates.size()) {
                latchRef.getAndSet(new CountDownLatch(1)).countDown();
            }
            else {
                latchRef.getAndSet(null).countDown();
            }
        }
        // ok, not an the initial update, now we're interested in the updates
        else {
            List<UpdateAction> allActions = new ArrayList<UpdateAction>();
            for (Update u : updates) {
                for (UpdateAction ua : u.getActions()) {
                    allActions.add(ua);
                }
            }
            if (expectedUpdates != null) {
                int waitingFor = nextExpected.getActions().size() - currentUpdateNextIndex;
                if (waitingFor < allActions.size()) {
                    notifyFailure("Found more actions than I'm waiting for: "+allActions);
                    return;
                }

                // now check each in turn
                for (int i=0; i<allActions.size(); i++) {
                    UpdateAction expected = nextExpected.getActions().get(currentUpdateNextIndex + i);
                    UpdateAction actual = allActions.get(i);
                    if (!expected.equals(actual)) {
                        notifyFailure("Expected: "+expected+", got: "+actual);
                        return;
                    }
                    else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Found expected update: " + actual);
                        }
                    }
                }

                currentUpdateNextIndex += allActions.size();
                if (currentUpdateNextIndex < nextExpected.getActions().size()) {
                    latchRef.getAndSet(new CountDownLatch(1)).countDown();
                }
                else {
                    expectedUpdatesCurrentIndex++;
                    currentUpdateNextIndex=0;
                    // if more to come then move along, otherwise we can stop now..
                    if (expectedUpdatesCurrentIndex < expectedUpdates.size()) {
                        latchRef.getAndSet(new CountDownLatch(1)).countDown();
                    }
                    else {
                        latchRef.getAndSet(null).countDown();
                    }
                }
            }
        }
    }

    public void run() {
        while (true) {
            CountDownLatch latch = latchRef.get();
            // null latch means all over
            if (latch != null) {
                boolean success = false;
                try {
                    // latch completing means we received the update in time
                    success = latch.await(maxTimeBetween, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
                if (!success) {
                    notifyFailure("Didn't receive write within "+maxTimeBetween+"ms");
                }
            }
            else {
                notifyComplete();
                break;
            }
        }

    }

    private List<ExpectingOutputListener> listeners = new CopyOnWriteArrayList<ExpectingOutputListener>();
    public void addListener(ExpectingOutputListener l) {
        listeners.add(l);
    }

    private void notifyFailure(String s) {
        new Exception(s).printStackTrace();
        for (ExpectingOutputListener l : listeners) {
            l.failure(s);
        }
    }

    private void notifyComplete() {
        for (ExpectingOutputListener l : listeners) {
            l.complete();
        }
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void writeString(String arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void writeBytes(byte[] arg0, int arg1, int arg2) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBytes(byte[] buffer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDouble(double value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInt(int value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLong(long value) throws IOException {
        throw new UnsupportedOperationException();
    }

    public List<Object> getAllValues() {
        return allValues;
    }

    public void setExpectedUpdates(List<Update> expectedUpdates) {
        this.expectedUpdates = normaliseUpdates(expectedUpdates);
    }

    public void setExpectedSubTerminations(List<TerminateSubscription> expectedSubTerminations) {
        this.expectedSubTerminations = expectedSubTerminations;
    }

    public List<TerminateSubscription> getExpectedSubTerminations() {
        return expectedSubTerminations;
    }

    public List<TerminateSubscription> getSubTerminations() {
        return subTerminations;
    }

    private List<Update> normaliseUpdates(List<Update> expectedUpdates) {
        List<Update> ret = new ArrayList<Update>();
        Update lastNormalUpdate = null;
        for (Update u : expectedUpdates) {
            if (u instanceof InitialUpdate) {
                ret.add(u);
                lastNormalUpdate = null;
                continue;
            }
            if (lastNormalUpdate == null) {
                ret.add(u);
                lastNormalUpdate = u;
                continue;
            }

            lastNormalUpdate.getActions().addAll(u.getActions());
        }
        return ret;
    }

    public static interface ExpectingOutputListener {
        void failure(String s);

        void complete();
    }
}
