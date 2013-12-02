package com.betfair.cougar.core.api.ev;

/**
 *
 */
public interface TimeConstraints {
    Long getExpiryTime();
    Long getTimeRemaining();
}