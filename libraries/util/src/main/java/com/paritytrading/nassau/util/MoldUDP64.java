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
package com.paritytrading.nassau.util;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.moldudp64.MoldUDP64Client;
import com.paritytrading.nassau.moldudp64.MoldUDP64ClientState;
import com.paritytrading.nassau.moldudp64.MoldUDP64ClientStatusListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

/**
 * Utility methods for working with the NASDAQ MoldUDP64 1.00 protocol.
 */
public class MoldUDP64 {

    private MoldUDP64() {
    }

    /**
     * Receive messages. Invoke the message listener on each message. Continue
     * until a packet indicating the End of Session is received.
     *
     * @param multicastInterface the multicast interface
     * @param multicastGroup the multicast group
     * @param requestAddress the request address
     * @param listener a message listener
     * @throws IOException if an I/O error occurs
     */
    public static void receive(NetworkInterface multicastInterface,
            InetSocketAddress multicastGroup, InetSocketAddress requestAddress,
            MessageListener listener) throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(multicastGroup.getPort()));
        channel.join(multicastGroup.getAddress(), multicastInterface);
        channel.configureBlocking(false);

        DatagramChannel requestChannel = DatagramChannel.open(StandardProtocolFamily.INET);

        requestChannel.configureBlocking(false);

        StatusListener statusListener = new StatusListener();

        try (Selector selector = Selector.open();
                MoldUDP64Client client = new MoldUDP64Client(channel, requestChannel,
                    requestAddress, listener, statusListener)) {
            SelectionKey channelKey = channel.register(selector, SelectionKey.OP_READ);

            SelectionKey requestChannelKey = requestChannel.register(selector, SelectionKey.OP_READ);

            while (statusListener.receive) {
                while (selector.select() == 0);

                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                if (selectedKeys.contains(channelKey))
                    client.receive();

                if (selectedKeys.contains(requestChannelKey))
                    client.receiveResponse();

                selectedKeys.clear();
            }
        }
    }

    private static class StatusListener implements MoldUDP64ClientStatusListener {

        boolean receive = true;

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

    }

}
