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
        return new DefaultTimeConstraints(System.currentTimeMillis()+timeout);
    }

    public static TimeConstraints rebaseFromNewStartTime(Date requestTime, TimeConstraints rawTimeConstraints) {
        if (rawTimeConstraints.getTimeRemaining() == null) {
            return NO_CONSTRAINTS;
        }
        long timeRemaining = rawTimeConstraints.getTimeRemaining();
        return new DefaultTimeConstraints(requestTime.getTime()+timeRemaining);
    }
}
