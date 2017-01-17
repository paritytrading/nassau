package com.paritytrading.nassau.moldudp64;

import java.util.ArrayList;
import java.util.List;
import org.jvirtanen.value.Value;

class MoldUDP64ClientStatus implements MoldUDP64ClientStatusListener {

    private List<Event> events;

    public MoldUDP64ClientStatus() {
        this.events = new ArrayList<>();
    }

    public List<Event> collect() {
        return events;
    }

    @Override
    public void state(MoldUDP64Client session, MoldUDP64ClientState next) {
        events.add(new State(next));
    }

    @Override
    public void downstream(MoldUDP64Client session, long sequenceNumber, int messageCount) {
        events.add(new Downstream(sequenceNumber, messageCount));
    }

    @Override
    public void request(MoldUDP64Client session, long sequenceNumber, int requestedMessageCount) {
        events.add(new Request(sequenceNumber, requestedMessageCount));
    }

    @Override
    public void endOfSession(MoldUDP64Client session) {
        events.add(new EndOfSession());
    }

    public interface Event {
    }

    public static class State extends Value implements Event {
        public final MoldUDP64ClientState next;

        public State(MoldUDP64ClientState next) {
            this.next = next;
        }
    }

    public static class Downstream extends Value implements Event {
        public final long sequenceNumber;
        public final int  messageCount;

        public Downstream(long sequenceNumber, int messageCount) {
            this.sequenceNumber = sequenceNumber;
            this.messageCount   = messageCount;
        }
    }

    public static class Request extends Value implements Event {
        public final long sequenceNumber;
        public final int  requestedMessageCount;

        public Request(long sequenceNumber, int requestedMessageCount) {
            this.sequenceNumber        = sequenceNumber;
            this.requestedMessageCount = requestedMessageCount;
        }
    }

    public static class EndOfSession extends Value implements Event {
    }

}
