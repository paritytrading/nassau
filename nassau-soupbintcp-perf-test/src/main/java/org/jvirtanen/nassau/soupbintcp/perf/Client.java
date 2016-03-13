package org.jvirtanen.nassau.soupbintcp.perf;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.HdrHistogram.Histogram;
import org.jvirtanen.nassau.MessageListener;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCP;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPClient;
import org.jvirtanen.nassau.soupbintcp.SoupBinTCPClientStatusListener;

class Client implements Closeable, MessageListener {

    private SoupBinTCPClient transport;

    private Histogram histogram;

    private long sentAtNanoTime;

    private boolean received;

    private Client(SocketChannel channel) {
        transport = new SoupBinTCPClient(channel, this, new SoupBinTCPClientStatusListener() {

            @Override
            public void loginAccepted(SoupBinTCPClient session, SoupBinTCP.LoginAccepted payload) {
            }

            @Override
            public void loginRejected(SoupBinTCPClient session, SoupBinTCP.LoginRejected payload) {
            }

            @Override
            public void endOfSession(SoupBinTCPClient session) {
            }

            @Override
            public void heartbeatTimeout(SoupBinTCPClient session) {
            }

        });

        histogram = new Histogram(3);
    }

    public static Client connect(SocketAddress address) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.connect(address);
        channel.configureBlocking(false);

        return new Client(channel);
    }

    @Override
    public void message(ByteBuffer buffer) {
        histogram.recordValue(System.nanoTime() - sentAtNanoTime);

        received = true;
    }

    @Override
    public void close() throws IOException {
        transport.close();
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void receive() throws IOException {
        while (!received) {
            if (transport.receive() < 0)
                return;
        }

        received = false;
    }

    public void send(ByteBuffer buffer) throws IOException {
        sentAtNanoTime = System.nanoTime();

        transport.send(buffer);
    }

}
