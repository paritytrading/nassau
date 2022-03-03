# Release Notes

## 1.0.0 (2022-03-03)

See the [upgrade instructions](UPGRADE-1.0.0.md).

- Use direct byte buffers (Jussi Virtanen)

  Switch from non-direct byte buffers, which incur performance overhead, to
  direct byte buffers.

- Clean up documentation (Jussi Virtanen)

- Improve code quality (Jan Nielsen, Jussi Virtanen)

- Clean up MoldUDP64 implementation (Jussi Virtanen)

- Remove `SystemClock` (Jussi Virtanen)

  Use the `System::currentTimeMillis` method reference instead.

- Improve I/O resource management (Jan Nielsen)

- Split Nassau SoupBinTCP Performance Test (Jan Nielsen)

  Split the SoupBinTCP performance test into a SoupBinTCP test client and
  a SoupBinTCP test server. This makes it possible to run them on different
  network hosts.

- Add `MoldUDP64Client#receive` and `MoldUDP64Client#receiveResponse` return
  value (Jussi Virtanen)

- Improve Nassau SoupBinTCP Test Client (Jan Nielsen)

  Fix coordinated omission: instead of sending the next message right after
  receiving the previous message, send messages at regular intervals.

- Improve `SoupBinTCP.LoginRequest`, `SoupBinTCP.LoginAccepted`, and
  `SoupBinTCP.LoginRejected` (Jan Nielsen, Jussi Virtanen)

  Replace direct access to message fields with getters and setters. This means
  that Nassau applications no longer need to directly use the Foundation
  library or custom means to read and write these message fields.

- Upgrade to Foundation 1.0.0 (Jussi Virtanen)

- Move `Clock` (Jussi Virtanen)

  Move it from the `com.paritytrading.nassau.time` package to the
  `com.paritytrading.nassau` package.

## 0.13.0 (2017-01-20)

- Upgrade to Java 8
- Improve support for requested initial sequence number in MoldUDP64 client
- Add downstream sequence number and message count parameters to MoldUDP64
  client status listener

## 0.12.0 (2016-12-20)

- Improve project structure
- Improve documentation
- Add requested initial sequence number support to MoldUDP64 client

## 0.11.0 (2016-11-27)

- Make SoupBinTCP server address configurable in SoupBinTCP gateway
- Update example configuration file for SoupBinTCP gateway
- Update example configuration file for BinaryFILE recorder
- Remove GZIP support from BinaryFILE writer
- Improve BinaryFILE write performance
- Add SoupBinTCP support to BinaryFILE recorder
- Fix sequence number in SoupBinTCP gateway
- Fix sequence number in SoupBinTCP utilities

## 0.10.0 (2016-07-22)

- Refactor SoupBinTCP implementation
- Remove dependency on NIO Extras 0.1.0
- Add dependency to Foundation 0.2.0
- Reduce memory allocation in SoupBinTCP implementation
- Add BinaryFILE write support
- Add BinaryFILE recorder
- Fix BinaryFILE reader interface
- Add utility methods for BinaryFILE file format
- Add utility methods for MoldUDP64 protocol
- Add utility methods for SoupBinTCP protocol

## 0.9.0 (2016-03-28)

- Move to `com.paritytrading` namespace

## 0.8.1 (2016-03-28)

- Improve API documentation

## 0.8.0 (2016-03-28)

- Add session parameter to MoldUDP64 client status listener
- Add session parameter to SoupBinTCP session status listeners

## 0.7.0 (2015-12-26)

- Improve SoupBinTCP API
- Improve SoupBinTCP performance
- Add SoupBinTCP gateway

## 0.6.0 (2015-10-06)

- Introduce new MoldUDP64 client

## 0.5.0 (2015-09-27)

- Improve MoldUDP64 gap fill performance
- Improve MoldUDP64 default message store performance
- Improve BinaryFILE performance
- Add BinaryFILE performance test
- Improve API documentation
- Add SoupBinTCP performance test

## 0.4.0 (2015-04-13)

- Add client state to MoldUDP64
- Add lower bound for built-in receive buffer size in SoupBinTCP
- Fix requested message count in MoldUDP64
- Fix end of session handling in MoldUDP64
- Add BinaryFILE support
- Add single-channel and multi-channel client for MoldUDP64

## 0.3.0 (2014-10-29)

- Fix message count handling in MoldUDP64
- Improve built-in payload transmit buffer size in SoupBinTCP
- Add session keep-alive to MoldUDP64
- Make built-in receive buffer size configurable in SoupBinTCP

## 0.2.0 (2014-08-03)

- Improve numeric handling in SoupBinTCP
- Add heartbeat timeout event to SoupBinTCP
- Add MoldUDP64 support

## 0.1.0 (2014-05-16)

- Initial release
