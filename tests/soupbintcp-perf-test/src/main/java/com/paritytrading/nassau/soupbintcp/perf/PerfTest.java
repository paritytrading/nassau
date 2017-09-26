package com.paritytrading.nassau.soupbintcp.perf;

import static org.jvirtanen.util.Applications.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

class PerfTest {

    private static final String USAGE = "nassau-soupbintcp-perf-test <packets>";

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            usage(USAGE);

        try {
            main(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            usage(USAGE);
        }
    }

    private static void main(int packets) throws IOException {
        final Server server = Server.open(new InetSocketAddress(0));
        final Client client = Client.connect(server.getLocalAddress());

        new Thread(() -> {
            Server.Session session = null;

            try {
                session = server.accept();

                server.close();

                while (session.receive() != -1);
            } catch (IOException e) {
            } finally {
                try {
                    if (session != null)
                        session.close();
                } catch (IOException e) {
                }
            }
        }).start();

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 'f', 'o', 'o' });

        System.out.println("Warming up...");

        for (int i = 0; i < packets; i++) {
            client.send(buffer);

            buffer.flip();

            client.receive();
        }

        client.getHistogram().reset();

        System.out.println("Benchmarking...");

        for (int i = 0; i < packets; i++) {
            client.send(buffer);

            buffer.flip();

            client.receive();
        }

        client.close();

        System.out.printf("Results (n = %d):\n", packets);
        System.out.printf("\n");
        System.out.printf( "      Min: %10.2f µs\n", client.getHistogram().getMinValue()                / 1000.0);
        System.out.printf("   50.00%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(50.00)  / 1000.0);
        System.out.printf("   90.00%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(90.00)  / 1000.0);
        System.out.printf("   99.00%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(99.00)  / 1000.0);
        System.out.printf("   99.90%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(99.90)  / 1000.0);
        System.out.printf("   99.99%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(99.99)  / 1000.0);
        System.out.printf("  100.00%%: %10.2f µs\n", client.getHistogram().getValueAtPercentile(100.00) / 1000.0);
        System.out.printf("\n");
    }

}
