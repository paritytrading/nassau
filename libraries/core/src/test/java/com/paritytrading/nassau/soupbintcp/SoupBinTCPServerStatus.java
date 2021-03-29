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
package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCPSessionStatus.*;

import com.paritytrading.nassau.Value;
import java.util.ArrayList;
import java.util.List;

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
