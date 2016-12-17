package com.paritytrading.nassau.moldudp64;

import static com.paritytrading.foundation.ByteBuffers.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientState.*;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.time.Clock;
import com.paritytrading.nassau.time.SystemClock;
import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

/**
 * An implementation of a MoldUDP64 client.
 */
public class MoldUDP64Client implements Closeable {

    private static final int RX_BUFFER_LENGTH = 65535;

    private static final long REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN = -1;

    private static final long REQUEST_TIMEOUT_MILLIS = 1000;

    private Clock clock;

    private DatagramChannel channel;

    private DatagramChannel requestChannel;

    private SocketAddress requestAddress;

    private MessageListener listener;

    private MoldUDP64ClientStatusListener statusListener;

    private ByteBuffer rxBuffer;
    private ByteBuffer txBuffer;

    private byte[] session;

    private long nextExpectedSequenceNumber;

    private long requestUntilSequenceNumber;

    private long requestSentMillis;

    private MoldUDP64ClientState state;

    MoldUDP64Client(Clock clock, DatagramChannel channel,
            DatagramChannel requestChannel, SocketAddress requestAddress,
            MessageListener listener, MoldUDP64ClientStatusListener statusListener,
            long requestedSequenceNumber) {
        this.clock = clock;

        this.channel = channel;

        this.requestChannel = requestChannel;

        this.requestAddress = requestAddress;

        this.listener = listener;

        this.statusListener = statusListener;

        this.rxBuffer = ByteBuffer.allocate(RX_BUFFER_LENGTH);
        this.txBuffer = ByteBuffer.allocate(HEADER_LENGTH);

        this.session = new byte[SESSION_LENGTH];

        this.nextExpectedSequenceNumber = Math.max(1, requestedSequenceNumber);

        this.requestUntilSequenceNumber = REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN;

        this.requestSentMillis = 0;

        this.state = UNKNOWN;
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel both for
     * receiving downstream packets and sending request packets. The underlying
     * datagram channel can be either blocking or non-blocking.
     *
     * @param channel the underlying datagram channel
     * @param requestAddress the request address
     * @param listener the message listener
     * @param statusListener the status listener
     */
    public MoldUDP64Client(DatagramChannel channel, SocketAddress requestAddress,
            MessageListener listener, MoldUDP64ClientStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, channel, requestAddress, listener,
                statusListener, 1);
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel both for
     * receiving downstream packets and sending request packets. The underlying
     * datagram channel can be either blocking or non-blocking.
     *
     * @param channel the underlying datagram channel
     * @param requestAddress the request address
     * @param listener the message listener
     * @param statusListener the status listener
     * @param requestedSequenceNumber the requested initial sequence number
     */
    public MoldUDP64Client(DatagramChannel channel, SocketAddress requestAddress,
            MessageListener listener, MoldUDP64ClientStatusListener statusListener,
            long requestedSequenceNumber) {
        this(SystemClock.INSTANCE, channel, channel, requestAddress, listener,
                statusListener, requestedSequenceNumber);
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel for
     * receiving downstream packets and the underlying request datagram
     * channel for sending request packets. Both the underlying datagram
     * channel and the underlying request datagram channel must be
     * non-blocking.
     *
     * @param channel the underlying datagram channel
     * @param requestChannel the underlying request datagram channel
     * @param requestAddress the request address
     * @param listener the message listener
     * @param statusListener the status listener
     */
    public MoldUDP64Client(DatagramChannel channel, DatagramChannel requestChannel,
            SocketAddress requestAddress, MessageListener listener,
            MoldUDP64ClientStatusListener statusListener) {
        this(SystemClock.INSTANCE, channel, requestChannel, requestAddress,
                listener, statusListener, 1);
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel for
     * receiving downstream packets and the underlying request datagram
     * channel for sending request packets. Both the underlying datagram
     * channel and the underlying request datagram channel must be
     * non-blocking.
     *
     * @param channel the underlying datagram channel
     * @param requestChannel the underlying request datagram channel
     * @param requestAddress the request address
     * @param listener the message listener
     * @param statusListener the status listener
     * @param requestedSequenceNumber the requested initial sequence number
     */
    public MoldUDP64Client(DatagramChannel channel, DatagramChannel requestChannel,
            SocketAddress requestAddress, MessageListener listener,
            MoldUDP64ClientStatusListener statusListener,
            long requestedSequenceNumber) {
        this(SystemClock.INSTANCE, channel, requestChannel, requestAddress,
                listener, statusListener, requestedSequenceNumber);
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
     * Get the underlying request datagram channel.
     *
     * @return the underlying request datagram channel
     */
    public DatagramChannel getRequestChannel() {
        return requestChannel;
    }

    /**
     * Receive data from the underlying datagram channel.
     *
     * @throws IOException if an I/O error occurs
     */
    public void receive() throws IOException {
        rxBuffer.clear();

        if (channel.receive(rxBuffer) == null)
            return;

        rxBuffer.flip();

        handle();
    }

    /**
     * Receive data from the underlying request datagram channel.
     *
     * @throws IOException if an I/O error occurs
     */
    public void receiveResponse() throws IOException {
        rxBuffer.clear();

        if (requestChannel.receive(rxBuffer) == null)
            return;

        rxBuffer.flip();

        handle();
    }

    /**
     * Close the underlying datagram channels.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        channel.close();

        if (requestChannel != channel)
            requestChannel.close();
    }

    private void handle() throws IOException {
        if (rxBuffer.remaining() < 20)
            throw truncatedPacket();

        rxBuffer.order(ByteOrder.BIG_ENDIAN);

        rxBuffer.get(session);

        long sequenceNumber = rxBuffer.getLong();
        int  messageCount   = getUnsignedShort(rxBuffer);

        boolean endOfSession = messageCount == MESSAGE_COUNT_END_OF_SESSION;

        if (endOfSession)
            messageCount = 0;

        long nextSequenceNumber = sequenceNumber + messageCount;

        if (sequenceNumber > nextExpectedSequenceNumber) {
            if (requestUntilSequenceNumber == REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN) {
                state(BACKFILL);

                requestUntilSequenceNumber = nextSequenceNumber;

                request(nextExpectedSequenceNumber, requestUntilSequenceNumber);
            } else if (requestUntilSequenceNumber == 0) {
                state(GAP_FILL);

                requestUntilSequenceNumber = nextSequenceNumber;

                request(nextExpectedSequenceNumber, requestUntilSequenceNumber);
            } else {
                requestUntilSequenceNumber = Math.max(requestUntilSequenceNumber, nextSequenceNumber);

                if (clock.currentTimeMillis() - requestSentMillis > REQUEST_TIMEOUT_MILLIS)
                    request(nextExpectedSequenceNumber, requestUntilSequenceNumber);
            }
        } else {
            if (requestUntilSequenceNumber != 0) {
                if (requestUntilSequenceNumber == REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN) {
                    requestUntilSequenceNumber = 0;

                    state(SYNCHRONIZED);
                } else if (requestUntilSequenceNumber == nextSequenceNumber) {
                    requestUntilSequenceNumber = 0;

                    state(SYNCHRONIZED);
                } else {
                    request(nextSequenceNumber, requestUntilSequenceNumber);
                }
            }

            if (endOfSession) {
                statusListener.endOfSession(this);
            } else {
                long skipUntilSequenceNumber = Math.min(nextSequenceNumber, nextExpectedSequenceNumber);

                while (sequenceNumber < skipUntilSequenceNumber) {
                    skip();

                    sequenceNumber++;
                }

                while (nextExpectedSequenceNumber < nextSequenceNumber) {
                    read();

                    nextExpectedSequenceNumber++;
                }
            }

            statusListener.downstream(this);
        }
    }

    private void request(long requestFromSequenceNumber, long requestUntilSequenceNumber) throws IOException {
        int requestedMessageCount = (int)Math.min(requestUntilSequenceNumber - requestFromSequenceNumber,
                MAX_MESSAGE_COUNT);

        txBuffer.clear();
        txBuffer.put(session);
        txBuffer.putLong(requestFromSequenceNumber);
        putUnsignedShort(txBuffer, requestedMessageCount);
        txBuffer.flip();

        while (requestChannel.send(txBuffer, requestAddress) == 0);

        statusListener.request(this, requestFromSequenceNumber, requestedMessageCount);

        requestSentMillis = clock.currentTimeMillis();
    }

    private void state(MoldUDP64ClientState next) throws IOException {
        state = next;

        statusListener.state(this, next);
    }

    private void read() throws IOException {
        int messageLength = readMessageLength();

        int limit = rxBuffer.limit();

        rxBuffer.limit(rxBuffer.position() + messageLength);

        listener.message(rxBuffer);

        rxBuffer.position(rxBuffer.limit());
        rxBuffer.limit(limit);
    }

    private void skip() throws IOException {
        int messageLength = readMessageLength();

        rxBuffer.position(rxBuffer.position() + messageLength);
    }

    private int readMessageLength() throws IOException {
        if (rxBuffer.remaining() < 2)
            throw truncatedPacket();

        rxBuffer.order(ByteOrder.BIG_ENDIAN);

        int messageLength = getUnsignedShort(rxBuffer);

        if (rxBuffer.remaining() < messageLength)
            throw truncatedPacket();

        return messageLength;
    }

    private MoldUDP64Exception truncatedPacket() {
        return new MoldUDP64Exception("Truncated packet");
    }

}
