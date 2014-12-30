/*
 * Copyright 2014, Simon Matić Langford
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

package com.betfair.testing.utils.cougar;

/**
 * Utility to aid invoking cougar baseline 2.8 for testing purposes.
 */
public class CougarBaseline2_8TestingInvoker {
    public static CougarTestingInvoker create() {
        return LegacyCougarTestingInvoker.create().setService("Baseline","cougarBaseline").setVersion("2.8");
    }
}
