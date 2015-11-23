package org.jvirtanen.nassau.soupbintcp.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCP;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServer;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPServerStatusListener;

class DownstreamServer {

    private UpstreamFactory upstream;

    private ServerSocketChannel serverChannel;

    private DownstreamServer(UpstreamFactory upstream, ServerSocketChannel serverChannel) {
        this.upstream      = upstream;
        this.serverChannel = serverChannel;
    }

    public static DownstreamServer open(UpstreamFactory upstream, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        return new DownstreamServer(upstream, serverChannel);
    }

    public ServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public Session accept() throws IOException {
        SocketChannel downstream = serverChannel.accept();
        if (downstream == null)
            return null;

        downstream.setOption(StandardSocketOptions.TCP_NODELAY, true);
        downstream.configureBlocking(false);

        return new Session(upstream, downstream);
    }

}
