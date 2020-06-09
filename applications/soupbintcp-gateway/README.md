# Nassau SoupBinTCP Gateway

Nassau SoupBinTCP Gateway serves a MoldUDP64 session over the SoupBinTCP
protocol.

## Usage

Run Nassau SoupBinTCP Gateway with Java:

```
java -jar nassau-soupbintcp-gateway.jar <configuration-file>
```

The command line arguments are as follows:

- `<configuration-file>`: A configuration file. The configuration file
  specifies how to join a MoldUDP64 session and serve a SoupBinTCP session.

Once started, the application starts listening for incoming connections from
SoupBinTCP clients.

## Configuration

Nassau SoupBinTCP Gateway uses a configuration file to specify how to join a
MoldUDP64 session and serve a SoupBinTCP session. The MoldUDP64 session is
called the upstream, the SoupBinTCP session the downstream.

The following configuration parameters are required:

```
upstream {

    # The IP address or name of the network interface for the MoldUDP64 session.
    multicast-interface = 127.0.0.1

    # The IP address of the multicast group for the MoldUDP64 session.
    multicast-group = 224.0.0.1

    # The UDP port for the MoldUDP64 session.
    multicast-port = 5000

    # The IP address of the MoldUDP64 request server.
    request-address = 127.0.0.1

    # The UDP port of the MoldUDP64 request server.
    request-port = 5001

}

downstream {

    # The local IP address for the SoupBinTCP server.
    address = 127.0.0.1

    # The local TCP port for the SoupBinTCP server.
    port = 5000

}
```

See the `etc` directory for an example configuration file.

## License

Released under the Apache License, Version 2.0.
