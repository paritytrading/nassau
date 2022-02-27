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
package com.paritytrading.nassau.binaryfile.recorder;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.binaryfile.BinaryFILEWriter;
import com.paritytrading.nassau.moldudp64.MoldUDP64Client;
import com.paritytrading.nassau.moldudp64.MoldUDP64ClientState;
import com.paritytrading.nassau.moldudp64.MoldUDP64ClientStatusListener;
import com.paritytrading.nassau.soupbintcp.SoupBinTCP;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClient;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClientStatusListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class Recorder {

    private static final int TIMEOUT_MILLIS = 1000;

    private static volatile boolean receive = true;

    public static void main(String[] args) {
        if (args.length != 2)
            usage();

        try {
            main(config(args[0]), new File(args[1]));
        } catch (ConfigException | FileNotFoundException e) {
            error(e);
        } catch (IOException e) {
            fatal(e);
        }
    }

    private static void main(Config config, File file) throws IOException {
        addShutdownHook();

        try (final BinaryFILEWriter writer = BinaryFILEWriter.open(file)) {

            MessageListener listener = new MessageListener() {

                @Override
                public void message(ByteBuffer buffer) throws IOException {
                    writer.write(buffer);
                }

            };

            if (config.hasPath("session.multicast-interface")) {
                try (MoldUDP64Client client = join(config, listener)) {
                    receive(client);
                }
            } else {
                try (SoupBinTCPClient client = connect(config, listener)) {
                    receive(client);
                }
            }
        }
    }

    private static SoupBinTCPClient connect(Config config, MessageListener listener) throws IOException {
        String address  = config.getString("session.address");
        int    port     = config.getInt("session.port");
        String username = config.getString("session.username");
        String password = config.getString("session.password");

        SocketChannel channel = SocketChannel.open();

        channel.connect(new InetSocketAddress(address, port));
        channel.configureBlocking(false);

        SoupBinTCPClientStatusListener statusListener = new SoupBinTCPClientStatusListener() {

            @Override
            public void heartbeatTimeout(SoupBinTCPClient session) {
                receive = false;
            };

            @Override
            public void loginAccepted(SoupBinTCPClient session, SoupBinTCP.LoginAccepted payload) {
            }

            @Override
            public void loginRejected(SoupBinTCPClient session, SoupBinTCP.LoginRejected payload) {
                receive = false;
            }

            @Override
            public void endOfSession(SoupBinTCPClient session) {
                receive = false;
            }

        };

        SoupBinTCPClient client = new SoupBinTCPClient(channel, listener, statusListener);

        SoupBinTCP.LoginRequest message = new SoupBinTCP.LoginRequest();

        message.setUsername(username);
        message.setPassword(password);
        message.setRequestedSession("");
        message.setRequestedSequenceNumber(1L);

        client.login(message);

        return client;
    }

    private static MoldUDP64Client join(Config config, MessageListener listener) throws IOException {
        String multicastInterface = config.getString("session.multicast-interface");
        String multicastGroup     = config.getString("session.multicast-group");
        int    multicastPort      = config.getInt("session.multicast-port");
        String requestAddress     = config.getString("session.request-address");
        int    requestPort        = config.getInt("session.request-port");

        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(multicastPort));
        channel.join(InetAddress.getByName(multicastGroup), getNetworkInterface(multicastInterface));
        channel.configureBlocking(false);

        DatagramChannel requestChannel = DatagramChannel.open(StandardProtocolFamily.INET);

        requestChannel.configureBlocking(false);

        MoldUDP64ClientStatusListener statusListener = new MoldUDP64ClientStatusListener() {

            @Override
            public void state(MoldUDP64Client session, MoldUDP64ClientState next) {
            }

            @Override
            public void downstream(MoldUDP64Client session, long sequenceNumber, int messageCount) {
            }

            @Override
            public void request(MoldUDP64Client session, long sequenceNumber, int requestedMessageCount) {
            }

            @Override
            public void endOfSession(MoldUDP64Client session) {
                receive = false;
            }

        };

        return new MoldUDP64Client(channel, requestChannel,
                new InetSocketAddress(requestAddress, requestPort), listener, statusListener);
    }

    private static void receive(MoldUDP64Client client) throws IOException {
        try (Selector selector = Selector.open()) {

            SelectionKey key = client.getChannel().register(selector, SelectionKey.OP_READ);

            SelectionKey requestKey = client.getRequestChannel().register(selector, SelectionKey.OP_READ);

            while (receive) {
                int numKeys = selector.select();
                if (numKeys > 0) {
                    if (selector.selectedKeys().contains(key))
                        client.receive();

                    if (selector.selectedKeys().contains(requestKey))
                        client.receiveResponse();

                    selector.selectedKeys().clear();
                }
            }
        }
    }

    private static void receive(SoupBinTCPClient client) throws IOException {
        try (Selector selector = Selector.open()) {

            client.getChannel().register(selector, SelectionKey.OP_READ);

            while (receive) {
                int numKeys = selector.select(TIMEOUT_MILLIS);
                if (numKeys > 0) {
                    if (client.receive() < 0)
                        break;

                    selector.selectedKeys().clear();
                }

                client.keepAlive();
            }
        }
    }

    private static NetworkInterface getNetworkInterface(String nameOrInetAddress) throws IOException {
        NetworkInterface networkInterface = NetworkInterface.getByName(nameOrInetAddress);
        if (networkInterface != null)
            return networkInterface;

        return NetworkInterface.getByInetAddress(InetAddress.getByName(nameOrInetAddress));
    }

    private static void addShutdownHook() {
        final Thread main = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                receive = false;

                try {
                    main.join();
                } catch (InterruptedException e) {
                }
            }

        });
    }

    private static Config config(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            throw new FileNotFoundException(filename + ": No such file");

        return ConfigFactory.parseFile(file);
    }

    private static void usage() {
        System.err.println("Usage: nassau-binaryfile-recorder <configuration-file> <output-file>");
        System.exit(2);
    }

    private static void error(Throwable throwable) {
        System.err.println("error: " + throwable.getMessage());
        System.exit(1);
    }

    private static void fatal(Throwable throwable) {
        System.err.println("fatal: " + throwable.getMessage());
        System.err.println();
        throwable.printStackTrace(System.err);
        System.err.println();
        System.exit(1);
    }

}
