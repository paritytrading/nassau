# Nassau

Nassau implements NASDAQ transport protocols on the JVM.

You can use Nassau to connect to [Nasdaq][] and other network endpoints that
use NASDAQ transport protocols. You can also use it to provide your own
services using these protocols.

  [Nasdaq]: http://nasdaq.com

Nassau is designed to exhibit low and predictable latency. To achieve this, it
supports non-blocking, zero-copy networking and does not allocate any memory
on message reception or transmission. The SoupBinTCP implementation can
achieve [4.8 µs round-trip time (RTT)](examples/soupbintcp-client) over the
loopback interface.

Nassau requires Java Runtime Environment (JRE) 8 or newer.

## Protocols

Nassau implements the following protocols:

- [**NASDAQ SoupBinTCP 3.00**][soupbintcp]: a reliable transport protocol for
  applications requiring delivery of delimited, sequenced messages between two
  endpoints.

- [**NASDAQ MoldUDP64 1.00**][moldudp64]: a reliable transport protocol for
  applications requiring delivery of delimited, sequenced messages from one
  sender to many receivers.

- [**NASDAQ BinaryFILE 1.00**][binaryfile]: a file format for storing
  delimited messages.

  [soupbintcp]: http://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/soupbintcp.pdf
  [moldudp64]: http://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/moldudp64.pdf
  [binaryfile]: http://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/binaryfile.pdf

## Download

See the [latest release][] on GitHub.

  [latest release]: https://github.com/paritytrading/nassau/releases/latest

## Modules

Nassau contains the following libraries:

- [**Core**](libraries/core) implements the NASDAQ transport protocols.

- [**Utilities**](libraries/util) contains utility methods for working with
  NASDAQ transport protocols.

Nassau contains the following applications:

- [**BinaryFILE Recorder**](applications/binaryfile-recorder) records a
  MoldUDP64 or SoupBinTCP session to a BinaryFILE file.

- [**SoupBinTCP Gateway**](applications/soupbintcp-gateway) serves a MoldUDP64
  session over the SoupBinTCP protocol.

Nassau contains the following example applications:

- [**SoupBinTCP Test Server**](examples/soupbintcp-server) implements a simple
  SoupBinTCP server.

- [**SoupBinTCP Test Client**](examples/soupbintcp-client) implements a simple
  SoupBinTCP client.

Nassau contains the following test application:

- [**BinaryFILE Performance Test**](tests/binaryfile-perf-test) is a simple
  benchmark for the BinaryFILE implementation.

## Links

For more information on Nassau:

- Follow [@paritytrading](https://twitter.com/paritytrading) on Twitter for
  news and announcements
- Join [paritytrading/chat](https://gitter.im/paritytrading/chat) on Gitter
  for discussions

## License

Copyright 2014 Nassau authors.

Released under the Apache License, Version 2.0. See `LICENSE.txt` for details.
