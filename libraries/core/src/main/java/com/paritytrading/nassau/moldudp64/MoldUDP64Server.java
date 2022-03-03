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

import com.paritytrading.nassau.Clock;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * An implementation of a MoldUDP64 server.
 */
public class MoldUDP64Server implements Closeable {

    private static final long HEARTBEAT_INTERVAL_MILLIS = 1000;

    private final Clock clock;

    private final DatagramChannel channel;

    private final ByteBuffer[] txBuffers;

    private final byte[] session;

    private long nextSequenceNumber;

    private long lastHeartbeatMillis;

    /**
     * Create a server. The underlying datagram channel must be connected, but
     * it can be either blocking or non-blocking.
     *
     * @param channel the underlying datagram channel
     * @param session the session name
     */
    public MoldUDP64Server(DatagramChannel channel, String session) {
        this(System::currentTimeMillis, channel, session);
    }

    /**
     * Create a server. The underlying datagram channel must be connected, but
     * it can be either blocking or non-blocking.
     *
     * @param clock a clock
     * @param channel the underlying datagram channel
     * @param session the session name
     */
    public MoldUDP64Server(Clock clock, DatagramChannel channel, String session) {
        this(clock, channel, MoldUDP64.session(session));
    }

    private MoldUDP64Server(Clock clock, DatagramChannel channel, byte[] session) {
        this.clock     = clock;
        this.channel   = channel;
        this.txBuffers = new ByteBuffer[2];
        this.session   = session;

        this.nextSequenceNumber = 1;

        this.lastHeartbeatMillis = clock.currentTimeMillis();

        txBuffers[0] = ByteBuffer.allocateDirect(HEADER_LENGTH);
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
     * Send a downstream packet.
     *
     * @param packet a downstream packet
     * @throws IOException if an I/O error occurs
     */
    public void send(MoldUDP64DownstreamPacket packet) throws IOException {
        txBuffers[0].clear();
        txBuffers[0].put(session);
        txBuffers[0].putLong(nextSequenceNumber);
        putUnsignedShort(txBuffers[0], packet.messageCount());
        txBuffers[0].flip();

        txBuffers[1] = packet.payload();
        txBuffers[1].flip();

        while (channel.write(txBuffers) == 0);

        nextSequenceNumber += packet.messageCount();
    }

    /**
     * Send a downstream packet indicating a Heartbeat.
     *
     * @throws IOException if an I/O error occurs
     */
    public void sendHeartbeat() throws IOException {
        send(MESSAGE_COUNT_HEARTBEAT);
    }

    /**
     * Send a downstream packet indicating the End of Session.
     *
     * @throws IOException if an I/O error occurs
     */
    public void sendEndOfSession() throws IOException {
        send(MESSAGE_COUNT_END_OF_SESSION);
    }

    private void send(int messageCount) throws IOException {
        txBuffers[0].clear();
        txBuffers[0].put(session);
        txBuffers[0].putLong(nextSequenceNumber);
        putUnsignedShort(txBuffers[0], messageCount);
        txBuffers[0].flip();

        while (channel.write(txBuffers[0]) == 0);
    }

    /**
     * Keep the session alive.
     *
     * <p>If the heartbeat interval duration has passed since the last
     * downstream packet indicating a Heartbeat was sent, send a downstream
     * packet indicating a Heartbeat.</p>
     *
     * @throws IOException if an I/O error occurs
     */
    public void keepAlive() throws IOException {
        long currentTimeMillis = clock.currentTimeMillis();

        if (currentTimeMillis - lastHeartbeatMillis >= HEARTBEAT_INTERVAL_MILLIS) {
            sendHeartbeat();

            lastHeartbeatMillis = currentTimeMillis;
        }
    }

    /**
     * Set the next sequence number.
     *
     * @param nextSequenceNumber the next sequence number
     */
    public void setNextSequenceNumber(int nextSequenceNumber) {
        this.nextSequenceNumber = nextSequenceNumber;
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
