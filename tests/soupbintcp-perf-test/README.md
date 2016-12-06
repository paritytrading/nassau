Nassau SoupBinTCP Performance Test
==================================

Nassau SoupBinTCP Performance Test is a simple latency benchmark for the
NASDAQ SoupBinTCP 3.00 implementation.


Usage
-----

Run Nassau SoupBinTCP Performance Test with Java:

    java -jar <executable> <packets>

The latency benchmark measures the round-trip time (RTT) over the loopback
interface. A SoupBinTCP client running on one thread sends an Unsequenced
Data packet to a SoupBinTCP server running on another thread. The server
responds to the client with a Sequenced Data packet.


License
-------

Released under the Apache License, Version 2.0.
