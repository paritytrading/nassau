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
package com.paritytrading.nassau;

import static java.nio.charset.StandardCharsets.*;

import java.nio.ByteBuffer;

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
