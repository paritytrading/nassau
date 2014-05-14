package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;

/**
 * The interface for inbound status events on the client side.
 */
public interface SoupBinTCPClientStatusListener {

    /**
     * Receive a Login Accepted packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    void loginAccepted(LoginAccepted payload) throws IOException;

    /**
     * Receive a Login Rejected packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    void loginRejected(LoginRejected payload) throws IOException;

    /**
     * Receive an End Of Session packet.
     *
     * @throws IOException if an I/O error occurs
     */
    void endOfSession() throws IOException;

}
