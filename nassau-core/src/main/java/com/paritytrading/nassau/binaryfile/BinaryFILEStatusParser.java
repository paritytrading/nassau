package com.paritytrading.nassau.binaryfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import com.paritytrading.nassau.MessageListener;

/**
 * A parser for status events.
 */
public class BinaryFILEStatusParser implements MessageListener {

    private MessageListener listener;

    private BinaryFILEStatusListener statusListener;

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
