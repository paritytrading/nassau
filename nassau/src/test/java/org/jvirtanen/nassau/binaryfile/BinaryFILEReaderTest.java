package org.jvirtanen.nassau.binaryfile;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.binaryfile.BinaryFILEStatus.*;

import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvirtanen.nassau.Messages;
import org.jvirtanen.nassau.util.Strings;

public class BinaryFILEReaderTest {

    private InputStream stream;

    private Messages<String> messages;

    @Before
    public void setUp() throws Exception {
        stream = getClass().getResourceAsStream("/binaryfile.dat");

        messages = new Messages<>(Strings.MESSAGE_PARSER);
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }

    @Test
    public void readStream() throws Exception {
        BinaryFILEReader reader = new BinaryFILEReader(stream, messages);

        while (reader.read());

        assertEquals(asList("foo", "bar", "baz", "quux", ""), messages.collect());
    }

    @Test
    public void readStreamWithStatusListener() throws Exception {
        BinaryFILEStatus status = new BinaryFILEStatus();

        BinaryFILEStatusParser parser = new BinaryFILEStatusParser(messages, status);

        BinaryFILEReader reader = new BinaryFILEReader(stream, parser);

        while (reader.read());

        assertEquals(asList("foo", "bar", "baz", "quux"), messages.collect());
        assertEquals(asList(new EndOfSession()), status.collect());
    }

}
