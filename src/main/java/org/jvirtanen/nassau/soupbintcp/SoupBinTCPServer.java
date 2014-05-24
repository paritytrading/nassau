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
 * An implementation of the server side of the protocol.
 */
public class SoupBinTCPServer extends SoupBinTCPSession {

    /**
     * Create a server. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param channel the underlying socket channel
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPServer(SocketChannel channel, MessageListener listener,
            SoupBinTCPServerStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, listener, statusListener);
    }

    /**
     * Create a server. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param clock a clock
     * @param channel the underlying socket channel
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPServer(Clock clock, SocketChannel channel, final MessageListener listener,
            final SoupBinTCPServerStatusListener statusListener) {
        super(clock, channel, PACKET_TYPE_SERVER_HEARTBEAT, new PacketListener() {

            @Override
            public void debug(ByteBuffer buffer) throws IOException {
            }

            @Override
            public void loginAccepted(LoginAccepted payload) throws IOException {
                unexpectedPacketType(PACKET_TYPE_LOGIN_ACCEPTED);
            }

            @Override
            public void loginRejected(LoginRejected payload) throws IOException {
                unexpectedPacketType(PACKET_TYPE_LOGIN_REJECTED);
            }

            @Override
            public void sequencedData(ByteBuffer buffer) throws IOException {
                unexpectedPacketType(PACKET_TYPE_SEQUENCED_DATA);
            }

            @Override
            public void serverHeartbeat() throws IOException {
                unexpectedPacketType(PACKET_TYPE_SERVER_HEARTBEAT);
            }

            @Override
            public void endOfSession() throws IOException {
                unexpectedPacketType(PACKET_TYPE_END_OF_SESSION);
            }

            @Override
            public void loginRequest(LoginRequest payload) throws IOException {
                statusListener.loginRequest(payload);
            }

            @Override
            public void unsequencedData(ByteBuffer buffer) throws IOException {
                listener.message(buffer);
            }

            @Override
            public void clientHeartbeat() {
            }

            @Override
            public void logoutRequest() throws IOException {
                statusListener.logoutRequest();
            }

        }, statusListener);
    }

    /**
     * Send a Login Accepted packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    public void accept(LoginAccepted payload) throws IOException {
        txPayload.clear();
        payload.put(txPayload);
        txPayload.flip();

        send(PACKET_TYPE_LOGIN_ACCEPTED, txPayload);
    }

    /**
     * Send a Login Rejected packet.
     *
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    public void reject(LoginRejected payload) throws IOException {
        txPayload.clear();
        payload.put(txPayload);
        txPayload.flip();

        send(PACKET_TYPE_LOGIN_REJECTED, txPayload);
    }

    /**
     * Send an End Of Session packet.
     *
     * @throws IOException if an I/O error occurs
     */
    public void endSession() throws IOException {
        send(PACKET_TYPE_END_OF_SESSION);
    }

    /**
     * Send a Sequenced Data packet.
     *
     * @param buffer a buffer containing the packet payload
     * @throws IOException if an I/O error occurs
     */
    public void send(ByteBuffer buffer) throws IOException {
        send(PACKET_TYPE_SEQUENCED_DATA, buffer);
    }

}
