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
package com.paritytrading.nassau.moldudp64;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

class DatagramChannels {

    static DatagramChannel openClientChannel() throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.bind(null);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, loopbackInterface());
        channel.join(multicastGroup(), loopbackInterface());

        return channel;
    }

    static DatagramChannel openServerChannel(DatagramChannel clientChannel) throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, loopbackInterface());
        channel.connect(new InetSocketAddress(multicastGroup(), getLocalPort(clientChannel)));

        return channel;
    }

    static DatagramChannel openServerRequestChannel() throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.bind(null);

        return channel;
    }

    static DatagramChannel openClientRequestChannel() throws IOException {
        return DatagramChannel.open(StandardProtocolFamily.INET);
    }

    private static InetAddress multicastGroup() throws IOException {
        return InetAddress.getByName("224.0.0.1");
    }

    private static NetworkInterface loopbackInterface() throws IOException {
        return NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
    }

    private static int getLocalPort(DatagramChannel channel) throws IOException {
        return ((InetSocketAddress)channel.getLocalAddress()).getPort();
    }

}
