package org.jvirtanen.nassau.soupbintcp.gateway;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.moldudp64.MoldUDP64Client;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCP;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServer;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServerStatusListener;

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
        loginAccepted.session        = payload.requestedSession;
        loginAccepted.sequenceNumber = payload.requestedSequenceNumber;

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
