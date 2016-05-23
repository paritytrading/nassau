Nassau
======

Nassau implements NASDAQ transport protocols on the JVM.

You can use Nassau to connect to NASDAQ, SIX Swiss Exchange, and other network
endpoints that use NASDAQ transport protocols. You can also use it to provide
your own services using these protocols.

Nassau requires Java Runtime Environment (JRE) 7 or newer.


Features
--------

Nassau implements the following protocols:

  - **NASDAQ SoupBinTCP 3.00**: a reliable transport protocol for applications
    requiring delivery of delimited, sequenced messages between two endpoints.

  - **NASDAQ MoldUDP64 1.00**: a reliable transport protocol for applications
    requiring delivery of delimited, sequenced messages from one sender to
    many receivers.

  - **NASDAQ BinaryFILE 1.00**: a file format for storing delimited messages.

See the [Wiki][] for links to the protocol specifications.

  [Wiki]: https://github.com/paritytrading/nassau/wiki/

Besides the library, Nassau contains the following applications:

  - [**SoupBinTCP Gateway**](nassau-soupbintcp-gateway) bridges the MoldUDP64
    protocol to the SoupBinTCP protocol.

  - [**SoupBinTCP Performance Test**](nassau-soupbintcp-perf-test) is a simple
    latency benchmark for the SoupBinTCP implementation.

  - [**BinaryFILE Performance Test**](nassau-binaryfile-perf-test) is a simple
    throughput benchmark for the BinaryFILE implementation.


Download
--------

Add a Maven dependency to Nassau:

    <dependency>
      <groupId>com.paritytrading.nassau</groupId>
      <artifactId>nassau</artifactId>
      <version><!-- latest version --></version>
    </dependency>


Development
-----------

See [Parity Guide][].

  [Parity Guide]: https://github.com/paritytrading/documentation


Links
-----

For more information on Nassau:

  - Follow [@paritytrading](https://twitter.com/paritytrading) on Twitter for
    news and announcements
  - Join [paritytrading/chat](https://gitter.im/paritytrading/chat) on Gitter
    for discussions


License
-------

Nassau is released under the Apache License, Version 2.0. See `LICENSE` for
details.
