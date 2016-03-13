package org.jvirtanen.nassau.soupbintcp.perf;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCP;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServer;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServerStatusListener;

class Server implements Closeable {

    private ServerSocketChannel serverChannel;

    private Server(ServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    public static Server open(SocketAddress address) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(address);

        return new Server(serverChannel);
    }

    public Session accept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.configureBlocking(false);

        return new Session(channel);
    }

    public SocketAddress getLocalAddress() throws IOException {
        return serverChannel.getLocalAddress();
    }

    @Override
    public void close() throws IOException {
        serverChannel.close();
    }

    public static class Session implements Closeable, MessageListener {

        private SoupBinTCPServer transport;

        private Session(SocketChannel channel) {
            transport = new SoupBinTCPServer(channel, this, new SoupBinTCPServerStatusListener() {

                @Override
                public void loginRequest(SoupBinTCPServer session, SoupBinTCP.LoginRequest message) {
                }

                @Override
                public void logoutRequest(SoupBinTCPServer session) {
                }

                @Override
                public void heartbeatTimeout(SoupBinTCPServer session) {
                }

            });
        }

        @Override
        public void message(ByteBuffer buffer) throws IOException {
            transport.send(buffer);
        }

        @Override
        public void close() throws IOException {
            transport.close();
        }

        public int receive() throws IOException {
            return transport.receive();
        }

    }

}
