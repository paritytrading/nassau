# Nassau

Nassau is a fast Nasdaq transport protocol library for the JVM.

You can use Nassau to connect to [Nasdaq][] and other network endpoints that
use Nasdaq transport protocols. You can also use it to provide your own
services using these protocols.

  [Nasdaq]: https://www.nasdaq.com

Nassau is designed to exhibit low and predictable latency. To achieve this, it
supports non-blocking, zero-copy networking and does not allocate memory on
message reception or transmission.

Nassau requires Java Runtime Environment (JRE) 8 or newer.

## Protocols

Nassau implements the following protocols:

- [**SoupBinTCP 3.00**][soupbintcp]: a reliable transport protocol for
  applications requiring delivery of delimited, sequenced messages between two
  endpoints.

- [**MoldUDP64 1.00**][moldudp64]: a reliable transport protocol for
  applications requiring delivery of delimited, sequenced messages from one
  sender to many receivers.

- [**BinaryFILE 1.00**][binaryfile]: a file format for storing delimited
  messages.

  [soupbintcp]: https://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/soupbintcp.pdf
  [moldudp64]: https://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/moldudp64.pdf
  [binaryfile]: https://www.nasdaqtrader.com/content/technicalsupport/specifications/dataproducts/binaryfile.pdf

## Download

See the [latest release][] on GitHub.

  [latest release]: https://github.com/paritytrading/nassau/releases/latest

## Modules

Nassau contains the following libraries:

- [**Core**](libraries/core) implements the Nasdaq transport protocols.

- [**Utilities**](libraries/util) contains utility methods for working with
  Nasdaq transport protocols.

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

- Follow [paritytrading@fosstodon.org](https://fosstodon.org/@paritytrading)
  on Mastodon for news and announcements
- Join [the community][GitHub Discussions] on GitHub for discussions

  [GitHub Discussions]: https://github.com/paritytrading/nassau/discussions

## License

Copyright 2014 Nassau authors.

Released under the Apache License, Version 2.0. See `LICENSE.txt` for details.
