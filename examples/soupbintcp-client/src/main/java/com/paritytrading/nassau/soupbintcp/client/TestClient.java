package com.paritytrading.nassau.soupbintcp.client;

import static org.jvirtanen.util.Applications.*;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.soupbintcp.SoupBinTCP;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClient;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClientStatusListener;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.HdrHistogram.Histogram;

class TestClient implements Closeable, MessageListener {

    private static final String USAGE = "nassau-soupbintcp-client <host> <port> <packets> <packets-per-second>";

    private SoupBinTCPClient transport;

    private Histogram histogram;

    private int receiveCount;

    public static void main(String[] args) throws IOException {
        if (args.length != 4)
            usage(USAGE);

        try {
            String host             = args[0];
            int    port             = Integer.parseInt(args[1]);
            int    packets          = Integer.parseInt(args[2]);
            int    packetsPerSecond = Integer.parseInt(args[3]);

            main(new InetSocketAddress(host, port), packets, packetsPerSecond);
        } catch (NumberFormatException e) {
            usage(USAGE);
        }
    }

    private static void main(InetSocketAddress address, int packets, int packetsPerSecond) throws IOException {
        try (final TestClient client = TestClient.connect(address)) {
            long intervalNanos = 1_000_000_000 / packetsPerSecond;

            ByteBuffer buffer = ByteBuffer.allocateDirect(Long.BYTES);

            System.out.println("Warming up...");

            client.sendAndReceive(buffer, packets, intervalNanos);

            client.reset();

            System.out.println("Benchmarking...");

            client.sendAndReceive(buffer, packets, intervalNanos);

            System.out.printf("Results (n = %d)\n", packets);
            System.out.printf("\n");
            System.out.printf( "      Min: %10.2f µs\n", client.histogram.getMinValue()                / 1000.0);
            System.out.printf("   50.00%%: %10.2f µs\n", client.histogram.getValueAtPercentile(50.00)  / 1000.0);
            System.out.printf("   90.00%%: %10.2f µs\n", client.histogram.getValueAtPercentile(90.00)  / 1000.0);
            System.out.printf("   99.00%%: %10.2f µs\n", client.histogram.getValueAtPercentile(99.00)  / 1000.0);
            System.out.printf("   99.90%%: %10.2f µs\n", client.histogram.getValueAtPercentile(99.90)  / 1000.0);
            System.out.printf("   99.99%%: %10.2f µs\n", client.histogram.getValueAtPercentile(99.99)  / 1000.0);
            System.out.printf("  100.00%%: %10.2f µs\n", client.histogram.getValueAtPercentile(100.00) / 1000.0);
            System.out.printf("\n");
        }
    }

    private static TestClient connect(SocketAddress address) throws IOException {
        SocketChannel channel = SocketChannel.open();

        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.connect(address);
        channel.configureBlocking(false);

        return new TestClient(channel);
    }

    private TestClient(SocketChannel channel) {
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

    @Override
    public void message(ByteBuffer buffer) {
        histogram.recordValue(System.nanoTime() - buffer.getLong());

        receiveCount++;
    }

    @Override
    public void close() throws IOException {
        transport.close();
    }

    private void sendAndReceive(ByteBuffer buffer, int packets, long intervalNanos) throws IOException {
        for (long sendAtNanos = System.nanoTime(); receiveCount < packets; transport.receive()) {
            if (System.nanoTime() >= sendAtNanos) {
                buffer.clear();

                buffer.putLong(sendAtNanos);

                buffer.flip();

                transport.send(buffer);

                sendAtNanos += intervalNanos;
            }
        }
    }

    private void reset() {
        histogram.reset();

        receiveCount = 0;
    }

}
