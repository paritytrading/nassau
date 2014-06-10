package org.jvirtanen.nassau.soupbintcp;

import java.io.IOException;

/**
 * An interface for inbound status events on both the client and server side.
 */
public interface SoupBinTCPSessionStatusListener {

    /**
     * Receive an indication of a heartbeat timeout.
     *
     * @throws IOException if an I/O error occurs
     */
    void heartbeatTimeout() throws IOException;

}
