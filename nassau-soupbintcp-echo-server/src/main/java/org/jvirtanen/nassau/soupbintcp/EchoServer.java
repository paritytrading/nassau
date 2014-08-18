package org.jvirtanen.nassau.soupbintcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCP;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServer;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServerStatusListener;

class EchoServer {

    public static void main(String[] args) {
        if (args.length != 1)
            usage();

        try {
            Server.open(port(args[0])).run();
        } catch (Exception e) {
            error(e);
        }
    }

    private static int atoi(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private static InetSocketAddress port(String value) {
        return new InetSocketAddress(atoi(value));
    }

    private static void usage() {
        System.err.println("Usage: nassau-soupbintcp-echo-server <port>");
        System.exit(2);
    }

    private static void error(Throwable throwable) {
        System.err.println("error: " + throwable.getMessage());
        System.exit(1);
    }

    private static class Server {

        private static final long TIMEOUT_MILLIS = 100;

        private ServerSocketChannel serverChannel;

        private List<Session> sessions;
        private List<Session> closed;

        private Server(ServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;

            this.sessions = new ArrayList<>();
            this.closed   = new ArrayList<>();
        }

        public static Server open(InetSocketAddress address) throws IOException {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();

            serverChannel.bind(address);
            serverChannel.configureBlocking(false);

            return new Server(serverChannel);
        }

        public void run() throws IOException {
            Selector selector = Selector.open();

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int numKeys = selector.select(TIMEOUT_MILLIS);
                if (numKeys > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();

                        if (key.isAcceptable())
                            accept(selector);

                        if (key.isReadable())
                            read(key);

                        keys.remove();
                    }
                }

                keepAlive();

                close();
            }
        }

        private void accept(Selector selector) {
            SocketChannel channel = null;

            try {
                channel = serverChannel.accept();
            } catch (IOException e) {
            }

            if (channel == null)
                return;

            try {
                channel.configureBlocking(false);
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            } catch (IOException e) {
                close(channel);
            }

            Session session = new Session(channel);
            sessions.add(session);

            try {
                channel.register(selector, SelectionKey.OP_READ, session);
            } catch (IOException e) {
                sessions.remove(session);
                close(channel);
            }
        }

        private void close(SocketChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
            }
        }

        private void read(SelectionKey key) {
            Session session = (Session)key.attachment();

            try {
                if (session.getTransport().receive() < 0)
                    closed.add(session);
            } catch (IOException e) {
                closed.add(session);
            }
        }

        private void keepAlive() {
            for (int i = 0; i < sessions.size(); i++) {
                Session session = sessions.get(i);

                try {
                    session.getTransport().keepAlive();
                    if (session.isClosed())
                        closed.add(session);
                } catch (IOException e) {
                    closed.add(session);
                }
            }
        }

        private void close() {
            for (int i = 0; i < closed.size(); i++) {
                Session session = closed.get(i);

                try {
                    session.getTransport().close();
                } catch (IOException e) {
                }

                sessions.remove(session);
            }

            if (!closed.isEmpty())
                closed.clear();
        }

    }

    private static class Session implements SoupBinTCPServerStatusListener, MessageListener {

        private SoupBinTCP.LoginAccepted accepted;

        private SoupBinTCPServer transport;

        private boolean closed;

        Session(SocketChannel channel) {
            accepted = new SoupBinTCP.LoginAccepted();

            transport = new SoupBinTCPServer(channel, this, this);

            closed = false;
        }

        SoupBinTCPServer getTransport() {
            return transport;
        }

        boolean isClosed() {
            return closed;
        }

        @Override
        public void heartbeatTimeout() {
            closed = true;
        }

        @Override
        public void loginRequest(SoupBinTCP.LoginRequest payload) throws IOException {
            accepted.session        = payload.requestedSession;
            accepted.sequenceNumber = payload.requestedSequenceNumber;

            transport.accept(accepted);
        }

        @Override
        public void logoutRequest() {
            closed = true;
        }

        @Override
        public void message(ByteBuffer payload) throws IOException {
            transport.send(payload);
        }

    }

}
