package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

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
    public void loginAccepted(SoupBinTCPClient session, SoupBinTCP.LoginAccepted payload) {
        events.add(new LoginAccepted(payload.session, payload.sequenceNumber));
    }

    @Override
    public void loginRejected(SoupBinTCPClient session, SoupBinTCP.LoginRejected payload) {
        events.add(new LoginRejected(payload.rejectReasonCode));
    }

    @Override
    public void endOfSession(SoupBinTCPClient session) {
        events.add(new EndOfSession());
    }

    @Override
    public void heartbeatTimeout(SoupBinTCPClient session) {
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
