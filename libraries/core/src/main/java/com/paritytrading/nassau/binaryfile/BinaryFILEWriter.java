package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.foundation.ByteBuffers.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * An implementation of a BinaryFILE writer.
 */
public class BinaryFILEWriter implements Closeable {

    private static final long DEFAULT_SIZE = 128 * 1024 * 1024;

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0);

    private RandomAccessFile file;

    private FileChannel channel;

    private long size;

    private long position;

    private ByteBuffer buffer;

    private BinaryFILEWriter(RandomAccessFile file, long size) {
        this.file = file;

        this.channel = file.getChannel();

        this.size = size;

        this.position = 0;

        this.buffer = EMPTY_BUFFER;
    }

    /**
     * Open a BinaryFILE writer.
     *
     * @param file the output file
     * @return a BinaryFILE writer
     * @throws IOException if an I/O error occurs
     */
    public static BinaryFILEWriter open(File file) throws IOException {
        return open(file, DEFAULT_SIZE);
    }

    /**
     * Open a BinaryFILE writer.
     *
     * @param file the output file
     * @param size the size of the memory-mapped region
     * @return a BinaryFILE writer
     * @throws IOException if an I/O error occurs
     */
    public static BinaryFILEWriter open(File file, long size) throws IOException {
        return new BinaryFILEWriter(new RandomAccessFile(file, "rw"), size);
    }

    /**
     * Write a message.
     *
     * @param payload a buffer containing a message
     * @throws IOException if an I/O error occurs
     */
    public void write(ByteBuffer payload) throws IOException {
        if (buffer.remaining() < 2 + payload.remaining())
            map();

        putUnsignedShort(buffer, payload.remaining());
        buffer.put(payload);
    }

    @Override
    public void close() throws IOException {
        file.setLength(position + buffer.position());

        file.close();
    }

    private void map() throws IOException {
        position += buffer.position();

        buffer = channel.map(FileChannel.MapMode.READ_WRITE, position, size);
    }

}
