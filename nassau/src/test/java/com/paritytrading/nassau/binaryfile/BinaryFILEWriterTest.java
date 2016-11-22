package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        byte[] expectedBytes = Files.readAllBytes(Paths.get(getClass().getResource("/binaryfile.dat").toURI()));

        assertArrayEquals(expectedBytes, writtenBytes);
    }

}
