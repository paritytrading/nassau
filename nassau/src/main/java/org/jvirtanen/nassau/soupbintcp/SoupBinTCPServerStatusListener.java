package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;

/**
 * The interface for inbound status events on the server side.
 */
public interface SoupBinTCPServerStatusListener {

    /**
     * Receive an indication of a heartbeat timeout.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void heartbeatTimeout(SoupBinTCPServer session) throws IOException;

    /**
     * Receive a Login Request packet.
     *
     * @param session the session
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    void loginRequest(SoupBinTCPServer session, LoginRequest payload) throws IOException;

    /**
     * Receive a Logout Request packet.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void logoutRequest(SoupBinTCPServer session) throws IOException;

}
