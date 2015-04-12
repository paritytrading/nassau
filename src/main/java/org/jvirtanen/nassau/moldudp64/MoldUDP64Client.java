package org.jvirtanen.nassau.moldudp64;

import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jvirtanen.nassau.MessageListener;

class MoldUDP64Client {

    public static final int RX_BUFFER_LENGTH = 65535;

    public static void read(ByteBuffer buffer, MessageListener listener) throws IOException {
        int messageLength = readMessageLength(buffer);

        int limit = buffer.limit();

        buffer.limit(buffer.position() + messageLength);

        listener.message(buffer);

        buffer.position(buffer.limit());
        buffer.limit(limit);
    }

    public static void skip(ByteBuffer buffer) throws IOException {
        int messageLength = readMessageLength(buffer);

        buffer.position(buffer.position() + messageLength);
    }

    public static MoldUDP64Exception truncatedPacket() {
        return new MoldUDP64Exception("Truncated packet");
    }

    private static int readMessageLength(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < 2)
            throw truncatedPacket();

        buffer.order(ByteOrder.BIG_ENDIAN);

        int messageLength = getUnsignedShort(buffer);

        if (buffer.remaining() < messageLength)
            throw truncatedPacket();

        return messageLength;
    }

}
