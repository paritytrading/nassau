package com.paritytrading.nassau.util;

/**
 * The system time source.
 */
public class SystemClock implements Clock {

    public static final SystemClock INSTANCE = new SystemClock();

    private SystemClock() {
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

}
