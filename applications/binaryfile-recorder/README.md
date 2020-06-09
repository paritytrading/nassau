# Nassau BinaryFILE Recorder

Nassau BinaryFILE Recorder records a MoldUDP64 or SoupBinTCP session to a
BinaryFILE file.

## Usage

Run Nassau BinaryFILE Recorder with Java:

```
java -jar nassau-binaryfile-recorder.jar <configuration-file> <output-file>
```

The command line arguments are as follows:

- `<configuration-file>`: A configuration file. The configuration file
  specifies how to join a MoldUDP64 session or connect to a SoupBinTCP
  session.

- `<output-file>`: The output file. The application writes received messages
  to the output file in the BinaryFILE file format.

Once started, the application starts writing received messages to the output
file. The application terminates when it receives a packet indicating the End
of Session or, in case of SoupBinTCP, the SoupBinTCP server disconnects. You
can also terminate the application at any time.

## Configuration

Nassau BinaryFILE Recorder uses a configuration file to specify how to join a
MoldUDP64 session or connect to a SoupBinTCP session.

The following configuration parameters are required for a MoldUDP64 session:

```
session {

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
```

The following configuration parameters are required for a SoupBinTCP session:

```
session {

    # The IP address of the SoupBinTCP server.
    address = 127.0.0.1

    # The TCP port of the SoupBinTCP server.
    port = 5000

    # The SoupBinTCP username.
    username = nassau

    # The SoupBinTCP password.
    password = nassau

}
```

See the `etc` directory for example configuration files.

## License

Released under the Apache License, Version 2.0.
