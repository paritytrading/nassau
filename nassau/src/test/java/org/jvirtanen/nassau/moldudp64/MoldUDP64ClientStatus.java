package org.jvirtanen.nassau.moldudp64;

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
    public void downstream() {
        events.add(new Downstream());
    }

    @Override
    public void request(long sequenceNumber, int requestedMessageCount) {
        events.add(new Request(sequenceNumber, requestedMessageCount));
    }

    @Override
    public void endOfSession() {
        events.add(new EndOfSession());
    }

    public interface Event {
    }

    public static class Downstream extends Value implements Event {
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
