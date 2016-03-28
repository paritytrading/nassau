package com.paritytrading.nassau.binaryfile;

import com.paritytrading.nassau.ProtocolException;

/**
 * Indicates a protocol error while handling the BinaryFILE file format.
 */
public class BinaryFILEException extends ProtocolException {

    /**
     * Construct an instance with the specified detail message.
     *
     * @param message the detail message
     */
    public BinaryFILEException(String message) {
        super(message);
    }

}
