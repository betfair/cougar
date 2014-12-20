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

package com.betfair.cougar.client.socket;

import com.betfair.platform.virtualheap.conflate.Conflater;
import com.betfair.platform.virtualheap.conflate.SimpleConflater;
import com.betfair.platform.virtualheap.conflate.SmartConflater;
import com.betfair.platform.virtualheap.updates.UpdateBlock;

/**
 *
 */
public class ConflaterFactory {

    public static final String NONE = "none";
    public static final String SIMPLE = "simple";
    public static final String SMART = "smart";

    public static final Conflater NULL_CONFLATER = new NullConflater();

    // Valid values are: "none", "simple", "smart" or a fully qualified class name. Default is "none"
    private String conflater;

    public ConflaterFactory(String conflater) {
        this.conflater = conflater;
    }

    public Conflater getConflater() {
        // spring 2.5 has issues with null beans floating around, so we have a marker implementation to connote null
        if (conflater.equals(NONE)) {
            return NULL_CONFLATER;
        }
        else if (conflater.equals(SIMPLE)) {
            return new SimpleConflater();
        }
        else if (conflater.equals(SMART)) {
            return new SmartConflater();
        }
        else {
            try {
                return (Conflater) Class.forName(conflater).newInstance();
            }
            catch (Exception e) {
                throw new IllegalStateException("Can't initialise conflater: "+conflater, e);
            }
        }
    }

    private static class NullConflater implements Conflater {
        @Override
        public UpdateBlock conflate(UpdateBlock... updateBlocks) {
            throw new UnsupportedOperationException();
        }
    }
}
