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
package com.paritytrading.nassau.moldudp64;

import com.paritytrading.nassau.Value;
import java.util.ArrayList;
import java.util.List;

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
