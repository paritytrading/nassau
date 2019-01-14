package com.paritytrading.nassau.soupbintcp.server;

import static org.jvirtanen.util.Applications.*;

import java.io.IOException;
import java.net.InetSocketAddress;

class TestServer {

    private static final String USAGE = "nassau-soupbintcp-server <port>";

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            usage(USAGE);

        try {
            main(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            usage(USAGE);
        }
    }

    private static void main(int port) throws IOException {
        final Server server = Server.open(new InetSocketAddress(port));

        Server.Session session = server.accept();

        server.close();

        while (session.receive() != -1);

        session.close();
    }

}
