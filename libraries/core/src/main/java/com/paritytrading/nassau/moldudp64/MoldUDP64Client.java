/*
 * Copyright 2014 Nassau authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paritytrading.nassau.moldudp64;

import static com.paritytrading.foundation.ByteBuffers.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientState.*;

import com.paritytrading.nassau.Clock;
import com.paritytrading.nassau.MessageListener;
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

    private final Clock clock;

    private final DatagramChannel channel;

    private final DatagramChannel requestChannel;

    private final SocketAddress requestAddress;

    private final MessageListener listener;

    private final MoldUDP64ClientStatusListener statusListener;

    private final ByteBuffer rxBuffer;
    private final ByteBuffer txBuffer;

    private final byte[] session;

    private long nextExpectedSequenceNumber;

    private long requestUntilSequenceNumber;

    private long requestSentMillis;

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

        this.rxBuffer = ByteBuffer.allocateDirect(RX_BUFFER_LENGTH);
        this.txBuffer = ByteBuffer.allocateDirect(HEADER_LENGTH);

        this.session = new byte[SESSION_LENGTH];

        this.nextExpectedSequenceNumber = Math.max(0, requestedSequenceNumber);

        this.requestUntilSequenceNumber = REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN;

        this.requestSentMillis = 0;
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
        this(System::currentTimeMillis, channel, channel, requestAddress, listener,
                statusListener, 1);
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel both for
     * receiving downstream packets and sending request packets. The underlying
     * datagram channel can be either blocking or non-blocking.
     *
     * <p>Set the requested initial sequence number to 0 to start from the
     * first received message.</p>
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
        this(System::currentTimeMillis, channel, channel, requestAddress, listener,
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
        this(System::currentTimeMillis, channel, requestChannel, requestAddress,
                listener, statusListener, 1);
    }

    /**
     * Create a MoldUDP64 client. Use the underlying datagram channel for
     * receiving downstream packets and the underlying request datagram
     * channel for sending request packets. Both the underlying datagram
     * channel and the underlying request datagram channel must be
     * non-blocking.
     *
     * <p>Set the requested initial sequence number to 0 to start from the
     * first received message.</p>
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
        this(System::currentTimeMillis, channel, requestChannel, requestAddress,
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
     * @return true if data was received, otherwise false
     * @throws IOException if an I/O error occurs
     */
    public boolean receive() throws IOException {
        rxBuffer.clear();

        if (channel.receive(rxBuffer) == null)
            return false;

        rxBuffer.flip();

        handle();

        return true;
    }

    /**
     * Receive data from the underlying request datagram channel.
     *
     * @return true if data was received, otherwise false
     * @throws IOException if an I/O error occurs
     */
    public boolean receiveResponse() throws IOException {
        rxBuffer.clear();

        if (requestChannel.receive(rxBuffer) == null)
            return false;

        rxBuffer.flip();

        handle();

        return true;
    }

    /**
     * Set the next expected sequence number.
     *
     * @param nextExpectedSequenceNumber the next expected sequence number
     */
    public void setNextExpectedSequenceNumber(long nextExpectedSequenceNumber) {
        this.nextExpectedSequenceNumber = nextExpectedSequenceNumber;
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
            truncatedPacket();

        rxBuffer.order(ByteOrder.BIG_ENDIAN);

        rxBuffer.get(session);

        long sequenceNumber = rxBuffer.getLong();
        int  messageCount   = getUnsignedShort(rxBuffer);

        boolean endOfSession = messageCount == MESSAGE_COUNT_END_OF_SESSION;

        if (endOfSession)
            messageCount = 0;

        long nextSequenceNumber = sequenceNumber + messageCount;

        if (nextExpectedSequenceNumber == 0)
            nextExpectedSequenceNumber = sequenceNumber;

        if (sequenceNumber > nextExpectedSequenceNumber) {
            if (requestUntilSequenceNumber == REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN) {
                statusListener.state(this, BACKFILL);

                requestUntilSequenceNumber = nextSequenceNumber;

                request(nextExpectedSequenceNumber);
            } else if (requestUntilSequenceNumber == 0) {
                statusListener.state(this, GAP_FILL);

                requestUntilSequenceNumber = nextSequenceNumber;

                request(nextExpectedSequenceNumber);
            } else {
                requestUntilSequenceNumber = Math.max(requestUntilSequenceNumber, nextSequenceNumber);

                if (clock.currentTimeMillis() - requestSentMillis > REQUEST_TIMEOUT_MILLIS)
                    request(nextExpectedSequenceNumber);
            }
        } else {
            if (requestUntilSequenceNumber != 0) {
                if (requestUntilSequenceNumber == REQUEST_UNTIL_SEQUENCE_NUMBER_UNKNOWN) {
                    requestUntilSequenceNumber = 0;

                    statusListener.state(this, SYNCHRONIZED);
                } else if (requestUntilSequenceNumber == nextSequenceNumber) {
                    requestUntilSequenceNumber = 0;

                    statusListener.state(this, SYNCHRONIZED);
                } else {
                    request(nextSequenceNumber);
                }
            }

            if (endOfSession) {
                statusListener.endOfSession(this);
            } else {
                long skipUntilSequenceNumber = Math.min(nextSequenceNumber, nextExpectedSequenceNumber);

                for (long s = sequenceNumber; s < skipUntilSequenceNumber; s++)
                    skip();

                for (; nextExpectedSequenceNumber < nextSequenceNumber; nextExpectedSequenceNumber++)
                    read();
            }

            statusListener.downstream(this, sequenceNumber, messageCount);
        }
    }

    private void request(long requestFromSequenceNumber) throws IOException {
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

    private void read() throws IOException {
        int messageLength = readMessageLength();

        if (rxBuffer.remaining() < messageLength)
            truncatedPacket();

        int limit = rxBuffer.limit();

        rxBuffer.limit(rxBuffer.position() + messageLength);

        listener.message(rxBuffer);

        rxBuffer.position(rxBuffer.limit());
        rxBuffer.limit(limit);
    }

    private void skip() throws IOException {
        int messageLength = readMessageLength();

        if (rxBuffer.remaining() < messageLength)
            truncatedPacket();

        rxBuffer.position(rxBuffer.position() + messageLength);
    }

    private int readMessageLength() throws IOException {
        if (rxBuffer.remaining() < 2)
            truncatedPacket();

        rxBuffer.order(ByteOrder.BIG_ENDIAN);

        return getUnsignedShort(rxBuffer);
    }

    private void truncatedPacket() throws MoldUDP64Exception {
        throw new MoldUDP64Exception("Truncated packet");
    }

}
