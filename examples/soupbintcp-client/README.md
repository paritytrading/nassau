# Nassau SoupBinTCP Test Client

Nassau SoupBinTCP Test Client is an example application implementing a
SoupBinTCP client.

## Usage

Run Nassau SoupBinTCP Test Client with Java:

```
java -jar nassau-soupbintcp-client.jar <host> <port> <packets> <packets-per-second>
```

The application measures the round-trip time (RTT) between sending
an Unsequenced Data packet and receiving an Sequenced Data packet.

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
