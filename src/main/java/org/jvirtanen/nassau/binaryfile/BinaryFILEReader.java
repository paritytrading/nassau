package org.jvirtanen.nassau.binaryfile;

import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;
import org.jvirtanen.nassau.MessageListener;

/**
 * An implementation of a BinaryFILE reader.
 */
public class BinaryFILEReader implements Closeable {

    private ReadableByteChannel channel;

    private MessageListener listener;

    private ByteBuffer buffer;

    /**
     * Create a BinaryFILE reader.
     *
     * @param stream an input stream
     * @param listener a message listener
     */
    public BinaryFILEReader(InputStream stream, MessageListener listener) {
        this(Channels.newChannel(stream), listener);
    }

    /**
     * Create a BinaryFILE reader.
     *
     * @param channel an input channel
     * @param listener a message listener
     */
    public BinaryFILEReader(ReadableByteChannel channel, MessageListener listener) {
        this.channel  = channel;
        this.listener = listener;

        this.buffer = ByteBuffer.allocate(128 * 1024);

        this.buffer.limit(0);
    }

    /**
     * Open a BinaryFILE reader. The input file can be either uncompressed or
     * compressed with the GZIP file format.
     *
     * @param file the input file
     * @param listener the message listener
     * @return a BinaryFILE reader
     * @throws IOException if an I/O error occurs
     */
    public static BinaryFILEReader open(File file, MessageListener listener) throws IOException {
        FileInputStream stream = new FileInputStream(file);

        if (file.getName().endsWith(".gz"))
            return new BinaryFILEReader(new GZIPInputStream(stream), listener);
        else
            return new BinaryFILEReader(stream.getChannel(), listener);
    }

    /**
     * Read a message.
     *
     * @return true if a message was read, otherwise false
     * @throws IOException if an I/O error occurs
     */
    public boolean read() throws IOException {
        if (buffer.remaining() < 2) {
            buffer.compact();

            if (channel.read(buffer) < 0)
                return false;

            buffer.flip();
        }

        buffer.order(ByteOrder.BIG_ENDIAN);

        int payloadLength = getUnsignedShort(buffer);

        if (buffer.remaining() < payloadLength) {
            buffer.compact();

            if (channel.read(buffer) < 0)
                throw new BinaryFILEException("Unexpected end-of-stream");

            buffer.flip();
        }

        int limit = buffer.limit();

        buffer.limit(buffer.position() + payloadLength);

        listener.message(buffer);

        buffer.position(buffer.limit());
        buffer.limit(limit);

        return true;
    }

    /**
     * Close the underlying channel.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        channel.close();
    }

}
