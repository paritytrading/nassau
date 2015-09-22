package org.jvirtanen.nassau.moldudp64;

import static org.jvirtanen.nassau.moldudp64.MoldUDP64.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64Client.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64ClientState.*;
import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import org.jvirtanen.nassau.MessageListener;

/**
 * A multi-channel implementation of a MoldUDP64 client. This implementation
 * uses separate channels for receiving downstream packets and sending
 * request packets.
 *
 * <p>Using two channels is neither as simple nor as efficient as using one
 * channel. However, it results in the source UDP port in request packets
 * bearing no relation to the UDP port of the IP multicast. Therefore multiple
 * instances of this implementation can be listening to the same session on
 * the same host at the same time.</p>
 */
public class MultiChannelMoldUDP64Client implements Closeable {

    /*
     * The following attributes are used for receiving downstream packets.
     */
    private ByteBuffer rxBuffer;

    private DatagramChannel channel;

    private byte[] session;

    /*
     * The following attributes are used for sending request packets and
     * receiving response packets.
     */
    private ByteBuffer requestRxBuffer;
    private ByteBuffer requestTxBuffer;

    private DatagramChannel requestChannel;

    private byte[] requestSession;

    /*
     * Downstream packet reception and response packet reception can run on
     * separate threads.
     */
    private Object lock;

    private MessageListener listener;

    private MoldUDP64ClientStatusListener statusListener;

    private long nextExpectedSequenceNumber;

    private long nextEstimatedSequenceNumber;

    private MoldUDP64ClientState state;

    /**
     * Create a client. The underlying datagram channel can be either blocking
     * or non-blocking. The underlying datagram channel for requests can also
     * be either blocking or non-blocking, but it must be connected.
     *
     * @param channel the underlying datagram channel
     * @param requestChannel the underlying datagram channel for requests
     * @param listener the inbound message listener
     * @param statusListener the inbound status event listener
     */
    public MultiChannelMoldUDP64Client(DatagramChannel channel, DatagramChannel requestChannel,
            MessageListener listener, MoldUDP64ClientStatusListener statusListener) {
        this.rxBuffer = ByteBuffer.allocate(RX_BUFFER_LENGTH);

        this.channel = channel;

        this.session = new byte[SESSION_LENGTH];

        this.requestRxBuffer = ByteBuffer.allocate(RX_BUFFER_LENGTH);
        this.requestTxBuffer = ByteBuffer.allocate(HEADER_LENGTH);

        this.requestChannel = requestChannel;

        this.requestSession = new byte[SESSION_LENGTH];

        this.lock = new Object();

        this.listener = listener;

        this.statusListener = statusListener;

        this.nextExpectedSequenceNumber = 1;

        this.nextEstimatedSequenceNumber = 1;

        this.state = UNKNOWN;
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
     * Get the underlying datagram channel for requests.
     *
     * @return the underlying datagram channel for requests
     */
    public DatagramChannel getRequestChannel() {
        return requestChannel;
    }

    /**
     * Receive a downstream packet.
     *
     * @see SingleChannelMoldUDP64Client#receive
     * @throws IOException if an I/O error occurs
     */
    public void receive() throws IOException {
        rxBuffer.clear();

        if (channel.receive(rxBuffer) == null)
            return;

        rxBuffer.flip();

        receive(rxBuffer, session);
    }

    /**
     * Receive a response packet.
     *
     * @see SingleChannelMoldUDP64Client#receive
     * @throws IOException if an I/O error occurs
     */
    public void receiveResponse() throws IOException {
        requestRxBuffer.clear();

        if (requestChannel.receive(requestRxBuffer) == null)
            return;

        requestRxBuffer.flip();

        receive(requestRxBuffer, requestSession);
    }

    private void receive(ByteBuffer buffer, byte[] session) throws IOException {
        if (buffer.remaining() < 20)
            throw truncatedPacket();

        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.get(session);

        long sequenceNumber = buffer.getLong();
        int  messageCount   = getUnsignedShort(buffer);

        boolean endOfSession = messageCount == MESSAGE_COUNT_END_OF_SESSION;

        if (endOfSession)
            messageCount = 0;

        long nextSequenceNumber = sequenceNumber + messageCount;

        synchronized (lock) {
            nextEstimatedSequenceNumber = Math.max(nextEstimatedSequenceNumber, nextSequenceNumber);

            if (sequenceNumber > nextExpectedSequenceNumber) {
                int requestedMessageCount = (int)Math.min(nextEstimatedSequenceNumber - nextExpectedSequenceNumber,
                        MAX_MESSAGE_COUNT);

                if (state == SYNCHRONIZED)
                    state(GAP_FILL);
                else if (state == UNKNOWN)
                    state(BACKFILL);

                request(requestedMessageCount);
                return;
            }

            if (state != SYNCHRONIZED && nextEstimatedSequenceNumber == nextSequenceNumber)
                state(SYNCHRONIZED);

            if (endOfSession) {
                statusListener.endOfSession();
            } else {
                while (sequenceNumber < nextExpectedSequenceNumber) {
                    skip(buffer);

                    sequenceNumber++;
                }

                while (nextExpectedSequenceNumber < nextSequenceNumber) {
                    read(buffer, listener);

                    nextExpectedSequenceNumber++;
                }
            }

            statusListener.downstream();

            if (nextEstimatedSequenceNumber > nextSequenceNumber) {
                int requestedMessageCount = (int)Math.min(nextEstimatedSequenceNumber - nextExpectedSequenceNumber,
                        MAX_MESSAGE_COUNT);

                request(requestedMessageCount);
            }
        }
    }

    private void state(MoldUDP64ClientState next) throws IOException {
        state = next;

        statusListener.state(next);
    }

    private void request(int requestedMessageCount) throws IOException {
        requestTxBuffer.clear();
        requestTxBuffer.put(session);
        requestTxBuffer.putLong(nextExpectedSequenceNumber);
        putUnsignedShort(requestTxBuffer, requestedMessageCount);
        requestTxBuffer.flip();

        while (requestChannel.write(requestTxBuffer) == 0);

        statusListener.request(nextExpectedSequenceNumber, requestedMessageCount);
    }

    /**
     * Close the underlying datagram channel.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        channel.close();

        requestChannel.close();
    }

}
