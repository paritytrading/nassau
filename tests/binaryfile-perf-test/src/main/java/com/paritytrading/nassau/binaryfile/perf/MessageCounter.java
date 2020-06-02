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
package com.paritytrading.nassau.binaryfile.perf;

import com.paritytrading.nassau.MessageListener;
import java.nio.ByteBuffer;

class MessageCounter implements MessageListener {

    private long messageCount;

    @Override
    public void message(ByteBuffer buffer) {
        messageCount++;
    }

    public long getMessageCount() {
        return messageCount;
    }

}
