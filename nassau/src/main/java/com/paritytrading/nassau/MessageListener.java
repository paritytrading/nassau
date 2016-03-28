package com.paritytrading.nassau;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The interface for inbound messages.
 */
public interface MessageListener {

    /**
     * Receive a message. The message is contained in the buffer between the
     * current position and the limit.
     *
     * @param buffer a buffer
     * @throws IOException if an I/O error occurs
     */
    void message(ByteBuffer buffer) throws IOException;

}
