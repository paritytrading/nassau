package com.paritytrading.nassau.moldudp64;

import java.io.IOException;

/**
 * The interface for inbound status events on a MoldUDP64 client.
 */
public interface MoldUDP64ClientStatusListener {

    /**
     * Indicates that the client state changed.
     *
     * @param session the session
     * @param next the new client state
     * @throws IOException if an I/O error occurs
     */
    void state(MoldUDP64Client session, MoldUDP64ClientState next) throws IOException;

    /**
     * Indicates that a downstream packet was processed successfully.
     *
     * @param session the session
     * @param sequenceNumber the sequence number
     * @param messageCount the message count
     * @throws IOException if an I/O error occurs
     */
    void downstream(MoldUDP64Client session, long sequenceNumber,
            int messageCount) throws IOException;

    /**
     * Indicates that a request packet was sent.
     *
     * @param session the session
     * @param sequenceNumber the sequence number
     * @param requestedMessageCount the requested message count
     * @throws IOException if an I/O error occurs
     */
    void request(MoldUDP64Client session, long sequenceNumber,
            int requestedMessageCount) throws IOException;

    /**
     * Indicates that a downstream packet indicating the End of Session was
     * received.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void endOfSession(MoldUDP64Client session) throws IOException;

}
