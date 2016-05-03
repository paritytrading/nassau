package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCP.*;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.util.Clock;
import com.paritytrading.nassau.util.SystemClock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * An implementation of the server side of the protocol.
 */
public class SoupBinTCPServer extends SoupBinTCPSession {

    /*
     * The RX buffer length on the server side must be equal to or greater than
     * the length of the payload in a Login Request packet.
     */
    private static final int MIN_MAX_PAYLOAD_LENGTH = 46;

    private LoginRequest loginRequest;

    private ByteBuffer txPayload;

    private MessageListener listener;

    private SoupBinTCPServerStatusListener statusListener;

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
        this(channel, MAX_PACKET_LENGTH - 1, listener, statusListener);
    }

    /**
     * Create a server. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param channel the underlying socket channel
     * @param maxPayloadLength the maximum inbound message length
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPServer(SocketChannel channel, int maxPayloadLength,
            MessageListener listener, SoupBinTCPServerStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, maxPayloadLength, listener, statusListener);
    }

    /**
     * Create a server. The underlying socket channel can be either blocking
     * or non-blocking.
     *
     * @param clock a clock
     * @param channel the underlying socket channel
     * @param maxPayloadLength the maximum inbound message length
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public SoupBinTCPServer(Clock clock, SocketChannel channel, int maxPayloadLength,
            MessageListener listener, SoupBinTCPServerStatusListener statusListener) {
        super(clock, channel, Math.max(MIN_MAX_PAYLOAD_LENGTH, maxPayloadLength),
                PACKET_TYPE_SERVER_HEARTBEAT);

        this.loginRequest = new LoginRequest();

        /*
         * This built-in payload transmit buffer is used for Login Accepted
         * and Login Rejected packets.
         */
        this.txPayload = ByteBuffer.allocate(30);

        this.listener = listener;

        this.statusListener = statusListener;
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

    @Override
    protected void heartbeatTimeout() throws IOException {
        statusListener.heartbeatTimeout(this);
    }

    @Override
    protected void packet(byte packetType, ByteBuffer payload) throws IOException {
        switch (packetType) {
        case PACKET_TYPE_DEBUG:
            break;
        case PACKET_TYPE_LOGIN_REQUEST:
            loginRequest.get(payload);
            statusListener.loginRequest(this, loginRequest);
            break;
        case PACKET_TYPE_UNSEQUENCED_DATA:
            listener.message(payload);
            break;
        case PACKET_TYPE_CLIENT_HEARTBEAT:
            break;
        case PACKET_TYPE_LOGOUT_REQUEST:
            statusListener.logoutRequest(this);
            break;
        default:
            unexpectedPacketType(packetType);
        }
    }

}
