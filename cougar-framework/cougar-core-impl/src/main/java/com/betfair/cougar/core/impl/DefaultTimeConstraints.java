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

package com.betfair.cougar.core.impl;

import com.betfair.cougar.core.api.ev.TimeConstraints;

import java.util.Date;

/**
 *
 */
public class DefaultTimeConstraints implements TimeConstraints {

    private Long expiryTime;

    private DefaultTimeConstraints(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public Long getExpiryTime() {
        return expiryTime;
    }

    @Override
    public Long getTimeRemaining() {
        if (expiryTime == null) {
            return null;
        }
        return expiryTime - System.currentTimeMillis();
    }

    public static final TimeConstraints NO_CONSTRAINTS = new DefaultTimeConstraints(null);

    public static TimeConstraints fromExpiryTime(long expiryTime) {
        return new DefaultTimeConstraints(expiryTime);
    }

    public static TimeConstraints fromTimeout(long timeout) {
        if (timeout <= 0) {
            return DefaultTimeConstraints.NO_CONSTRAINTS;
        }
        return new DefaultTimeConstraints(System.currentTimeMillis()+timeout);
    }

    public static TimeConstraints rebaseFromNewStartTime(Date requestTime, TimeConstraints rawTimeConstraints) {
        if (rawTimeConstraints.getTimeRemaining() == null) {
            return NO_CONSTRAINTS;
        }
        long timeRemaining = rawTimeConstraints.getTimeRemaining();
        return new DefaultTimeConstraints(requestTime.getTime()+timeRemaining);
    }

    @Override
    public String toString() {
        return "DefaultTimeConstraints{" +
                "expiryTime=" + expiryTime +
                '}';
    }
}
