package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class PacketParser {

    private SoupBinTCPSession session;

    PacketParser(SoupBinTCPSession session) {
        this.session = session;
    }

    boolean parse(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < 2)
            return false;

        buffer.mark();

        buffer.order(ByteOrder.BIG_ENDIAN);

        int packetLength = getUnsignedShort(buffer);
        if (packetLength > buffer.capacity() - 2)
            throw new SoupBinTCPException("Packet length exceeds buffer capacity");

        if (buffer.remaining() < packetLength) {
            buffer.reset();
            return false;
        }

        byte packetType = buffer.get();

        int limit = buffer.limit();

        buffer.limit(buffer.position() + packetLength - 1);

        session.packet(packetType, buffer);

        buffer.position(buffer.limit());
        buffer.limit(limit);

        return true;
    }

}
