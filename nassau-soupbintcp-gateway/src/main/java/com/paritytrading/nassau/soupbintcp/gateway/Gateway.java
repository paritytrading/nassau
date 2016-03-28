package com.paritytrading.nassau.soupbintcp.gateway;

import static org.jvirtanen.util.Applications.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import org.jvirtanen.config.Configs;

class Gateway {

    public static void main(String[] args) {
        if (args.length != 1)
            usage("nassau-soupbintcp-gateway <configuration-file>");

        try {
            main(config(args[0]));
        } catch (ConfigException | FileNotFoundException e) {
            error(e);
        } catch (IOException e) {
            fatal(e);
        }
    }

    private static void main(Config config) throws IOException {
        UpstreamFactory  upstream   = upstream(config);
        DownstreamServer downstream = downstream(config, upstream);

        Events.process(downstream);
    }

    private static UpstreamFactory upstream(Config config) {
        NetworkInterface multicastInterface = Configs.getNetworkInterface(config, "upstream.multicast-interface");
        InetAddress      multicastGroup     = Configs.getInetAddress(config, "upstream.multicast-group");
        int              multicastPort      = Configs.getPort(config, "upstream.multicast-port");
        InetAddress      requestAddress     = Configs.getInetAddress(config, "upstream.request-address");
        int              requestPort        = Configs.getPort(config, "upstream.request-port");

        return new UpstreamFactory(multicastInterface, new InetSocketAddress(multicastGroup, multicastPort),
                new InetSocketAddress(requestAddress, requestPort));
    }

    private static DownstreamServer downstream(Config config, UpstreamFactory upstream) throws IOException {
        int port = Configs.getPort(config, "downstream.port");

        return DownstreamServer.open(upstream, port);
    }

}
