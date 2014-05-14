package org.jvirtanen.nassau.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.jvirtanen.nassau.MessageParser;

public class Strings {

    private Strings() {
    }

    public static final MessageParser<String> MESSAGE_PARSER = new MessageParser<String>() {

        @Override
        public String parse(ByteBuffer buffer) {
            return remaining(buffer);
        }

    };

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String remaining(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];

        buffer.get(bytes);

        return new String(bytes, UTF_8);
    }

    public static ByteBuffer wrap(String value) {
        return ByteBuffer.wrap(value.getBytes(UTF_8));
    }

}
