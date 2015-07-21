package org.jvirtanen.nassau.util;

import static java.nio.charset.StandardCharsets.*;

import java.nio.ByteBuffer;
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

    public static String get(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];

        buffer.get(bytes);

        return new String(bytes, UTF_8);
    }

    public static String remaining(ByteBuffer buffer) {
        return get(buffer, buffer.remaining());
    }

    public static ByteBuffer wrap(String value) {
        return ByteBuffer.wrap(value.getBytes(UTF_8));
    }

    public static String repeat(char c, int num) {
        StringBuilder builder = new StringBuilder(num);

        for (int i = 0; i < num; i++)
            builder.append(c);

        return builder.toString();
    }

}
