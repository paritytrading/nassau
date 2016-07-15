package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.nassau.binaryfile.BinaryFILEStatus.*;
import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;

public class BinaryFILEWriterTest {

    @Test
    public void write() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        BinaryFILEWriter writer = new BinaryFILEWriter(stream);

        List<String> messages = asList("foo", "bar", "baz", "quux", "");

        for (String message : messages)
            writer.write(wrap(message));

        byte[] writtenBytes  = stream.toByteArray();
        byte[] expectedBytes = toByteArray(getClass().getResourceAsStream("/binaryfile.dat"));

        assertArrayEquals(expectedBytes, writtenBytes);
    }

    private byte[] toByteArray(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int b;

        while ((b = input.read()) != -1)
            output.write(b);

        return output.toByteArray();
    }

}
