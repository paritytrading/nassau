package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

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
        String username                = payload.getUsername();
        String password                = payload.getPassword();
        String requestedSession        = payload.getRequestedSession();
        long   requestedSequenceNumber = payload.getRequestedSequenceNumber(); 

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
