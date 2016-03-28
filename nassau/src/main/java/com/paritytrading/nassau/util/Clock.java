package com.paritytrading.nassau.util;

/**
 * A time source.
 */
public interface Clock {

    /**
     * Get the current time in milliseconds.
     *
     * @return the current time in milliseconds
     */
    long currentTimeMillis();

}
