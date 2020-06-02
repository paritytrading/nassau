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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The interface for inbound messages.
 */
public interface MessageListener {

    /**
     * Receive a message. The message is contained in the buffer between the
     * current position and the limit.
     *
     * @param buffer a buffer
     * @throws IOException if an I/O error occurs
     */
    void message(ByteBuffer buffer) throws IOException;

}
