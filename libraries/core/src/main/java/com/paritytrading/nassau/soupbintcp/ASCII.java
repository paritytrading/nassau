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
package com.paritytrading.nassau.soupbintcp;

import static java.nio.charset.StandardCharsets.US_ASCII;

class ASCII {

    /**
     * Get an ASCII string from a byte array.
     *
     * @param bytes a byte array
     * @return a string
     */
    static String get(byte[] bytes) {
        return new String(bytes, US_ASCII);
    }

    /**
     * Get an integer formatted as an ASCII string from a byte array.
     *
     * @param bytes a byte array
     * @return an integer
     */
    static long getLong(byte[] bytes) {
        long sign = +1;

        int i = 0;

        while (bytes[i] == ' ')
            i++;

        if (bytes[i] == '-') {
            sign = -1;

            i++;
        }

        long l = 0;

        while (i < bytes.length && bytes[i] != ' ')
            l = 10 * l + bytes[i++] - '0';

        return sign * l;
    }

    /**
     * Put an ASCII string into a byte array.
     *
     * <p>If the length of the string is smaller than the length of the array,
     * the space character is used to fill the trailing bytes.</p>
     *
     * @param bytes a byte array
     * @param s a string
     * @throws IndexOutOfBoundsException if the length of the string is larger
     *   than the length of the array
     */
    static void putLeft(byte[] bytes, CharSequence s) {
        int i = 0;

        for (; i < s.length(); i++)
            bytes[i] = (byte)s.charAt(i);

        for (; i < bytes.length; i++)
            bytes[i] = (byte)' ';
    }

    /**
     * Put an ASCII string into a byte array.
     *
     * <p>If the length of the string is smaller than the length of the array,
     * the space character is used to fill the leading bytes.</p>
     *
     * @param bytes a byte array
     * @param s a string
     * @throws IndexOutOfBoundsException if the length of the string is larger
     *   than the length of the array
     */
    static void putRight(byte[] bytes, CharSequence s) {
        int i = 0;

        for (; i < bytes.length - s.length(); i++)
            bytes[i] = (byte)' ';

        for (int j = 0; j < s.length(); i++, j++)
            bytes[i] = (byte)s.charAt(j);
    }

    /**
     * Put an integer formatted as an ASCII string into a byte array.
     *
     * <p>If the length of the string is smaller than the length of the array,
     * the space character is used to fill the leading bytes.</p>
     *
     * @param bytes a byte array
     * @param l an integer
     * @throws IndexOutOfBoundsException if the length of the string is larger
     *   than the length of the array
     */
    static void putLongRight(byte[] bytes, long l) {
        long sign = l;

        if (sign < 0)
            l = -l;

        int i = bytes.length - 1;

        do {
            bytes[i--] = (byte)('0' + l % 10);

            l /= 10;
        } while (l > 0);

        if (sign < 0)
            bytes[i--] = '-';

        for (; i >= 0; i--)
            bytes[i] = ' ';
    }

}
