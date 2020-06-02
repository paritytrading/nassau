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

import java.io.IOException;

/**
 * The interface for inbound status events on a MoldUDP64 client.
 */
public interface MoldUDP64ClientStatusListener {

    /**
     * Indicates that the client state changed.
     *
     * @param session the session
     * @param next the new client state
     * @throws IOException if an I/O error occurs
     */
    void state(MoldUDP64Client session, MoldUDP64ClientState next) throws IOException;

    /**
     * Indicates that a downstream packet was processed successfully.
     *
     * @param session the session
     * @param sequenceNumber the sequence number
     * @param messageCount the message count
     * @throws IOException if an I/O error occurs
     */
    void downstream(MoldUDP64Client session, long sequenceNumber,
            int messageCount) throws IOException;

    /**
     * Indicates that a request packet was sent.
     *
     * @param session the session
     * @param sequenceNumber the sequence number
     * @param requestedMessageCount the requested message count
     * @throws IOException if an I/O error occurs
     */
    void request(MoldUDP64Client session, long sequenceNumber,
            int requestedMessageCount) throws IOException;

    /**
     * Indicates that a downstream packet indicating the End of Session was
     * received.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void endOfSession(MoldUDP64Client session) throws IOException;

}
