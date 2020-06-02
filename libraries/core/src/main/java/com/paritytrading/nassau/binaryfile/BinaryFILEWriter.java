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

    private final RandomAccessFile file;

    private final FileChannel channel;

    private final long size;

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

        buffer.putShort((short)payload.remaining());
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
