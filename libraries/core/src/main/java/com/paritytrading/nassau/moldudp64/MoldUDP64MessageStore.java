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

import java.nio.ByteBuffer;

/**
 * The interface for a MoldUDP64 message store. A message store is used by a
 * request server to fulfill incoming requests.
 */
public interface MoldUDP64MessageStore {

    /**
     * Retrieve zero or more messages. Start with the specified sequence number
     * and a message count of zero. Repeat the following algorithm until it
     * terminates:
     *
     * <ol>
     * <li>Check the current message count. If it is equal to the requested
     * message count, terminate the algorithm.</li>
     * <li>Find the message with the current sequence number. If the message
     * is not found, terminate the algorithm.</li>
     * <li>Check the buffer. If the buffer has fewer bytes remaining than are
     * required by a two-byte message header and the message, terminate the
     * algorithm.</li>
     * <li>Put a message header into the buffer. The message header consists
     * of one field: the message length encoded as an unsigned 16-bit big
     * endian integer.</li>
     * <li>Put the message to the buffer.</li>
     * <li>Increment the message count.</li>
     * <li>Increment the sequence number.</li>
     * </ol>
     *
     * <p>Return the message count.</p>
     * 
     * @param buffer a buffer
     * @param sequenceNumber the sequence number
     * @param requestedMessageCount the requested message count
     * @return the message count
     */
    int get(ByteBuffer buffer, long sequenceNumber, int requestedMessageCount);

}
