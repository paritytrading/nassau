Nassau
======

Nassau implements NASDAQ transport protocols on the JVM.


Download
--------

Add a Maven dependency to Nassau:

    <dependency>
      <groupId>org.jvirtanen.nassau</groupId>
      <artifactId>nassau</artifactId>
      <version><!-- latest version --></version>
    </dependency>


Features
--------

Nassau implements the following protocols:

  - **NASDAQ SoupBinTCP 3.00**: a reliable transport protocol for applications
    requiring delivery of delimited, sequenced messages between two endpoints.

  - **NASDAQ MoldUDP64 1.00**: a reliable transport protocol for applications
    requiring delivery of delimited, sequenced messages from one sender to
    many receivers.

  - **NASDAQ BinaryFILE 1.00**: a file format for storing delimited messages.

Besides the library, Nassau contains the following applications:

  - [**SoupBinTCP Performance Test**](nassau-soupbintcp-perf-test) is a simple
    latency benchmark for the SoupBinTCP implementation.


License
-------

Nassau is released under the Apache License, Version 2.0. See `LICENSE` for
details.
