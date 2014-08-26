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

package com.betfair.cougar.util;

import com.betfair.platform.virtualheap.Heap;
import com.betfair.platform.virtualheap.HeapListener;
import com.betfair.platform.virtualheap.NodeType;
import com.betfair.platform.virtualheap.updates.Update;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectedObjectUtils {
    public static boolean waitForRootInstall(Heap heap, long timeoutMillis) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        // adds the listener first
        HeapListener listener = new HeapListener() {
            @Override
            public void applyUpdate(UpdateBlock update) {
                for (Update u : update.list()) {
                    if (u.getUpdateType() == Update.UpdateType.INSTALL_ROOT) {
                        result.set(true);
                    }
                }
                if (result.get()) {
                    latch.countDown();
                }
            }
        };
        heap.addListener(listener, false);
        // since that was an atomic operation, we can now check if we missed out whilst waiting to add the listener:
        if (heap.isRootInstalled()) {
            heap.removeListener(listener);
            return true;
        }

        // right, didn't appear to be there, so now we just wait as long as we've been asked for it to turn up..
        try {
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            return result.get();
        }
        catch (InterruptedException ie) {
            return result.get();
        }
        finally {
            heap.removeListener(listener);
        }
    }
}
