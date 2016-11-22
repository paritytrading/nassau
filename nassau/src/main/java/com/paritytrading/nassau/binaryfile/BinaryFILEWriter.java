package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.foundation.ByteBuffers.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * An implementation of a BinaryFILE writer.
 */
public class BinaryFILEWriter implements Closeable {

    private WritableByteChannel channel;

    private ByteBuffer header;

    /**
     * Create a BinaryFILE writer.
     *
     * @param stream an output stream
     */
    public BinaryFILEWriter(OutputStream stream) {
        this(Channels.newChannel(stream));
    }

    /**
     * Create a BinaryFILE writer.
     *
     * @param channel an output channel
     */
    public BinaryFILEWriter(WritableByteChannel channel) {
        this.channel = channel;

        this.header = ByteBuffer.allocate(2);
    }

    /**
     * Open a BinaryFILE writer.
     *
     * @param file the output file
     * @return a BinaryFILE writer
     * @throws IOException if an I/O error occurs
     */
    public static BinaryFILEWriter open(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);

        return new BinaryFILEWriter(stream.getChannel());
    }

    /**
     * Write a message.
     *
     * @param payload a buffer containing a message
     * @throws IOException if an I/O error occurs
     */
    public void write(ByteBuffer payload) throws IOException {
        header.clear();
        putUnsignedShort(header, payload.remaining());
        header.flip();

        do {
            channel.write(header);
        } while (header.hasRemaining());

        do {
            channel.write(payload);
        } while (payload.hasRemaining());
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

}
