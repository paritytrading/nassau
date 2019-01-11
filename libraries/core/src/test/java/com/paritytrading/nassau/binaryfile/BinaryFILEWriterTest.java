package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

public class BinaryFILEWriterTest {

    @Test
    public void write() throws Exception {
        File file = File.createTempFile("binaryfile", ".dat");

        try (BinaryFILEWriter writer = BinaryFILEWriter.open(file)) {
            List<String> messages = asList("foo", "bar", "baz", "quux", "");

            for (String message : messages)
                writer.write(wrap(message));
        }

        byte[] writtenBytes  = Files.readAllBytes(file.toPath());
        byte[] expectedBytes = Files.readAllBytes(Paths.get(getClass().getResource("/binaryfile.dat").toURI()));

        assertArrayEquals(expectedBytes, writtenBytes);
    }

}
