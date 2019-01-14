package com.paritytrading.nassau.soupbintcp.client;

import static org.jvirtanen.util.Applications.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

class TestClient {

    private static final String USAGE = "nassau-soupbintcp-client <host> <port> <packets>";

    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            usage(USAGE);

        try {
            String host   = args[0];
            int    port   = Integer.parseInt(args[1]);
            int    orders = Integer.parseInt(args[2]);

            main(new InetSocketAddress(host, port), orders);
        } catch (NumberFormatException e) {
            usage(USAGE);
        }
    }

    private static void main(InetSocketAddress address, int packets) throws IOException {
        final Client client = Client.connect(address);

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
