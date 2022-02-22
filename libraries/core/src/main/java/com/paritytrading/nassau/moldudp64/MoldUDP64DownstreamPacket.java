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

import com.paritytrading.nassau.MessageListener;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An implementation of a MoldUDP64 downstream packet.
 */
public class MoldUDP64DownstreamPacket {

    private int messageCount;

    private final ByteBuffer payload;

    /**
     * Create a downstream packet.
     */
    public MoldUDP64DownstreamPacket() {
        this.messageCount = 0;

        this.payload = ByteBuffer.allocateDirect(MAX_PAYLOAD_LENGTH);
    }

    /**
     * Append a message to this downstream packet.
     *
     * @param buffer a buffer containing a message
     * @throws MoldUDP64Exception if the message is too large
     */
    public void put(ByteBuffer buffer) throws MoldUDP64Exception {
        if (remaining() < buffer.remaining())
            throw new MoldUDP64Exception("Buffer overflow");

        putUnsignedShort(payload, buffer.remaining());
        payload.put(buffer);

        messageCount++;
    }

    /**
     * Apply the message listener to each message in this downstream packet.
     *
     * @param listener a message listener
     * @throws IOException if an I/O error occurs in the message listener
     */
    public void apply(MessageListener listener) throws IOException {
        while (true) {
            if (payload.remaining() < 2)
                break;

            int messageLength = getUnsignedShort(payload);

            int limit = payload.limit();

            payload.limit(payload.position() + messageLength);

            listener.message(payload);

            payload.position(payload.limit());
            payload.limit(limit);
        }
    }

    /**
     * Clear this downstream packet. The message count is set to zero and the
     * payload is discarded.
     */
    public void clear() {
        messageCount = 0;

        payload.clear();
    }

    /**
     * Get the message count.
     *
     * @return the message count
     */
    public int messageCount() {
        return messageCount;
    }

    /**
     * Get the payload.
     *
     * @return the payload
     */
    public ByteBuffer payload() {
        return payload;
    }

    /**
     * Get the number of bytes remaining.
     *
     * @return the number of bytes remaining
     */
    public int remaining() {
        return Math.max(0, payload.remaining() - 2);
    }

}
