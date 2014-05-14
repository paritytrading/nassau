package org.jvirtanen.nassau;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Messages<M> implements MessageListener {

    private MessageParser<M> parser;

    private List<M> messages;

    public Messages(MessageParser<M> parser) {
        this.parser = parser;

        this.messages = new ArrayList<>();
    }

    public List<M> collect() {
        return messages;
    }

    @Override
    public void message(ByteBuffer buffer) {
        messages.add(parser.parse(buffer));
    }

}
