package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;

/**
 * The interface for inbound status events on the server side.
 */
public interface SoupBinTCPServerStatusListener {

    /**
     * Receive a Login Request packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    void loginRequest(LoginRequest payload) throws IOException;

    /**
     * Receive a Logout Request packet.
     *
     * @throws IOException if an I/O error occurs
     */
    void logoutRequest() throws IOException;

}
