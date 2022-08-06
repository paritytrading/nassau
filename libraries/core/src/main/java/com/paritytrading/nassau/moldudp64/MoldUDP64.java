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
package com.paritytrading.nassau.moldudp64;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;

class MoldUDP64 {

    static final int MESSAGE_COUNT_HEARTBEAT = 0x0;

    static final int MESSAGE_COUNT_END_OF_SESSION = 0xFFFF;

    static final int MAX_MESSAGE_COUNT = MESSAGE_COUNT_END_OF_SESSION - 1;

    static final int HEADER_LENGTH = 20;

    static final int MAX_PAYLOAD_LENGTH = 1400;

    static final int SESSION_LENGTH = 10;

    private static final byte SPACE = ' ';

    static byte[] session(String name) {
        byte[] session = new byte[SESSION_LENGTH];
        fill(session, SPACE);

        byte[] bytes = name.getBytes(US_ASCII);
        System.arraycopy(bytes, 0, session, 0, Math.min(bytes.length, session.length));

        return session;
    }

}
