Nassau
======

Nassau implements NASDAQ transport protocols on the JVM.

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

  [Wiki]: https://github.com/jvirtanen/nassau/wiki/

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
      <groupId>org.jvirtanen.nassau</groupId>
      <artifactId>nassau</artifactId>
      <version><!-- latest version --></version>
    </dependency>


License
-------

Nassau is released under the Apache License, Version 2.0. See `LICENSE` for
details.
