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

import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoldUDP64DefaultMessageStoreTest {

    private MoldUDP64DefaultMessageStore store;

    private ByteBuffer buffer;

    @BeforeEach
    void setUp() {
        store = new MoldUDP64DefaultMessageStore();

        buffer = ByteBuffer.allocateDirect(1024);
    }

    @Test
    void downstreamPacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        MoldUDP64DownstreamPacket packet = new MoldUDP64DownstreamPacket();

        for (String message : messages)
            packet.put(wrap(message));

        packet.payload().flip();

        store.put(packet);

        int messageCount = store.get(buffer, 1, messages.size());
        assertEquals(messageCount, messages.size());

        buffer.flip();

        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);

            assertEquals(message.length(), buffer.getShort() & 0xffff);
            assertEquals(message, get(buffer, message.length()));
        }

        assertFalse(buffer.hasRemaining());
    }

}
