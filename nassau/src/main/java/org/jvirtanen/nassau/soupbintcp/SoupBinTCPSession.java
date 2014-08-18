package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.Packets.*;
import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import org.jvirtanen.nassau.util.Clock;

/**
 * The base for both the client and server side of the protocol.
 */
public abstract class SoupBinTCPSession implements Closeable {

    private static final long RX_HEARTBEAT_TIMEOUT_MILLIS  = 15000;
    private static final long TX_HEARTBEAT_INTERVAL_MILLIS =  1000;

    private Clock clock;

    private SocketChannel channel;

    private PacketParser parser;

    private SoupBinTCPSessionStatusListener statusListener;

    /*
     * These variables are written on data reception and data transmission,
     * respectively, and read on session keep-alive. All three functions can
     * run on different threads.
     */
    private volatile long lastRxMillis;
    private volatile long lastTxMillis;

    private ByteBuffer rxBuffer;

    /*
     * Both data transmission and session keep-alive write to the socket. These
     * functions can run on different threads.
     */
    private Object txLock;

    private   ByteBuffer txHeader;
    protected ByteBuffer txPayload;

    private ByteBuffer[] txBuffers;

    private ByteBuffer txHeartbeat;

    protected SoupBinTCPSession(Clock clock, SocketChannel channel, byte heartbeatPacketType,
            PacketListener listener, SoupBinTCPSessionStatusListener statusListener) {
        this.clock   = clock;
        this.channel = channel;
        this.parser  = new PacketParser(listener);

        this.statusListener = statusListener;

        this.lastRxMillis = clock.currentTimeMillis();
        this.lastTxMillis = clock.currentTimeMillis();

        this.rxBuffer  = ByteBuffer.allocate(2 + MAX_PACKET_LENGTH);

        this.txLock = new Object();

        this.txHeader  = ByteBuffer.allocate(3);
        this.txPayload = ByteBuffer.allocate(MAX_PACKET_LENGTH);

        this.txBuffers = new ByteBuffer[2];

        this.txHeartbeat = ByteBuffer.allocate(3);

        this.txHeader.order(ByteOrder.BIG_ENDIAN);
        this.txPayload.order(ByteOrder.BIG_ENDIAN);
        this.txHeartbeat.order(ByteOrder.BIG_ENDIAN);

        this.txBuffers[0] = txHeader;

        putUnsignedShort(this.txHeartbeat, 1);
        this.txHeartbeat.put(heartbeatPacketType);
    }

    /**
     * Get the underlying socket channel.
     *
     * @return the underlying socket channel
     */
    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * Receive data from the underlying socket channel. For each packet
     * received, invoke the corresponding listener if applicable.
     *
     * @return The number of bytes read, possibly zero, or <code>-1</code>
     *   if the channel has reached end-of-stream
     * @throws IOException if an I/O error occurs
     */
    public int receive() throws IOException {
        int bytes = channel.read(rxBuffer);

        if (bytes <= 0)
            return bytes;

        rxBuffer.flip();

        while (parser.parse(rxBuffer));

        rxBuffer.compact();

        receivedData();

        return bytes;
    }

    /**
     * Keep the session alive.
     *
     * <p>If the heartbeat interval duration has passed since the last packet
     * was sent, send a Heartbeat packet. If the heartbeat timeout duration
     * has passed since the last packet was received, invoke the corresponding
     * method on the status listener.</p>
     *
     * @throws IOException if an I/O error occurs
     */
    public void keepAlive() throws IOException {
        long currentTimeMillis = clock.currentTimeMillis();

        if (currentTimeMillis - lastRxMillis > RX_HEARTBEAT_TIMEOUT_MILLIS)
            heartbeatTimeout();
        else if (currentTimeMillis - lastTxMillis > TX_HEARTBEAT_INTERVAL_MILLIS)
            heartbeat();
    }

    /**
     * Close the underlying socket channel.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

    protected void send(byte packetType) throws IOException {
        txPayload.clear();
        txPayload.flip();

        send(packetType, txPayload);
    }

    protected void send(byte packetType, ByteBuffer payload) throws IOException {
        int packetLength = payload.remaining() + 1;

        if (packetLength > MAX_PACKET_LENGTH)
            throw new SoupBinTCPException("Packet length exceeds maximum packet length");

        txHeader.clear();
        putUnsignedShort(txHeader, packetLength);
        txHeader.put(packetType);
        txHeader.flip();

        txBuffers[1] = payload;

        int remaining = txHeader.remaining() + payload.remaining();

        synchronized (txLock) {
            do {
                remaining -= channel.write(txBuffers);
            } while (remaining > 0);
        }

        sentData();
    }

    private void heartbeat() throws IOException {
        txHeartbeat.flip();

        synchronized (txLock) {
            do {
                channel.write(txHeartbeat);
            } while (txHeartbeat.remaining() > 0);
        }

        sentData();
    }

    private void heartbeatTimeout() throws IOException {
        statusListener.heartbeatTimeout();

        receivedData();
    }

    private void receivedData() {
        lastRxMillis = clock.currentTimeMillis();
    }

    private void sentData() {
        lastTxMillis = clock.currentTimeMillis();
    }

}
