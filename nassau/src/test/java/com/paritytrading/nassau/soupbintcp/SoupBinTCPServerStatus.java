package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

import com.paritytrading.foundation.ASCII;
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
    public void loginRequest(SoupBinTCPServer server, SoupBinTCP.LoginRequest payload) {
        String username                = ASCII.get(payload.username);
        String password                = ASCII.get(payload.password);
        String requestedSession        = ASCII.get(payload.requestedSession);
        long   requestedSequenceNumber = ASCII.getLong(payload.requestedSequenceNumber);

        events.add(new LoginRequest(username, password, requestedSession, requestedSequenceNumber));
    }

    @Override
    public void logoutRequest(SoupBinTCPServer server) {
        events.add(new LogoutRequest());
    }

    @Override
    public void heartbeatTimeout(SoupBinTCPServer server) {
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
