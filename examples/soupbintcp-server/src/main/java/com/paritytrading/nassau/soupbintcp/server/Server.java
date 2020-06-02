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
package com.paritytrading.nassau.soupbintcp.server;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.soupbintcp.SoupBinTCP;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPServer;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPServerStatusListener;
import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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
