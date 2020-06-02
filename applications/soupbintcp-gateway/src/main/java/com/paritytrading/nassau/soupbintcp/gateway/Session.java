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
package com.paritytrading.nassau.soupbintcp.gateway;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.moldudp64.MoldUDP64Client;
import com.paritytrading.nassau.soupbintcp.SoupBinTCP;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPServer;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPServerStatusListener;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Session implements Closeable, MessageListener, SoupBinTCPServerStatusListener {

    private static SoupBinTCP.LoginAccepted loginAccepted = new SoupBinTCP.LoginAccepted();

    private MoldUDP64Client  upstream;
    private SoupBinTCPServer downstream;

    public Session(UpstreamFactory upstream, SocketChannel downstream) throws IOException {
        this.downstream = new SoupBinTCPServer(downstream, this, this);
        this.upstream   = upstream.create(this.downstream);
    }

    @Override
    public void close() throws IOException {
        upstream.close();
        downstream.close();
    }

    @Override
    public void message(ByteBuffer buffer) throws IOException {
        close();
    }

    @Override
    public void heartbeatTimeout(SoupBinTCPServer session) throws IOException {
        close();
    }

    @Override
    public void loginRequest(SoupBinTCPServer session, SoupBinTCP.LoginRequest payload) throws IOException {
        loginAccepted.setSession(payload.requestedSession);
        loginAccepted.setSequenceNumber(1);

        downstream.accept(loginAccepted);
    }

    @Override
    public void logoutRequest(SoupBinTCPServer session) throws IOException {
        close();
    }

    public MoldUDP64Client getUpstream() {
        return upstream;
    }

    public SoupBinTCPServer getDownstream() {
        return downstream;
    }

}
