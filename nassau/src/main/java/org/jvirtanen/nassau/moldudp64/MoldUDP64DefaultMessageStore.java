package org.jvirtanen.nassau.moldudp64;

import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jvirtanen.nassau.MessageListener;

/**
 * The default implementation of a MoldUDP64 message store.
 */
public class MoldUDP64DefaultMessageStore implements MoldUDP64MessageStore {

    private List<byte[]> messages;

    private MessageListener listener;

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
