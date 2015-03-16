package com.betfair.cougar.util.time;

public class SystemClock implements Clock {

    @Override
    public long millis() {
        return System.currentTimeMillis();
    }
}
