package com.paritytrading.nassau.soupbintcp.gateway;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

class Events {

    private static final int TIMEOUT = 500;

    public static void process(DownstreamServer downstream) throws IOException {
        Selector selector = Selector.open();

        final List<Session> toKeepAlive = new ArrayList<>();
        final List<Session> toCleanUp   = new ArrayList<>();

        downstream.getServerChannel().register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int numKeys = selector.select(TIMEOUT);

            if (numKeys > 0) {
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        final Session session = downstream.accept();
                        if (session == null)
                            continue;

                        session.getDownstream().getChannel().register(selector,
                                SelectionKey.OP_READ, new Receiver() {

                                    @Override
                                    public void close() {
                                        toCleanUp.add(session);
                                    }

                                    @Override
                                    public void receive() throws IOException {
                                        session.getDownstream().receive();
                                    }

                                });

                        session.getUpstream().getChannel().register(selector,
                                SelectionKey.OP_READ, new Receiver() {

                                    @Override
                                    public void close() {
                                        toCleanUp.add(session);
                                    }

                                    @Override
                                    public void receive() throws IOException {
                                        session.getUpstream().receive();
                                    }

                                });

                        session.getUpstream().getRequestChannel().register(selector,
                                SelectionKey.OP_READ, new Receiver() {

                                    @Override
                                    public void close() {
                                        toCleanUp.add(session);
                                    }

                                    @Override
                                    public void receive() throws IOException {
                                        session.getUpstream().receiveResponse();
                                    }

                                });

                        toKeepAlive.add(session);
                    } else {
                        Receiver receiver = (Receiver)key.attachment();

                        try {
                            receiver.receive();
                        } catch (IOException e1) {
                            try {
                                receiver.close();
                            } catch (IOException e2) {
                            }
                        }
                    }
                }

                selector.selectedKeys().clear();
            }

            for (int i = 0; i < toKeepAlive.size(); i++) {
                Session session = toKeepAlive.get(i);

                try {
                    session.getDownstream().keepAlive();
                } catch (IOException e) {
                    toCleanUp.add(session);
                }
            }

            if (toCleanUp.isEmpty())
                continue;

            for (int i = 0; i < toCleanUp.size(); i++) {
                try (Session session = toCleanUp.get(i)) {
                    toKeepAlive.remove(session);
                }
            }

            toCleanUp.clear();
        }
    }

    private interface Receiver extends Closeable {

        void receive() throws IOException;

    }

}
