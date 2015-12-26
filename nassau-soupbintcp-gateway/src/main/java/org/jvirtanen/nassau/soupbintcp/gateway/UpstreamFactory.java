package org.jvirtanen.nassau.soupbintcp.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.moldudp64.MoldUDP64Client;
import org.jvirtanen.nassau.moldudp64.MoldUDP64ClientState;
import org.jvirtanen.nassau.moldudp64.MoldUDP64ClientStatusListener;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServer;

class UpstreamFactory {

    private NetworkInterface  multicastInterface;
    private InetSocketAddress multicastGroup;
    private InetSocketAddress requestAddress;

    public UpstreamFactory(NetworkInterface multicastInterface,
            InetSocketAddress multicastGroup, InetSocketAddress requestAddress) {
        this.multicastInterface = multicastInterface;
        this.multicastGroup     = multicastGroup;
        this.requestAddress     = requestAddress;
    }

    public MoldUDP64Client create(final SoupBinTCPServer downstream) throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(multicastGroup.getPort()));
        channel.join(multicastGroup.getAddress(), multicastInterface);
        channel.configureBlocking(false);

        DatagramChannel requestChannel = DatagramChannel.open(StandardProtocolFamily.INET);

        requestChannel.configureBlocking(false);

        MessageListener listener = new MessageListener() {

            @Override
            public void message(ByteBuffer buffer) throws IOException {
                downstream.send(buffer);
            }

        };

        MoldUDP64ClientStatusListener statusListener = new MoldUDP64ClientStatusListener() {

            @Override
            public void state(MoldUDP64ClientState next) {
            }

            @Override
            public void downstream() {
            }

            @Override
            public void request(long sequenceNumber, int requestedMessageCount) {
            }

            @Override
            public void endOfSession() throws IOException {
                downstream.endSession();
            }

        };

        return new MoldUDP64Client(channel, requestChannel, requestAddress,
                listener, statusListener);
    }

}
