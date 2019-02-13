package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

import java.util.ArrayList;
import java.util.List;
import org.jvirtanen.value.Value;

class SoupBinTCPClientStatus implements SoupBinTCPClientStatusListener {

    private List<Event> events;

    public SoupBinTCPClientStatus() {
        this.events = new ArrayList<>();
    }

    public List<Event> collect() {
        return events;
    }

    @Override
    public void loginAccepted(SoupBinTCPClient client, SoupBinTCP.LoginAccepted payload) {
        String session        = payload.getSession();
        long   sequenceNumber = payload.getSequenceNumber();

        events.add(new LoginAccepted(session, sequenceNumber));
    }

    @Override
    public void loginRejected(SoupBinTCPClient client, SoupBinTCP.LoginRejected payload) {
        events.add(new LoginRejected(payload.rejectReasonCode));
    }

    @Override
    public void endOfSession(SoupBinTCPClient client) {
        events.add(new EndOfSession());
    }

    @Override
    public void heartbeatTimeout(SoupBinTCPClient client) {
        events.add(new HeartbeatTimeout());
    }

    public interface Event {
    }

    public static class LoginAccepted extends Value implements Event {
        public final String session;
        public final long   sequenceNumber;

        public LoginAccepted(String session, long sequenceNumber) {
            this.session        = session;
            this.sequenceNumber = sequenceNumber;
        }
    }

    public static class LoginRejected extends Value implements Event {
        public final byte rejectReasonCode;

        public LoginRejected(byte rejectReasonCode) {
            this.rejectReasonCode = rejectReasonCode;
        }
    }

    public static class EndOfSession extends Value implements Event {
    }

}
