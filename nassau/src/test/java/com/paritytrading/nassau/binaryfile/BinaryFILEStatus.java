package com.paritytrading.nassau.binaryfile;

import java.util.ArrayList;
import java.util.List;
import org.jvirtanen.value.Value;

class BinaryFILEStatus implements BinaryFILEStatusListener {

    private List<Event> events;

    public BinaryFILEStatus() {
        this.events = new ArrayList<>();
    }

    public List<Event> collect() {
        return events;
    }

    @Override
    public void endOfSession() {
        events.add(new EndOfSession());
    }

    public interface Event {
    }

    public static class EndOfSession extends Value implements Event {
    }

}
