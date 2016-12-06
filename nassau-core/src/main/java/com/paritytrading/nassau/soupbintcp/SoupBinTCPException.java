package com.paritytrading.nassau.soupbintcp;

import com.paritytrading.nassau.ProtocolException;

/**
 * Indicates a protocol error while handling the SoupBinTCP protocol.
 */
public class SoupBinTCPException extends ProtocolException {

    /**
     * Create an instance with the specified detail message.
     *
     * @param message the detail message
     */
    public SoupBinTCPException(String message) {
        super(message);
    }

}
