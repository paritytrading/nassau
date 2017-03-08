# Nassau SoupBinTCP Performance Test

Nassau SoupBinTCP Performance Test is a simple benchmark for the SoupBinTCP
implementation.

## Download

Download the [latest release][] from GitHub.

  [latest release]: https://github.com/paritytrading/nassau/releases/latest

## Usage

Run Nassau SoupBinTCP Performance Test with Java:

```
java -jar nassau-soupbintcp-perf-test.jar <packets>
```

The command line arguments are as follows:

- `<packets>`: The number of packets to send.

The application measures the round-trip time (RTT) over the loopback
interface. A SoupBinTCP client running on one thread sends an Unsequenced
Data packet to a SoupBinTCP server running on another thread. The server
responds to the client with a Sequenced Data packet.

## Results

The following results have been obtained on an Amazon Web Services (AWS)
c4.xlarge EC2 instance running Amazon Linux 2016.09 and OpenJDK 8:

```
Warming up...
Benchmarking...
Results (n = 50000):

      Min:       4.79 µs
   50.00%:       5.43 µs
   90.00%:       6.86 µs
   99.00%:       8.72 µs
   99.90%:      13.70 µs
   99.99%:      28.93 µs
  100.00%:      45.15 µs
```

## License

Released under the Apache License, Version 2.0.
