/*
 * Copyright 2014 Nassau authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paritytrading.nassau.soupbintcp.gateway;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

class Gateway {

    public static void main(String[] args) {
        if (args.length != 1)
            usage();

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

    private static UpstreamFactory upstream(Config config) throws IOException {
        String multicastInterface = config.getString("upstream.multicast-interface");
        String multicastGroup     = config.getString("upstream.multicast-group");
        int    multicastPort      = config.getInt("upstream.multicast-port");
        String requestAddress     = config.getString("upstream.request-address");
        int    requestPort        = config.getInt("upstream.request-port");

        return new UpstreamFactory(getNetworkInterface(multicastInterface),
                new InetSocketAddress(multicastGroup, multicastPort),
                new InetSocketAddress(requestAddress, requestPort));
    }

    private static DownstreamServer downstream(Config config, UpstreamFactory upstream) throws IOException {
        String address = config.getString("downstream.address");
        int    port    = config.getInt("downstream.port");

        return DownstreamServer.open(upstream, new InetSocketAddress(address, port));
    }

    private static NetworkInterface getNetworkInterface(String nameOrInetAddress) throws IOException {
        NetworkInterface networkInterface = NetworkInterface.getByName(nameOrInetAddress);
        if (networkInterface != null)
            return networkInterface;

        return NetworkInterface.getByInetAddress(InetAddress.getByName(nameOrInetAddress));
    }

    private static Config config(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            throw new FileNotFoundException(filename + ": No such file");

        return ConfigFactory.parseFile(file);
    }

    private static void usage() {
        System.err.println("Usage: nassau-soupbintcp-gateway <configuration-file>");
        System.exit(2);
    }

    private static void error(Throwable throwable) {
        System.err.println("error: " + throwable.getMessage());
        System.exit(1);
    }

    private static void fatal(Throwable throwable) {
        System.err.println("fatal: " + throwable.getMessage());
        System.err.println();
        throwable.printStackTrace(System.err);
        System.err.println();
        System.exit(1);
    }

}
