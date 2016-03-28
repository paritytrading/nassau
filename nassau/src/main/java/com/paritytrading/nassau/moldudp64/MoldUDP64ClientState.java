package com.paritytrading.nassau.moldudp64;

/**
 * The state of a MoldUDP64 client in relation to the MoldUDP64 server.
 */
public enum MoldUDP64ClientState {

    /**
     * The state of the client in relation to the server is unknown.
     */
    UNKNOWN,

    /**
     * The client is yet to achieve synchronization with the server.
     */
    BACKFILL,

    /**
     * The client is in synchronization with the server.
     */
    SYNCHRONIZED,

    /**
     * The client has fallen out of synchronization with the server.
     */
    GAP_FILL,

}
