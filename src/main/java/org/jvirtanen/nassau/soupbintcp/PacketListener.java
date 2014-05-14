package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;
import java.nio.ByteBuffer;

interface PacketListener {

    void debug(ByteBuffer buffer) throws IOException;

    void loginAccepted(LoginAccepted payload) throws IOException;

    void loginRejected(LoginRejected payload) throws IOException;

    void sequencedData(ByteBuffer buffer) throws IOException;

    void serverHeartbeat() throws IOException;

    void endOfSession() throws IOException;

    void loginRequest(LoginRequest payload) throws IOException;

    void unsequencedData(ByteBuffer buffer) throws IOException;

    void clientHeartbeat() throws IOException;

    void logoutRequest() throws IOException;

}
