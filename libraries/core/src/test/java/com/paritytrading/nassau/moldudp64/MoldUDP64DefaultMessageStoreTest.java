package com.paritytrading.nassau.moldudp64;

import static com.paritytrading.foundation.ByteBuffers.*;
import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MoldUDP64DefaultMessageStoreTest {

    private MoldUDP64DefaultMessageStore store;

    private ByteBuffer buffer;

    @Before
    public void setUp() {
        store = new MoldUDP64DefaultMessageStore();

        buffer = ByteBuffer.allocateDirect(1024);
    }

    @Test
    public void downstreamPacket() throws Exception {
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

            assertEquals(message.length(), getUnsignedShort(buffer));
            assertEquals(message, get(buffer, message.length()));
        }

        assertFalse(buffer.hasRemaining());
    }

}
