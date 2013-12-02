package com.betfair.cougar.core.api.ev;

/**
 *
 */
public interface TimeConstraints {
    /**
     *
     * @return
     */
    Long getExpiryTime();

    /**
     *
     * @return
     */
    Long getTimeRemaining();
}