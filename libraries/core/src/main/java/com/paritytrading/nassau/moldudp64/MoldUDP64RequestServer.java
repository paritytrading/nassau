package com.paritytrading.nassau.moldudp64;

import static com.paritytrading.foundation.ByteBuffers.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * An implementation of a MoldUDP64 request server.
 */
public class MoldUDP64RequestServer implements Closeable {

    private static final int MESSAGE_COUNT_OFFSET = 18;

    private final DatagramChannel channel;

    private final ByteBuffer rxBuffer;
    private final ByteBuffer txBuffer;

    private final byte[] session;

    /**
     * Create a request server. The underlying datagram channel must not be
     * connected, but it can be either blocking or non-blocking.
     *
     * @param channel the underlying datagram channel
     */
    public MoldUDP64RequestServer(DatagramChannel channel) {
        this.channel = channel;

        this.rxBuffer = ByteBuffer.allocateDirect(HEADER_LENGTH);
        this.txBuffer = ByteBuffer.allocateDirect(HEADER_LENGTH + MAX_PAYLOAD_LENGTH);

        this.session = new byte[SESSION_LENGTH];
    }

    /**
     * Get the underlying datagram channel.
     *
     * @return the underlying datagram channel
     */
    public DatagramChannel getChannel() {
        return channel;
    }

    /**
     * Serve a request.
     *
     * <p>Retrieve messages from the message store based on the sequence
     * number and the requested message count in the request packet.</p>
     *
     * @param store a message store
     * @throws IOException if an I/O error occurs
     */
    public void serve(MoldUDP64MessageStore store) throws IOException {
        rxBuffer.clear();

        SocketAddress address = channel.receive(rxBuffer);
        if (address == null)
            return;

        rxBuffer.flip();

        if (rxBuffer.remaining() < HEADER_LENGTH)
            return;

        rxBuffer.get(session);

        long sequenceNumber = rxBuffer.getLong();
        if (sequenceNumber < 1)
            return;

        int requestedMessageCount = getUnsignedShort(rxBuffer);

        txBuffer.clear();

        txBuffer.put(session);
        txBuffer.putLong(sequenceNumber);
        putUnsignedShort(txBuffer, 0);

        int messageCount = store.get(txBuffer, sequenceNumber, requestedMessageCount);
        if (messageCount == 0)
            return;

        putUnsignedShort(txBuffer, MESSAGE_COUNT_OFFSET, messageCount);

        txBuffer.flip();

        while (channel.send(txBuffer, address) == 0);
    }

    /**
     * Close the underlying datagram channel.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

}
