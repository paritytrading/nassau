package org.jvirtanen.nassau.soupbintcp;

import static java.nio.charset.StandardCharsets.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DataTypes {

    private static final byte SPACE = ' ';

    static String getAlphanumeric(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];

        buffer.get(bytes);

        return new String(bytes, US_ASCII);
    }

    static long getNumeric(ByteBuffer buffer, int length) throws IOException {
        String alphanumeric = getAlphanumeric(buffer, length).trim();

        try {
            long value = Long.parseLong(alphanumeric);
            if (value < 0)
                throw new SoupBinTCPException("Negative numeric: " + alphanumeric);

            return value;
        } catch (NumberFormatException e) {
            throw new SoupBinTCPException("Malformed numeric: " + alphanumeric);
        }
    }

    static void putAlphanumericPadLeft(ByteBuffer buffer, String value, int length) {
        byte[] bytes = value.getBytes(US_ASCII);

        int i;

        for (i = 0; i < length - bytes.length; i++)
            buffer.put(SPACE);

        for (i = 0; i < bytes.length - length; i++);

        while (i < bytes.length)
            buffer.put(bytes[i++]);
    }

    static void putAlphanumericPadRight(ByteBuffer buffer, String value, int length) {
        byte[] bytes = value.getBytes(US_ASCII);

        int i = 0;

        for (; i < Math.min(bytes.length, length); i++)
            buffer.put(bytes[i]);

        for (; i < length; i++)
            buffer.put(SPACE);
    }

    static void putNumeric(ByteBuffer buffer, long value, int length) {
        putAlphanumericPadLeft(buffer, Long.toString(value), length);
    }

}
