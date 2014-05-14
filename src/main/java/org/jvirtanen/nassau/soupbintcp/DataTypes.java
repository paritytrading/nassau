package org.jvirtanen.nassau.soupbintcp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DataTypes {

    private static final byte SPACE = ' ';

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    static String getAlphanumeric(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];

        buffer.get(bytes);

        return new String(bytes, US_ASCII);
    }

    static long getNumeric(ByteBuffer buffer, int length) {
        return Long.parseLong(getAlphanumeric(buffer, length).trim());
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
