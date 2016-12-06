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
