/*
 * Copyright #{YEAR}, The Sporting Exchange Limited
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

package com.betfair.testing.utils.misc;

import com.betfair.testing.utils.cougar.misc.TimingHelpers;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class TestTimingHelpers {

    @Test
    public void summerTimeUTCConversion() {
        String date1 = TimingHelpers.convertUTCDateTimeToCougarFormat((int) 2009, (int) 6, (int) 1, (int) 13, (int) 50, (int) 0, (int) 0);
        assertEquals("2009-06-01T13:50:00.000Z",date1);
    }
}
