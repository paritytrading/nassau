package org.jvirtanen.nassau.moldudp64;

import java.nio.ByteBuffer;

/**
 * The interface for a MoldUDP64 message store. A message store is used by a
 * request server to fulfill incoming requests.
 */
public interface MoldUDP64MessageStore {

    /**
     * Retrieve zero or more messages. Start with the specified sequence number
     * and a message count of zero. Repeat the following algorithm until it
     * terminates:
     *
     * <ol>
     * <li>Check the current message count. If it is equal to the requested
     * message count, terminate the algorithm.</li>
     * <li>Find the message with the current sequence number. If the message
     * is not found, terminate the algorithm.</li>
     * <li>Check the buffer. If the buffer has fewer bytes remaining than are
     * required by a two-byte message header and the message, terminate the
     * algorithm.</li>
     * <li>Put a message header into the buffer. The message header consists
     * of one field: the message length encoded as an unsigned 16-bit big
     * endian integer.</li>
     * <li>Put the message to the buffer.</li>
     * <li>Increment the message count.</li>
     * <li>Increment the sequence number.</li>
     * </ol>
     *
     * <p>Return the message count.</p>
     * 
     * @param buffer a buffer
     * @param sequenceNumber the sequence number
     * @param requestedMessageCount the requested message count
     * @return the message count
     */
    int get(ByteBuffer buffer, long sequenceNumber, int requestedMessageCount);

}
