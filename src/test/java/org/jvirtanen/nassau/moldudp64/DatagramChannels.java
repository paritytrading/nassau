package org.jvirtanen.nassau.moldudp64;

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

        channel.connect(new InetSocketAddress(multicastGroup(), getLocalPort(clientChannel)));

        return channel;
    }

    static DatagramChannel openServerRequestChannel() throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.bind(null);

        return channel;
    }

    static DatagramChannel openClientRequestChannel(DatagramChannel serverRequestChannel) throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.connect(serverRequestChannel.getLocalAddress());

        return channel;
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
