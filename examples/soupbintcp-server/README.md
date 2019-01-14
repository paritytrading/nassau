# Nassau SoupBinTCP Test Server

Nassau SoupBinTCP Test Server is an example application implementing a
SoupBinTCP server.

## Usage

Run Nassau SoupBinTCP Test Server with Java:

```
java -jar nassau-soupbintcp-server <port>
```

When started, the application listens for a SoupBinTCP connection on the specified
port. After a SoupBinTCP session is established, it responds to Unsequenced Data 
messages with Sequenced Data messages.

## License

Released under the Apache License, Version 2.0.
