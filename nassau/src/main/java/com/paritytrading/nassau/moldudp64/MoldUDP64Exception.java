package com.paritytrading.nassau.moldudp64;

import com.paritytrading.nassau.ProtocolException;

/**
 * Indicates a protocol error while handling the MoldUDP64 protocol.
 */
public class MoldUDP64Exception extends ProtocolException {

    /**
     * Construct an instance with the specified detail message.
     *
     * @param message the detail message.
     */
    public MoldUDP64Exception(String message) {
        super(message);
    }

}
