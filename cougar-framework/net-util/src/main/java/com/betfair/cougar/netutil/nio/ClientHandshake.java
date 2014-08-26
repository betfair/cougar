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

package com.betfair.cougar.netutil.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * State object holding the current state of the handshake.
 */
public class ClientHandshake {
    private static final Logger LOG = LoggerFactory.getLogger(ClientHandshake.class);

    public static String HANDSHAKE = "handshake";

    private final CountDownLatch latch = new CountDownLatch(1);
    private boolean success;

    public ClientHandshake() {
    }

    public void accept() {
        success = true;
        latch.countDown();
    }

    public void reject() {
        success = false;
        latch.countDown();
    }

    public boolean successful() {
        return success;
    }

    public boolean await(long timeout) {
        boolean ret = false;
        try {
            ret = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!ret) {
                LOG.warn("Handshake response not received within " + timeout + " milliseconds");
            }
        } catch (InterruptedException e) {
            // arse to it.
        }
        return ret;
    }
}
