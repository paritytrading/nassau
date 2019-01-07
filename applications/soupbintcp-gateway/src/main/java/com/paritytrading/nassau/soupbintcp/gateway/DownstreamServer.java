package com.paritytrading.nassau.soupbintcp.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

class DownstreamServer {

    private UpstreamFactory upstream;

    private ServerSocketChannel serverChannel;

    private DownstreamServer(UpstreamFactory upstream, ServerSocketChannel serverChannel) {
        this.upstream      = upstream;
        this.serverChannel = serverChannel;
    }

    public static DownstreamServer open(UpstreamFactory upstream,
            InetSocketAddress address) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.bind(address);
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
