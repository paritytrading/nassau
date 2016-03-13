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

    /*
     * The RX buffer length on the client side must be equal to or greater than
     * the length of the payload in a Login Accepted packet.
     */
    private static final int MIN_MAX_PAYLOAD_LENGTH = 30;

    private LoginAccepted loginAccepted;
    private LoginRejected loginRejected;

    private MessageListener listener;

    private SoupBinTCPClientStatusListener statusListener;

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
        this(SystemClock.INSTANCE, channel, MAX_PACKET_LENGTH - 1, listener, statusListener);
    }

    /**
     * Create a client. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param channel the underlying socket channel
     * @param maxPayloadLength maximum inbound message length
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPClient(SocketChannel channel, int maxPayloadLength,
            MessageListener listener, SoupBinTCPClientStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, maxPayloadLength, listener, statusListener);
    }

    /**
     * Create a client. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param clock a clock
     * @param channel the underlying socket channel
     * @param maxPayloadLength maximum inbound message length
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPClient(Clock clock, SocketChannel channel, int maxPayloadLength,
            MessageListener listener, SoupBinTCPClientStatusListener statusListener) {
        super(clock, channel, Math.max(MIN_MAX_PAYLOAD_LENGTH, maxPayloadLength),
                PACKET_TYPE_CLIENT_HEARTBEAT);

        this.loginAccepted = new LoginAccepted();
        this.loginRejected = new LoginRejected();

        this.listener = listener;

        this.statusListener = statusListener;
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

    @Override
    protected void heartbeatTimeout() throws IOException {
        statusListener.heartbeatTimeout();
    }

    @Override
    protected void packet(byte packetType, ByteBuffer payload) throws IOException {
        switch (packetType) {
        case PACKET_TYPE_DEBUG:
            break;
        case PACKET_TYPE_LOGIN_ACCEPTED:
            loginAccepted.get(payload);
            statusListener.loginAccepted(loginAccepted);
            break;
        case PACKET_TYPE_LOGIN_REJECTED:
            loginRejected.get(payload);
            statusListener.loginRejected(loginRejected);
            break;
        case PACKET_TYPE_SEQUENCED_DATA:
            listener.message(payload);
            break;
        case PACKET_TYPE_SERVER_HEARTBEAT:
            break;
        case PACKET_TYPE_END_OF_SESSION:
            statusListener.endOfSession();
            break;
        default:
            unexpectedPacketType(packetType);
        }
    }

}
