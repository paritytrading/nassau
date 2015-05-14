package org.jvirtanen.nassau.binaryfile.perf;

import java.nio.ByteBuffer;
import org.jvirtanen.nassau.MessageListener;

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
