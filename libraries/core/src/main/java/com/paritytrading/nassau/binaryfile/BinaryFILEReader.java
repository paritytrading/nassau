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
package com.paritytrading.nassau.binaryfile;

import com.paritytrading.nassau.MessageListener;
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

/**
 * An implementation of a BinaryFILE reader.
 */
public class BinaryFILEReader implements Closeable {

    private static final int BUFFER_SIZE = 128 * 1024;

    private static final int GZIP_BUFFER_SIZE = 64 * 1024;

    private final ReadableByteChannel channel;

    private final MessageListener listener;

    private final ByteBuffer buffer;

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

        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
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
            return new BinaryFILEReader(new GZIPInputStream(stream, GZIP_BUFFER_SIZE), listener);
        else
            return new BinaryFILEReader(stream.getChannel(), listener);
    }

    /**
     * Read messages. Invoke the message listener on each message.
     *
     * @return the number of bytes read, possibly zero, or {@code -1} if the
     *   channel has reached end-of-stream
     * @throws IOException if an I/O error occurs
     */
    public int read() throws IOException {
        int bytes = channel.read(buffer);

        if (bytes <= 0)
            return bytes;

        buffer.flip();

        while (parse());

        buffer.compact();

        return bytes;
    }

    private boolean parse() throws IOException {
        if (buffer.remaining() < 2)
            return false;

        buffer.mark();

        buffer.order(ByteOrder.BIG_ENDIAN);

        int payloadLength = buffer.getShort() & 0xffff;

        if (buffer.remaining() < payloadLength) {
            buffer.reset();

            return false;
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
    @Override
    public void close() throws IOException {
        channel.close();
    }

}
