package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.Packets.*;
import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.util.Clock;
import org.jvirtanen.nassau.util.SystemClock;

/**
 * An implementation of the client side of the protocol.
 */
public class SoupBinTCPClient extends SoupBinTCPSession {

    /**
     * Create a client. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param channel the underlying socket channel
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPClient(SocketChannel channel, MessageListener listener,
            SoupBinTCPClientStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, listener, statusListener);
    }

    /**
     * Create a client. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param clock a clock
     * @param channel the underlying socket channel
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPClient(Clock clock, SocketChannel channel, final MessageListener listener,
            final SoupBinTCPClientStatusListener statusListener) {
        super(clock, channel, PACKET_TYPE_CLIENT_HEARTBEAT, new PacketListener() {

            @Override
            public void debug(ByteBuffer buffer) throws IOException {
            }

            @Override
            public void loginAccepted(LoginAccepted payload) throws IOException {
                statusListener.loginAccepted(payload);
            }

            @Override
            public void loginRejected(LoginRejected payload) throws IOException {
                statusListener.loginRejected(payload);
            }

            @Override
            public void sequencedData(ByteBuffer buffer) throws IOException {
                listener.message(buffer);
            }

            @Override
            public void serverHeartbeat() {
            }

            @Override
            public void endOfSession() throws IOException {
                statusListener.endOfSession();
            }

            @Override
            public void loginRequest(LoginRequest payload) throws IOException {
                unexpectedPacketType(PACKET_TYPE_LOGIN_REQUEST);
            }

            @Override
            public void unsequencedData(ByteBuffer buffer) throws IOException {
                unexpectedPacketType(PACKET_TYPE_UNSEQUENCED_DATA);
            }

            @Override
            public void clientHeartbeat() throws IOException {
                unexpectedPacketType(PACKET_TYPE_CLIENT_HEARTBEAT);
            }

            @Override
            public void logoutRequest() throws IOException {
                unexpectedPacketType(PACKET_TYPE_LOGOUT_REQUEST);
            }

        });
    }

    /**
     * Send a Login Request packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    public void login(LoginRequest payload) throws IOException {
        txPayload.clear();
        payload.put(txPayload);
        txPayload.flip();

        send(PACKET_TYPE_LOGIN_REQUEST, txPayload);
    }

    /**
     * Send a Logout Request packet.
     *
     * @throws IOException if an I/O error occurs
     */
    public void logout() throws IOException {
        send(PACKET_TYPE_LOGOUT_REQUEST);
    }

    /**
     * Send an Unsequenced Data packet.
     *
     * @param buffer a buffer containing the packet payload
     * @throws IOException if an I/O error occurs
     */
    public void send(ByteBuffer buffer) throws IOException {
        send(PACKET_TYPE_UNSEQUENCED_DATA, buffer);
    }

}
