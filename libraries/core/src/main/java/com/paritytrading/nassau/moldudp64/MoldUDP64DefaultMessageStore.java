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

import com.paritytrading.nassau.MessageListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of a MoldUDP64 message store.
 */
public class MoldUDP64DefaultMessageStore implements MoldUDP64MessageStore {

    private final List<byte[]> messages;

    private final MessageListener listener;

    /**
     * Create a message store.
     */
    public MoldUDP64DefaultMessageStore() {
        this.messages = new ArrayList<>();
        this.listener = new MessageListener() {

            @Override
            public void message(ByteBuffer buffer) {
                put(buffer);
            }

        };
    }

    /**
     * Store a message.
     *
     * @param buffer a buffer containing a message
     */
    public void put(ByteBuffer buffer) {
        int length = buffer.remaining();

        byte[] message = new byte[length];

        buffer.get(message);

        messages.add(message);
    }

    /**
     * Store all messages in a downstream packet.
     *
     * @param packet a downstream packet
     */
    public void put(MoldUDP64DownstreamPacket packet) {
        try {
            packet.apply(listener);
        } catch (IOException e) {
            // The listener does not throw I/O exceptions.
        }
    }

    @Override
    public int get(ByteBuffer buffer, long sequenceNumber, int requestedMessageCount) {
        if (sequenceNumber > Integer.MAX_VALUE)
            return 0;

        int messageCount = 0;

        for (int i = (int)sequenceNumber; i < sequenceNumber + requestedMessageCount; i++) {
            if (i > messages.size())
                break;

            byte[] message = messages.get(i - 1);

            if (buffer.remaining() < 2 + message.length)
                break;

            putUnsignedShort(buffer, message.length);
            buffer.put(message);

            messageCount++;
        }

        return messageCount;
    }

}
