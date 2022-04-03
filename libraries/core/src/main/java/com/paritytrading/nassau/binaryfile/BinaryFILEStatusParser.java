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
package com.paritytrading.nassau.binaryfile;

import com.paritytrading.nassau.MessageListener;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A parser for status events.
 */
public class BinaryFILEStatusParser implements MessageListener {

    private final MessageListener listener;

    private final BinaryFILEStatusListener statusListener;

    /**
     * Create a parser for status events. The parser passes payloads that do
     * not correspond to status events to the message listener.
     *
     * @param listener a message listener
     * @param statusListener a status listener
     */
    public BinaryFILEStatusParser(MessageListener listener, BinaryFILEStatusListener statusListener) {
        this.listener = listener;

        this.statusListener = statusListener;
    }

    @Override
    public void message(ByteBuffer buffer) throws IOException {
        if (buffer.hasRemaining())
            listener.message(buffer);
        else
            statusListener.endOfSession();
    }

}
