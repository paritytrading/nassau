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

    /**
     * The minimum block size is 4 KiB.
     */
    public static final int MIN_BLOCK_SIZE = 4 * 1024;

    /**
     * The default block size is 64 MiB.
     */
    public static final int DEFAULT_BLOCK_SIZE = 64 * 1024 * 1024;

    private int blockSize;

    private Block block;

    private List<Message> messages;

    private MessageListener listener;

    /**
     * Create a message store using the default block size.
     */
    public MoldUDP64DefaultMessageStore() {
        this(DEFAULT_BLOCK_SIZE);
    }

    /**
     * Create a message store using a custom block size.
     *
     * @param blockSize the block size
     */
    public MoldUDP64DefaultMessageStore(int blockSize) {
        this.blockSize = Math.max(blockSize, MIN_BLOCK_SIZE);

        this.block    = new Block(this.blockSize);
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

        if (block.remaining() < length)
            block = new Block(blockSize);

        int offset = block.position();

        block.put(buffer);

        messages.add(new Message(block, offset, length));
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

            Message message = messages.get(i - 1);

            if (buffer.remaining() < 2 + message.length)
                break;

            putUnsignedShort(buffer, message.length);
            message.block.get(buffer, message.offset, message.length);

            messageCount++;
        }

        return messageCount;
    }

    private static class Message {
        Block block;
        int   offset;
        int   length;

        Message(Block block, int offset, int length) {
            this.block  = block;
            this.offset = offset;
            this.length = length;
        }
    }

    private static class Block {
        byte[] bytes;
        int    position;

        Block(int blockSize) {
            this.bytes    = new byte[blockSize];
            this.position = 0;
        }

        void put(ByteBuffer buffer) {
            int length = buffer.remaining();

            buffer.get(bytes, position, length);

            position += length;
        }

        void get(ByteBuffer buffer, int offset, int length) {
            buffer.put(bytes, offset, length);
        }

        int position() {
            return position;
        }

        int remaining() {
            return bytes.length - position;
        }
    }

}
