package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

import java.util.ArrayList;
import java.util.List;
import org.jvirtanen.value.Value;

class SoupBinTCPServerStatus implements SoupBinTCPServerStatusListener {

    private List<Event> events;

    public SoupBinTCPServerStatus() {
        this.events = new ArrayList<>();
    }

    public List<Event> collect() {
        return events;
    }

    @Override
    public void loginRequest(SoupBinTCPServer session, SoupBinTCP.LoginRequest payload) {
        events.add(new LoginRequest(payload.username, payload.password,
                    payload.requestedSession, payload.requestedSequenceNumber));
    }

    @Override
    public void logoutRequest(SoupBinTCPServer session) {
        events.add(new LogoutRequest());
    }

    @Override
    public void heartbeatTimeout(SoupBinTCPServer session) {
        events.add(new HeartbeatTimeout());
    }

    public interface Event {
    }

    public static class LoginRequest extends Value implements Event {
        public final String username;
        public final String password;
        public final String requestedSession;
        public final long   requestedSequenceNumber;

        public LoginRequest(String username, String password,
                String requestedSession, long requestedSequenceNumber) {
            this.username                = username;
            this.password                = password;
            this.requestedSession        = requestedSession;
            this.requestedSequenceNumber = requestedSequenceNumber;
        }
    }

    public static class LogoutRequest extends Value implements Event {
    }

}
